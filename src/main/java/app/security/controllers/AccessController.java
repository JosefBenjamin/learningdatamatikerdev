package app.security.controllers;

import app.security.enums.Role;
import app.security.services.SecurityService;
import dk.bugelhartmann.UserDTO;
import io.javalin.http.Context;
import io.javalin.http.UnauthorizedResponse;
import io.javalin.security.RouteRole;

import java.util.Map;
import java.util.Set;

/**
 * Purpose: To handle security in the API at the route level
 *  Author: Jon Bertelsen
 */

//TODO: Might rename to authorization controller
public class AccessController implements IAuthorizationController {

    private SecurityController securityController = SecurityController.getInstance();

    /**
     * This method checks if the user has the necessary roles to access the route.
     * @param ctx
     */
    public void accessHandler(Context ctx) {

        // If no roles are specified on the endpoint, then anyone can access the route
        if (ctx.routeRoles().isEmpty() || ctx.routeRoles().contains(Role.ANYONE)){
            return;
        }

        // Check if the user is authenticated
        try {
            securityController.authenticate().handle(ctx);
        } catch (UnauthorizedResponse e) {
            throw new UnauthorizedResponse(e.getMessage());
        } catch (Exception e) {
            throw new UnauthorizedResponse("In order to access this endpoint, you must be logged in!");
        }

        // Check if the user has the necessary roles to access the route
        UserDTO user = ctx.attribute("user");
        Set<RouteRole> allowedRoles = ctx.routeRoles(); // roles allowed for the current route
        if (!securityController.authorize(user, allowedRoles)) {
            throw new UnauthorizedResponse("Unauthorized with roles: " + user.getRoles() + ". Needed roles are: " + allowedRoles);
        }
    }


}
