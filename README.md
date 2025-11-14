# Trip Planner API
This project is for a mock exam, for the 3rd semester in Computer Science. 
The project is a backend system for an e-commerce platform offering guided travel experiences (commonly known as charter). 
Users can browse, create, update, and delete trips, view available guides, and see relevant packing lists fetched from an external API based on trip category.



| Exception strategy | What it means | Pros | Cons |
| --- | --- | --- | --- |
| Local handling | Each controller wraps service calls in try/catch and builds the HTTP response on the spot. | Easy to reason about per endpoint; no extra wiring. | Lots of duplicated code; harder to keep responses consistent. |
| Global handling | Controllers let exceptions bubble up and a single handler (e.g., `ExceptionController`) turns them into HTTP responses. | One place to change behavior; consistent logs and payloads. | Requires registering handlers up front; can feel “indirect” when debugging. |

**Global exception handling tweaks**
- `JavalinConfig` now constructs a single `ExceptionController` and registers it with `app.exception(...)`. That way every `ApiException` or uncaught `Exception` runs through the same logging + JSON response helper instead of relying on ad-hoc try/catch blocks.
- `ExceptionController` sets the status, logs the failure (including any `requestInfo` attribute), and returns a compact `ErrorMessageDTO` payload with status and message.
- To add a new custom exception in the future, add a handler method to `ExceptionController` (e.g., `public void myCustomHandler(MyCustomException e, Context ctx)`), register it with `app.exception(MyCustomException.class, exceptionController::myCustomHandler)`, and decide the status + JSON payload you want to send.

```
        ExceptionController exceptionController = new ExceptionController();
        app.exception(ApiException.class, exceptionController::apiExceptionHandler);
        app.exception(DatabaseException.class, exceptionController::databaseExceptionHandler);
```
What they do: 

They register global exception handlers with your Javalin app.
So whenever one of those exceptions is thrown anywhere in your code (in a controller, service, etc.), 
Javalin will automatically catch it and forward it to the corresponding handler method in your ExceptionController.

✅ In short

These lines tell Javalin:

“If an ApiException or DatabaseException ever escapes a route, don’t crash — call my custom error handler instead.”

It’s like a global try–catch, but centralized and clean.


## Context ctx
```
ctx.body();           // read request body as String
ctx.bodyAsClass(MyDTO.class);  // parse JSON into a DTO
ctx.status(201);      // set HTTP status code
ctx.json(someObject); // send JSON back to client
ctx.pathParam("id");  // get /api/item/{id}
ctx.attribute("x", obj) // Store an object for this request
ctx.attribute("x") // Retrieve it later
```


## Utils

| Method | Plain-English explainer | When to use it |
| --- | --- | --- |
| `getPropertyValue` | Finds a value in a `.properties` file and trims any extra spaces. | Grab configuration data such as the database URL or credentials. |
| `getObjectMapper` | Builds a Jackson mapper that already handles our date formats and skips unknown JSON fields. | Convert Java objects to JSON (and back) using the shared project settings. |
| `convertToJsonMessage` | Shapes a JSON string with your message plus the current HTTP status from Javalin’s context. | Return quick API responses like “created” or friendly error notes. |

## Lazy Loading

| Topic | Plain-English explainer | What to do |
| --- | --- | --- |
| Why the error appears | `LazyInitializationException` pops up when JPA tries to grab lazily loaded data after the transaction is already closed—think “I asked for the guide’s trips after leaving the DB session.” | Keep conversions inside the active transaction or only touch fields that are eager. |
| Fetching upfront | If you need the extra data, load it before the session closes using `JOIN FETCH`, entity graphs, or purpose-built queries. | Craft DAO queries that pull everything you’ll expose in the DTO. |
| DTO-first approach | Converting entities to DTOs immediately (in the DAO or service) keeps lazy proxies from firing later in the controller. | Return DTOs or close the transaction only after mapping is done. |
| EAGER vs LAZY quick view | EAGER loads parent and child together—safe but heavier queries. LAZY loads just the parent and defers the child until you ask for it. | If you flip to LAZY, make sure to fetch the children before the DAO closes its `EntityManager` by using fetch joins, entity graphs, or accessing them during conversion. |

**Lazy DAO mock-ups**

```java
// GuideDAO example: fetch only the parent (safe with LAZY as long as you avoid guide.getTrips() later)
public Guide findGuide(Long id) {
    try (EntityManager em = emf.createEntityManager()) {
        return em.find(Guide.class, id); // parent fields are available; child collection stays lazy
    }
}
```

```java
// GuideDAO example: fetch parent + trips when you know the service needs them
public Guide findGuideWithTrips(Long id) {
    try (EntityManager em = emf.createEntityManager()) {
        return em.createQuery(
                "SELECT g FROM Guide g JOIN FETCH g.trips WHERE g.id = :id",
                Guide.class)
            .setParameter("id", id)
            .getSingleResult(); // trips are loaded eagerly for this call only
    }
}
```

## Controller Wiring

| Approach | Pros | Cons |
| --- | --- | --- |
| Constructor injection (`new TripController(tripService, guideService)`) | Single service instances, easy testing/mocking, clear dependencies. | Needs wiring code (or DI container) to supply the services. |
| Instantiate services inside controller (`new TripService()`) | Quick for demos; no extra wiring code. | Harder to test, duplicate service instances, tight coupling to service implementations. |


## User Stories

User Stories

### US-1: As a system administrator

| Role | Goal | Benefit |
| --- | --- | --- |
| As a system administrator | I want to configure the database connection and entity management using Hibernate | so that the application can persist and retrieve trip and guide data reliably.

**Context**
- Each trip has a name, start and end times, location coordinates, price, and category (e.g., beach, city, forest, lake, sea, snow).
- Each guide has personal information (name, email, phone, years of experience) and can be associated with one or more trips.

**Acceptance Criteria**
- The system must include entities for Trip and Guide with proper JPA relationships.
- The system must initialize with sample data via a Populator class.

**Interpretation (ChatGPT):** Focuses on configuring Hibernate and ensuring starter data exists through a populator.

### US-2: As a developer

| Role | Goal | Benefit |
| --- | --- | --- |
| As a developer | I want to create, read, update, and delete trip and guide records through DAOs | so that I can manage data consistently across the application.

**Acceptance Criteria**
- TripDAO implements CRUD and allows linking trips and guides.
- GuideDAO implements basic CRUD as needed.
- DTOs are used for all data exchange between layers.

**Interpretation (ChatGPT):** Emphasizes consistent CRUD operations and DTO usage across the service layer.

### US-3: As a REST API consumer

| Role | Goal | Benefit |
| --- | --- | --- |
| As a REST API consumer | I want to manage trips using HTTP endpoints | so that I can perform standard CRUD operations and attach guides to trips.

**Acceptance Criteria**
- GET /trips returns all trips.
- GET /trips/{id} returns trip details including guide and packing items.
- POST /trips creates a trip.
- PUT /trips/{id} updates a trip.
- DELETE /trips/{id} deletes a trip.
- PUT /trips/{tripId}/guides/{guideId} links an existing guide to a trip.

**Interpretation (ChatGPT):** Defines the REST surface for trip management and guide attachment workflows.

### US-4: As a trip planner

| Role | Goal | Benefit |
| --- | --- | --- |
| As a trip planner | I want to view and filter trips by category | so that I can easily find trips that fit a specific travel type.

**Acceptance Criteria**
- A GET /trips?category={category} endpoint filters trips by category using JPA or streams.

**Interpretation (ChatGPT):** Introduces category filtering to support targeted trip searches.

### US-5: As an analyst

| Role | Goal | Benefit |
| --- | --- | --- |
| As an analyst | I want to see the total value of trips offered by each guide | so that I can analyze guide performance and revenue contribution.

**Acceptance Criteria**
- Endpoint: GET /trips/guides/totalprice returns JSON with each guide’s ID and total trip price sum.

**Interpretation (ChatGPT):** Requires aggregation endpoints to support revenue analysis per guide.

### US-6: As a traveler

| Role | Goal | Benefit |
| --- | --- | --- |
| As a traveler | I want to see recommended packing items for my trip’s category | so that I know what to bring along.

**Acceptance Criteria:**
- When retrieving a trip by ID, the response must include packing items fetched from the external API.
- A separate endpoint provides the total packing weight per trip (GET /trips/{id}/packing/weight).

**Interpretation (ChatGPT):** Ties external packing data into trip responses and adds a helper endpoint for weight totals.

**Details about the external Packing API**

- Details about the external Packing API
- The external API is available at https://packingapi.cphbusinessapps.dk/packinglist/{category}.
- The available categories are beach, city, forest, lake, sea and snow.
- The API returns a JSON object with a list of items to pack for the trip in this format:

```
{
"items": [
{
"name": "Beach Umbrella",
"weightInGrams": 1200,
"quantity": 1,
"description": "Sunshade umbrella for beach outings.",
"category": "beach",
"createdAt": "2024-10-30T17:44:58.547Z",
"updatedAt": "2024-10-30T17:44:58.547Z",
"buyingOptions": [
{
"shopName": "Sunny Store",
"shopUrl": "https://shop3.com",
"price": 50
},
{
"shopName": "Beach Essentials",
"shopUrl": "https://shop4.com",
"price": 55
}
]
},
...
]
}
```

- NB: The date format for createdAt and updatedAt is ZonedDateTime format, like 2024-10-30T17:44:58.547Z. Jackson might need an extra dependency to handle this format, and this custom configuration of the ObjectMapper:

```
ObjectMapper objectMapper = new ObjectMapper();
objectMapper.registerModule(new JavaTimeModule());
objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
```

**Interpretation (ChatGPT):** Highlights the external dependency and necessary Jackson configuration for ZonedDateTime handling.

### US-7: As a tester

| Role | Goal | Benefit |
| --- | --- | --- |
| As a tester | I want automated tests for all REST endpoints | so that the application’s functionality is verified and regressions are avoided.

**Acceptance Criteria:**
- Each endpoint has corresponding unit/integration tests.
- Tests set up mock data and verify JSON responses and status codes.
- Trip-by-ID tests confirm packing data is included.

**Interpretation (ChatGPT):** Stresses coverage of every endpoint with meaningful assertions, including packing data validation.

### US-8: As a secure API consumer

| Role | Goal | Benefit |
| --- | --- | --- |
| As a secure API consumer | I want to log in and access protected endpoints using a JWT token | so that only authorized users can modify or view sensitive data.

**Acceptance Criteria:**
- POST /login authenticates and returns a JWT.
- Protected endpoints validate the token and enforce roles.
- Unauthorized requests return 401 Unauthorized.
- Tests verify secure access behavior.

**Interpretation (ChatGPT):** Defines the authentication flow and expected security checks for protected routes.

## controllers

| Notes |
| --- |
| return void |

## Services

| Notes |
| --- |
| Uses daos to for CRUD |
| Make entities to DTO |
| return DTOs |

## Q&A Reference

### Q: What does MVC stands for

Model  
View  
Controller

### Q: When should I throw an exception in the method signature 

```
Checked expcetions when thrown should also be defined in the method signature.
```

### Q: What's the difference between typedQuery and query 



### Q: WHat's the difference between @OneToMany(orphanRemoval = true) vs removeTrip method?
- orphanRemoval is a setting on the relationship annotation (like @OneToMany).
- It tells Hibernate what to do with child entities when they’re removed from the parent’s collection.
- If false → “just unlink it.”
- If true → “delete it completely.”
- trip.setGuide(null) manually unlinks without deletion.

So the big question you ask yourself is:

“Do I want the child (Trip) to survive without its parent (Guide)?”
If yes → orphanRemoval = false.
If no → orphanRemoval = true.

### Q: Why most all entities carry a lombok @NoArgsConstructor annotation? 

When Hibernate makes an instance of the entity, it doesn't want anything in the parameters,
because it wants to set them at the DB level. In the words of ChatGPT: 
```
Hibernate doesn’t want any parameters because it’s responsible for populating all the fields from the database itself.
```

### Q: What's the difference between the @GeneratedValue strategies? 
- IDENTITY: “Let the database auto-increment it.” (simple and common)
- SEQUENCE: “Ask the database for the next number first.” (efficient for batch inserts)
- TABLE: “Store a counter in a separate table.” (universal fallback, slow)
- AUTO: “Hibernate, you decide.” (convenient but not predictable)

### Q: What's the difference between FetchType LAZY and EAGER loading?
- EAGER → Right now.
When you fetch a Guide, Hibernate immediately loads all its trips too — even if you never use them.
It’s like saying:
“Whenever I get a Guide, also bring all their trips along.”

- LAZY → Only if needed.
When you fetch a Guide, Hibernate doesn’t load the trips right away — it creates a lightweight proxy.
The trips are only fetched later, the moment you call something like guide.getTrips().
“Just give me the Guide for now — I’ll ask for trips later if I need them.”

### Q: What's the between the CascadeTypes? 
“What should happen to related entities when I perform an operation?”

It controls what Hibernate does to child entities (Trips here) when something happens to the parent (Guide).
- CascadeType.ALL → applies all cascade actions (persist, merge, remove, refresh, detach).
Example:
- Saving a Guide → also saves all its Trips.
- Deleting a Guide → also deletes all its Trips.
- Updating a Guide → also updates the related Trips.

Other options include:
- PERSIST → Save trips when saving the guide.
- REMOVE → Delete trips when deleting the guide.
- MERGE → Update trips when updating the guide.
- DETACH / REFRESH → Used less often.

💡 Rule of thumb:
Use cascading when child entities truly “belong” to the parent and shouldn’t exist independently (like trips belonging to a specific guide).

**Interpretation (ChatGPT):** These Q&A reminders summarize common Hibernate patterns and settings relevant to the project.
