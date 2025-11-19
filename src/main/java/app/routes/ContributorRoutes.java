package app.routes;

import app.controllers.ContributorController;
import app.security.enums.Role;
import app.services.ContributorService;
import io.javalin.apibuilder.EndpointGroup;

import static io.javalin.apibuilder.ApiBuilder.*;

public class ContributorRoutes {
    private final ContributorService contributorService = new ContributorService();
    private final ContributorController contributorController = new ContributorController(contributorService);

    //TODO: config.router.contextPath = "/api/learn_v1" <--base path for all endpoints
    public EndpointGroup getContributorRoutes() {
        return () -> {
            path("/contributors", () -> {
                get(ctx -> contributorController.handleContributorQuery(ctx), Role.ANYONE);
                get("/contributions", ctx -> contributorController.contributorsByMostContributions(ctx), Role.ANYONE);
                put("/{name}", ctx -> contributorController.updateAContributor(ctx), Role.USER, Role.ADMIN);
                delete("/{name}", ctx -> contributorController.deleteAContributor(ctx), Role.USER, Role.ADMIN);
            });
        };
    }

}
