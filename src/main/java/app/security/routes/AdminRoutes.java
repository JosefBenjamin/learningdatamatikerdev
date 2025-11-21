package app.security.routes;

import app.security.controllers.SecurityController;
import app.security.enums.Role;
import app.utils.Utils;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.apibuilder.EndpointGroup;

import static io.javalin.apibuilder.ApiBuilder.*;

public class AdminRoutes {
    private static ObjectMapper jsonMapper = new Utils().getObjectMapper();
    private static SecurityController securityController = SecurityController.getInstance();


    public EndpointGroup getSecuredRoutes() {
        return () -> {
            path("/protected", () -> {
                get("/admin_demo", (ctx) -> ctx.json(jsonMapper.createObjectNode().put("msg", "Hello from ADMIN Protected")), Role.ADMIN);
            });

            path("/auth", () -> {
                post("/user/addrole", securityController.addRole(), Role.ADMIN); //Change to Role.ADMIN
            });

        };
    }

}
