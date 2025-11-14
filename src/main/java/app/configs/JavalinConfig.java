package app.configs;

import app.controllers.ExceptionController;
import app.exceptions.ApiException;
import app.exceptions.DatabaseException;
import app.routes.ResourceRoutes;
import app.security.controllers.AccessController;
import app.security.controllers.SecurityController;
import app.security.enums.Role;
import app.security.routes.SecurityRoutes;
import app.utils.Utils;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.Javalin;
import io.javalin.http.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class JavalinConfig {

    private static ResourceRoutes resourceRoutes = new ResourceRoutes();
    private static ObjectMapper jsonMapper = new Utils().getObjectMapper();
    private static SecurityController securityController = SecurityController.getInstance();
    private static AccessController accessController = new AccessController();
    private static Logger logger = LoggerFactory.getLogger(JavalinConfig.class);
    private static int count = 1;

    public static void configuration(io.javalin.config.JavalinConfig config) {
        config.showJavalinBanner = false;
        //TODO: api/tp_v1/routes Javalin shows a page with every route available
        config.bundledPlugins.enableRouteOverview("/routes", Role.ANYONE);
        config.router.contextPath = "/api/learn_v1"; // base path for all endpoints
        config.router.apiBuilder(resourceRoutes.getResourceRoutes());
        config.router.apiBuilder(SecurityRoutes.getSecuredRoutes());
        config.router.apiBuilder(SecurityRoutes.getSecurityRoutes());
    }

    /**
     * 2.With CORS enabled properly on your API:
     * The browser asks:
     * “Hey myapi.com, evil.com is trying to call you. Is that allowed?”
     * Your server (via your corsHeadersOptions) replies:
     * “Nope. Only requests from https://myfrontend.com are allowed.”
     * The browser then blocks evil.com’s request before it ever reaches your API.
     */


    //corsHeaders: used on normal requests to ensure the response includes proper CORS permissions.
    private static void corsHeaders(Context ctx) {
        ctx.header("Access-Control-Allow-Origin", "*");
        ctx.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        ctx.header("Access-Control-Allow-Headers", "Content-Type, Authorization");
        ctx.header("Access-Control-Allow-Credentials", "true");
    }

    //corsHeadersOptions: used specifically for preflight requests to say:
    // “yes, this cross-origin call is allowed,” with an empty 204 response.
    private static void corsHeadersOptions(Context ctx) {
        ctx.header("Access-Control-Allow-Origin", "*");
        ctx.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        ctx.header("Access-Control-Allow-Headers", "Content-Type, Authorization");
        ctx.header("Access-Control-Allow-Credentials", "true");
        ctx.status(204);
    }


    public static Javalin startServer(int port) {   //POM currently dictates port 7070, edit it and this in future!
        Javalin app = Javalin.create(JavalinConfig::configuration);

        app.before(JavalinConfig::corsHeaders);
        app.options("/*", JavalinConfig::corsHeadersOptions);

        app.beforeMatched(accessController::accessHandler);
        app.after(JavalinConfig::afterRequest);

        ExceptionController exceptionController = new ExceptionController();
        app.exception(ApiException.class, exceptionController::apiExceptionHandler);
        app.exception(DatabaseException.class, exceptionController::databaseExceptionHandler);
        app.start(port);
        return app;
    }

    public static void afterRequest(Context ctx) {
        String requestInfo = ctx.req().getMethod() + " " + ctx.req().getRequestURI();
        logger.info(" Request {} - {} was handled with status code {}", count++, requestInfo, ctx.status());
    }

    public static void stopServer(Javalin app) {
        app.stop();
    }

}
