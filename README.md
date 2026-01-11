# LearnDatamatiker API

A RESTful API for an online learning resource sharing platform where contributors can share and discover educational content.

## Base URL

```
/api/learn_v1
```

## Authentication

The API uses JWT (JSON Web Token) for authentication. Include the token in the `Authorization` header:

```
Authorization: Bearer <your_token>
```

### Roles
- `ANYONE` - Public access, no authentication required
- `USER` - Authenticated users
- `ADMIN` - Administrator privileges

---

## Authentication Endpoints

### Health Check
```http
GET /auth/healthcheck
```
**Role:** ANYONE

### Register
```http
POST /auth/register
```
**Role:** ANYONE

**Request Body:**
```json
{
  "username": "string",
  "password": "string"
}
```

### Login
```http
POST /auth/login
```
**Role:** ANYONE

**Request Body:**
```json
{
  "username": "string",
  "password": "string"
}
```

**Response:**
```json
{
  "token": "jwt_token_here",
  "username": "string"
}
```

### Add Role to User (Admin Only)
```http
POST /auth/user/addrole
```
**Role:** ADMIN

---

## Resource Endpoints

### Get All Resources
```http
GET /resources
```
**Role:** ANYONE

**Query Parameters (optional):**
| Parameter | Type | Description |
|-----------|------|-------------|
| `page` | Integer | Page number (0-indexed). Required with `limit` for pagination. |
| `limit` | Integer | Items per page (max 100). Required with `page` for pagination. |

**Response without pagination:** Array of `SimpleResourceDTO`

**Response with pagination (`?page=0&limit=20`):**
```json
{
  "content": [SimpleResourceDTO, ...],
  "page": 0,
  "limit": 20,
  "totalElements": 150,
  "totalPages": 8,
  "hasNext": true,
  "hasPrevious": false
}
```

### Get Newest Resources
```http
GET /resources/newest
```
**Role:** ANYONE

**Response:** Array of `SimpleResourceDTO` sorted by creation date (newest first)

### Get Recently Updated Resources
```http
GET /resources/updated
```
**Role:** ANYONE

**Response:** Array of `SimpleResourceDTO` sorted by modification date

### Get Resource by ID
```http
GET /resources/id/{id}
```
**Role:** ANYONE

| Parameter | Type | Description |
|-----------|------|-------------|
| `id` | Long | Database ID of the resource |

### Get Resource by Learning ID
```http
GET /resources/learning/{learning_id}
```
**Role:** ANYONE

| Parameter | Type | Description |
|-----------|------|-------------|
| `learning_id` | Integer | Unique learning resource ID |

### Get Resources by Format Category
```http
GET /resources/format/{format_category}
```
**Role:** USER, ADMIN

| Parameter | Type | Description |
|-----------|------|-------------|
| `format_category` | String | One of the format categories (see below) |

### Get Resources by Sub Category
```http
GET /resources/sub/{sub_category}
```
**Role:** USER, ADMIN

| Parameter | Type | Description |
|-----------|------|-------------|
| `sub_category` | String | One of the sub categories (see below) |

### Get Resource by Title
```http
GET /resources/title/{title}
```
**Role:** ANYONE

### Get Resources by Contributor
```http
GET /resources/contributor/{name}
```
**Role:** ANYONE

| Parameter | Type | Description |
|-----------|------|-------------|
| `name` | String | GitHub profile or screen name |

### Search Resources by Keyword
```http
GET /resources/search/{keyword}
```
**Role:** ANYONE

### Create Resource
```http
POST /resources
```
**Role:** USER, ADMIN

**Request Body:**
```json
{
  "learningResourceLink": "https://example.com/resource",
  "title": "Resource Title",
  "formatCategory": "YOUTUBE",
  "subCategory": "PROGRAMMING",
  "description": "A description of the learning resource"
}
```

**Response:** `SimpleResourceDTO`

### Update Resource
```http
PUT /resources/{learning_id}
```
**Role:** USER, ADMIN (owner or admin only)

**Request Body:** Partial `SimpleResourceDTO` with fields to update

### Delete Resource
```http
DELETE /resources/{learning_id}
```
**Role:** USER, ADMIN (owner or admin only)

**Response:**
```json
{
  "resourceDeleted": true
}
```

### Like a Resource
```http
POST /resources/{id}/like
```
**Role:** USER, ADMIN

| Parameter | Type | Description |
|-----------|------|-------------|
| `id` | Long | Database ID of the resource |

**Response:**
```json
{
  "liked": true
}
```

### Unlike a Resource
```http
DELETE /resources/{id}/like
```
**Role:** USER, ADMIN

| Parameter | Type | Description |
|-----------|------|-------------|
| `id` | Long | Database ID of the resource |

**Response:**
```json
{
  "unliked": true
}
```

---

## Contributor Endpoints

### Get Contributors
```http
GET /contributors
```
**Role:** ANYONE

**Query Parameters:**
| Parameter | Type | Description |
|-----------|------|-------------|
| `name` | String | Get contributor by GitHub/screen name |
| `id` | Long | Get contributor by ID (ADMIN only) |

### Get Contributors by Most Contributions
```http
GET /contributors/contributions
```
**Role:** ANYONE

**Response:** Contributors sorted by contribution count (descending)

### Update Contributor
```http
PUT /contributors/{name}
```
**Role:** USER, ADMIN

**Request Body:** `ProfileDTO`

### Delete Contributor
```http
DELETE /contributors/{name}
```
**Role:** USER, ADMIN

---

## Data Models

### SimpleResourceDTO
```json
{
  "learningId": 1,
  "learningResourceLink": "https://example.com",
  "title": "Resource Title",
  "formatCategory": "YOUTUBE",
  "subCategory": "PROGRAMMING",
  "description": "Description text",
  "simpleContributorDTO": {
    "contributorId": 1,
    "githubProfile": "username",
    "screenName": "Display Name",
    "contributions": 5
  },
  "createdAt": "2024-01-15T10:30:00",
  "modifiedAt": "2024-01-15T12:45:00",
  "likeCount": 42,
  "isLikedByCurrentUser": true
}
```

**Note:** `isLikedByCurrentUser` is `true`/`false` if logged in, `null` if not authenticated.

### ProfileDTO
```json
{
  "githubProfile": "username",
  "screenName": "Display Name",
  "contributions": 5,
  "resources": []
}
```

---

## Enums

### FormatCategory
| Value | Description |
|-------|-------------|
| `PDF` | PDF documents |
| `YOUTUBE` | YouTube videos |
| `ARTICLE` | Online articles |
| `EBOOK` | E-books |
| `PODCAST` | Audio podcasts |
| `GAME` | Educational games |
| `BLOGPOST` | Blog posts |
| `OTHER` | Other formats |

### SubCategory
| Value | Description |
|-------|-------------|
| `PROGRAMMING` | General programming |
| `WEB_DEVELOPMENT` | Frontend/backend/fullstack |
| `DATA_SCIENCE` | Data analysis, ML, AI |
| `DATABASES` | SQL, NoSQL |
| `DEVOPS` | CI/CD, cloud, infrastructure |
| `ALGORITHMS` | Data structures & algorithms |
| `SECURITY` | Cybersecurity |
| `MOBILE` | Mobile development |
| `DESIGN` | UI/UX design |
| `CAREER` | Career development |

---

## Error Responses

```json
{
  "status": 400,
  "message": "Error description"
}
```

Common HTTP status codes:
- `200` - Success
- `201` - Created
- `400` - Bad Request
- `403` - Forbidden
- `404` - Not Found
- `500` - Internal Server Error

---

## Feature Suggestions

The following features would be easy to implement with high impact:


