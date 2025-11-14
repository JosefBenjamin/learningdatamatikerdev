package app.security.controllers;

import io.javalin.http.Context;


public interface IAuthorizationController {
    void accessHandler(Context ctx);
}
