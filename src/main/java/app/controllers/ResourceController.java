package app.controllers;

import app.dtos.resourcedtos.LearningIdDTO;
import app.dtos.resourcedtos.SimpleResourceDTO;
import app.services.ContributorService;
import app.services.ResourceService;
import dk.bugelhartmann.UserDTO;
import io.javalin.http.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class ResourceController {
    private final Logger LOGGER = LoggerFactory.getLogger(ResourceController.class);
    private final ResourceService resourceService;
    private final ContributorService contributorService;

    public ResourceController(ResourceService resourceService, ContributorService contributorService) {
        this.resourceService = resourceService;
        this.contributorService = contributorService;
    }


    //TODO: POST resources/ (only admin or user/contributor)
    public void createResource(Context ctx) {
        // 1) Extract request body
        SimpleResourceDTO simpleResourceDTO = ctx.bodyAsClass(SimpleResourceDTO.class);

        // 2) Extract authenticated user context
        UserDTO userDTO = ctx.attribute("user");
        Long authenticatedContributorId = contributorService.getContributorIdForUser(userDTO);


        // 3) Call the service method with creation and authentication
        SimpleResourceDTO responseResource = resourceService.createResource(simpleResourceDTO, authenticatedContributorId);

        // 4) Returns http status code + json body
        ctx.status(201).json(responseResource);
    }



    //TODO: PUT resources/{learning_id}
    public void updateResource(Context ctx){
        // 1) Extract path param from URL
        Integer learningId = Integer.valueOf(ctx.pathParam("learning_id"));

        // 2) Extract json body from request
        SimpleResourceDTO extractedResource = ctx.bodyAsClass(SimpleResourceDTO.class);
        if(extractedResource.learningId() != null && !extractedResource.learningId().equals(learningId)){
            throw new IllegalArgumentException("learning id must be the same in the endpoint param, as in the json body (if relevant");
        }

        // 3) Merge learningId and extracted resource
        SimpleResourceDTO merged = new SimpleResourceDTO(
                learningId,
                extractedResource.learningResourceLink(),
                extractedResource.title(),
                extractedResource.formatCategory(),
                extractedResource.subCategory(),
                extractedResource.description(),
                extractedResource.simpleContributorDTO());

        // ) Extract user and authentication
        UserDTO userDTO = ctx.attribute("user");
        Long authenticatedContributorId = contributorService.getContributorIdForUser(userDTO);

        // 4) Call service
        SimpleResourceDTO updatedSimpleResourceDTO = resourceService.updateResource(merged, authenticatedContributorId);

        // 5) Return status code and json
        ctx.status(200).json(updatedSimpleResourceDTO);
    }


    //TODO: DELETE resources/{learning_id}
    public void deleteResource(Context ctx){
        // 1) Extract path param from URL
        Integer extractedId = Integer.valueOf(ctx.pathParam("learning_id"));
        LearningIdDTO learningIdDTO = new LearningIdDTO(extractedId);

        // 2) Extract authenticated user context (from security middleware)
        UserDTO userDTO = ctx.attribute("user");
        boolean isAdmin = userDTO.getRoles().contains("ADMIN");
        Long authenticatedContributorId = isAdmin ? null : contributorService.getContributorIdForUser(userDTO);

        // 3) Call the service method
        boolean resultOfDeletion = resourceService.deleteResource(learningIdDTO, isAdmin, authenticatedContributorId);


        // 4) Return HTTP status code
        ctx.status(200).json(Map.of("resourceDeleted", resultOfDeletion));
    }



}
