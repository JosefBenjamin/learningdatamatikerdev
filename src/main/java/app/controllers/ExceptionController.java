package app.controllers;


import app.exceptions.ApiException;
import app.dtos.exceptiondtos.ErrorMessageDTO;
import app.exceptions.DatabaseException;
import io.javalin.http.Context;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.NoResultException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExceptionController {
    private final Logger LOGGER = LoggerFactory.getLogger(ExceptionController.class);

    public void apiExceptionHandler(ApiException e, Context ctx) {
        // Get ApiException code from when thrown, should correlate with HTTP status codes
        // Sets the HTTP status line
        ctx.status(e.getCode());

        // Log
        // turns each {} corresponding to a string
        String requestInfo = ctx.attribute("requestInfo");
        if (requestInfo == null) {
            requestInfo = "";
        }
        LOGGER.error("{} {} {}", requestInfo, ctx.status(), e.getMessage());

        // Return a compact JSON payload for the client
        // HTTP body response
        ctx.json(new ErrorMessageDTO(e.getCode(), e.getMessage()));
    }


    public void databaseExceptionHandler(DatabaseException e, Context ctx) {
        // Get DatabaseException code from when thrown, use the provided status (usually 500)
        // Sets the HTTP status line
        ctx.status(e.getCode());

        //Log
        String requestInfo = ctx.attribute("requestInfo");
        if (requestInfo == null) {
            requestInfo = "";
        }
        LOGGER.error("{} {} {}", requestInfo, ctx.status(), e.getMessage());

        // Return a compact JSON payload for the client
        // HTTP body response
        ctx.json(new ErrorMessageDTO(e.getCode(), e.getMessage()));
    }

    public void illegalArgumentExceptionHandler(IllegalArgumentException e, Context ctx) {
        // Get DatabaseException code from when thrown, use the provided status (usually 500)
        // Sets the HTTP status line
        ctx.status(400);

        //Log
        String requestInfo = ctx.attribute("requestInfo");
        if (requestInfo == null) {
            requestInfo = "";
        }
        LOGGER.error("{} {} {}", requestInfo, ctx.status(), e.getMessage());

        // Return a compact JSON payload for the client
        // HTTP body response
        ctx.json(new ErrorMessageDTO(400, e.getMessage()));
    }

    public void entityNotFoundExceptionHandler(EntityNotFoundException e, Context ctx) {
        // Get DatabaseException code from when thrown, use the provided status (usually 500)
        // Sets the HTTP status line
        ctx.status(404);

        //Log
        String requestInfo = ctx.attribute("requestInfo");
        if (requestInfo == null) {
            requestInfo = "";
        }
        LOGGER.error("{} {} {}", requestInfo, ctx.status(), e.getMessage());

        // Return a compact JSON payload for the client
        // HTTP body response
        ctx.json(new ErrorMessageDTO(404, e.getMessage()));
    }

    public void noResultExceptionHandler(NoResultException e, Context ctx) {
        // Get DatabaseException code from when thrown, use the provided status (usually 500)
        // Sets the HTTP status line
        ctx.status(404);

        //Log
        String requestInfo = ctx.attribute("requestInfo");
        if (requestInfo == null) {
            requestInfo = "";
        }
        LOGGER.error("{} {} {}", requestInfo, ctx.status(), e.getMessage());

        // Return a compact JSON payload for the client
        // HTTP body response
        ctx.json(new ErrorMessageDTO(404, e.getMessage()));
    }

    public void runtimeExceptionHandler(RuntimeException e, Context ctx) {
        // Get DatabaseException code from when thrown, use the provided status (usually 500)
        // Sets the HTTP status line
        ctx.status(400);

        //Log
        String requestInfo = ctx.attribute("requestInfo");
        if (requestInfo == null) {
            requestInfo = "";
        }
        LOGGER.error("{} {} {}", requestInfo, ctx.status(), e.getMessage());

        // Return a compact JSON payload for the client
        // HTTP body response
        ctx.json(new ErrorMessageDTO(400, e.getMessage()));
    }








}
