package app.routes;

import app.controllers.ResourceController;
import app.services.ContributorService;
import app.services.ResourceService;
import app.security.enums.Role;
import io.javalin.apibuilder.EndpointGroup;

import static io.javalin.apibuilder.ApiBuilder.*;

public class ResourceRoutes {
    private final ResourceService resourceService = new ResourceService();
    private final ContributorService contributorService = new ContributorService();
    private final ResourceController resourceController = new ResourceController(resourceService, contributorService);


    //TODO: config.router.contextPath = "/api/learn_v1" <--base path for all endpoints
    public EndpointGroup getResourceRoutes() {

        return () -> {
            path("/resources", () -> {
                get(ctx -> resourceController.getAllResources(ctx), Role.ANYONE);
                get("/newest", ctx -> resourceController.getNewestResources(ctx), Role.ANYONE);
                get("/updated", ctx -> resourceController.getRecentlyUpdatedResources(ctx), Role.ANYONE);
                get("/id/{id}", ctx -> resourceController.getResourceById(ctx), Role.ANYONE);
                get("/learning/{learning_id}", ctx -> resourceController.getResourceByLearningId(ctx), Role.ANYONE);
                get("/format/{format_category}", ctx -> resourceController.getResourcesByFormatCategory(ctx), Role.USER, Role.ADMIN);
                get("/sub/{sub_category}", ctx -> resourceController.getResourcesBySubCategory(ctx), Role.USER, Role.ADMIN);
                get("/title/{title}", ctx -> resourceController.getResourceByTitle(ctx), Role.ANYONE);
                get("/contributor/{name}", ctx -> resourceController.getResourcesByContributor(ctx), Role.ANYONE);
                get("/search/{keyword}", ctx -> resourceController.getResourcesByKeyword(ctx), Role.ANYONE);
                post(ctx -> resourceController.createResource(ctx), Role.USER, Role.ADMIN);
                put("/{learning_id}", ctx -> resourceController.updateResource(ctx), Role.USER, Role.ADMIN);
                delete("/{learning_id}", ctx -> resourceController.deleteResource(ctx), Role.USER, Role.ADMIN);
            });
        };
    }


}
