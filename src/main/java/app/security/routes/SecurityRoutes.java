package app.security.routes;

import app.security.controllers.SecurityController;
import app.security.enums.Role;
import app.utils.Utils;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.apibuilder.EndpointGroup;

import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.path;
import static io.javalin.apibuilder.ApiBuilder.post;


/**
 * Purpose: To handle security in the API
 *  Author: Thomas Hartmann
 */
public class SecurityRoutes {
    private static ObjectMapper jsonMapper = new Utils().getObjectMapper();
    private static SecurityController securityController = SecurityController.getInstance();

    public static EndpointGroup getSecurityRoutes() {
        return () -> {
            path("/auth", () -> {
                get("/healthcheck", ctx -> securityController.healthCheck(ctx), Role.ANYONE);
                get("/populate", ctx -> securityController.populateUsers(ctx), Role.ANYONE);
                post("/login", securityController.login(), Role.ANYONE);
                post("/register", securityController.register(), Role.ANYONE);
            });
        };
    }


}
