package app.routes;

import app.controllers.ResourceController;
import app.services.ContributorService;
import app.services.ResourceService;
import io.javalin.apibuilder.EndpointGroup;

import static io.javalin.apibuilder.ApiBuilder.*;
import static io.javalin.apibuilder.ApiBuilder.delete;

public class ResourceRoutes {
    private final ResourceService resourceService = new ResourceService();
    private final ContributorService contributorService = new ContributorService();
    private final ResourceController resourceController = new ResourceController(resourceService, contributorService);


    //TODO: config.router.contextPath = "/api/learn_v1" <--base path for all endpoints
    public EndpointGroup getResourceRoutes() {

        return () -> {
        };
    }


}
