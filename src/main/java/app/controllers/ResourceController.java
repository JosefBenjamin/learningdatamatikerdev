package app.controllers;

import app.dtos.PageDTO;
import app.dtos.categorydtos.SingleFormatCatDTO;
import app.dtos.categorydtos.SingleSubCategoryDTO;
import app.dtos.contributordtos.ContributorNameDTO;
import app.dtos.resourcedtos.LearningIdDTO;
import app.dtos.resourcedtos.ResourceIdDTO;
import app.dtos.resourcedtos.ResourceKeywordDTO;
import app.dtos.resourcedtos.ResourceTitleDTO;
import app.dtos.resourcedtos.SimpleResourceDTO;
import app.services.ContributorService;
import app.services.ResourceService;
import app.enums.FormatCategory;
import app.enums.SubCategory;
import dk.bugelhartmann.UserDTO;
import io.javalin.http.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
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

    //TODO: GET resources/{id}
    public void getResourceById(Context ctx){
        Long id = Long.valueOf(ctx.pathParam("id"));
        UserDTO userDTO = ctx.attribute("user");
        String username = userDTO != null ? userDTO.getUsername() : null;
        SimpleResourceDTO resource = resourceService.findResourceByIdWithLikes(id, username);
        ctx.status(200).json(resource);
    }

    //TODO: GET resources/{learning_id}
    public void getResourceByLearningId(Context ctx){
        Integer learningId = Integer.valueOf(ctx.pathParam("learning_id"));
        SimpleResourceDTO resource = resourceService.findResourceById(new LearningIdDTO(learningId));
        ctx.status(200).json(resource);
    }

    //TODO: GET resources/  <-- retrieve all, supports pagination with ?page=0&limit=20
    public void getAllResources(Context ctx){
        String pageParam = ctx.queryParam("page");
        String limitParam = ctx.queryParam("limit");

        if (pageParam != null && limitParam != null) {
            int page = Integer.parseInt(pageParam);
            int limit = Integer.parseInt(limitParam);
            limit = Math.min(limit, 100); // Cap at 100 to prevent abuse
            PageDTO<SimpleResourceDTO> response = resourceService.getAllResourcesPaginated(page, limit);
            ctx.status(200).json(response);
        } else {
            List<SimpleResourceDTO> response = resourceService.getAllResources();
            ctx.status(200).json(response);
        }
    }

    //TODO: GET resources/newest
    public void getNewestResources(Context ctx){
        List<SimpleResourceDTO> response = resourceService.getNewestResources();
        ctx.status(200).json(response);
    }

    //TODO: GET resources/newest
    public void getRecentlyUpdatedResources(Context ctx){
        List<SimpleResourceDTO> response = resourceService.getRecentlyUpdatedResources();
        ctx.status(200).json(response);
    }



    //TODO: GET resources/{format_category}
    public void getResourcesByFormatCategory(Context ctx){
        String formatCat = ctx.pathParam("format_category");
        FormatCategory formatCategory = FormatCategory.valueOf(formatCat.toUpperCase());

        UserDTO user = ctx.attribute("user");
        boolean isAdmin = user != null && user.getRoles().contains("ADMIN");
        Long contributorId = contributorService.getContributorIdForUser(user);

        List<SimpleResourceDTO> response = resourceService.getAllResourcesInFormatCat(
                new SingleFormatCatDTO(formatCategory),
                contributorId,
                isAdmin);
        ctx.status(200).json(response);
    }

    //TODO: GET resource/{sub_category}
    public void getResourcesBySubCategory(Context ctx){
        String subCat = ctx.pathParam("sub_category");
        SubCategory subCategory = SubCategory.valueOf(subCat.toUpperCase());

        UserDTO user = ctx.attribute("user");
        boolean isAdmin = user != null && user.getRoles().contains("ADMIN");
        Long contributorId = contributorService.getContributorIdForUser(user);

        List<SimpleResourceDTO> response = resourceService.getAllResourcesInSubCat(
                new SingleSubCategoryDTO(subCategory),
                contributorId,
                isAdmin);
        ctx.status(200).json(response);
    }

    //TODO: GET resource/{title}
    public void getResourceByTitle(Context ctx){
        String title = ctx.pathParam("title");
        SimpleResourceDTO resource = resourceService.findByTitle(new ResourceTitleDTO(title));
        ctx.status(200).json(resource);
    }

    //TODO: GET resource/{contributor}
    public void getResourcesByContributor(Context ctx){
        String contributorName = ctx.pathParam("name");
        List<SimpleResourceDTO> resources = resourceService.findByContributor(new ContributorNameDTO(contributorName));
        ctx.status(200).json(resources);
    }

    //TODO: GET resource/{keyword}
    public void getResourcesByKeyword(Context ctx){
        String keyword = ctx.pathParam("keyword");
        List<SimpleResourceDTO> resources = resourceService.findByKeyword(new ResourceKeywordDTO(keyword));
        ctx.status(200).json(resources);
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
                extractedResource.simpleContributorDTO(),
                null,
                null
                );

        // ) Extract user and authentication
        UserDTO userDTO = ctx.attribute("user");
        boolean isAdmin = userDTO.getRoles().contains("ADMIN");
        Long authenticatedContributorId = isAdmin ? null : contributorService.getContributorIdForUser(userDTO);

        // 4) Call service
        SimpleResourceDTO updatedSimpleResourceDTO = resourceService.updateResource(merged, isAdmin, authenticatedContributorId);

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


    //TODO: POST resources/{id}/like
    public void likeResource(Context ctx) {
        Long id = Long.valueOf(ctx.pathParam("id"));
        UserDTO userDTO = ctx.attribute("user");
        String username = userDTO != null ? userDTO.getUsername() : null;

        resourceService.likeResource(id, username);
        ctx.status(201).json(Map.of("liked", true));
    }

    //TODO: DELETE resources/{id}/like
    public void unlikeResource(Context ctx) {
        Long id = Long.valueOf(ctx.pathParam("id"));
        UserDTO userDTO = ctx.attribute("user");
        String username = userDTO != null ? userDTO.getUsername() : null;

        boolean removed = resourceService.unlikeResource(id, username);
        ctx.status(200).json(Map.of("unliked", removed));
    }

}
