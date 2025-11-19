package app.controllers;

import app.dtos.contributordtos.ContributorIdDTO;
import app.dtos.contributordtos.ContributorNameDTO;
import app.dtos.contributordtos.GitHubScreenNameListsDTO;
import app.dtos.contributordtos.ProfileDTO;
import app.services.ContributorService;
import dk.bugelhartmann.UserDTO;
import io.javalin.http.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class ContributorController {
    private final Logger LOGGER = LoggerFactory.getLogger(ContributorController.class);
    private final ContributorService contributorService;

    public ContributorController(ContributorService contributorService) {
        this.contributorService = contributorService;
    }

    //TODO: GET /contributors <--> supports query params ?id= / ?name=
    public void handleContributorQuery(Context ctx){
        // 1) Extra query param if applicable ?id= or ?name=
        String idParam = ctx.queryParam("id");
        String nameParam = ctx.queryParam("name");

        //Executes if name is relevant
        if (nameParam != null){
            ContributorNameDTO contributorNameDTO = new ContributorNameDTO(nameParam);
            ProfileDTO profileDTO = contributorService.getContributorByName(contributorNameDTO);
            ctx.status(200).json(profileDTO);
            return;
        }

        //Executes if id is relevant
        if (idParam != null){
            Long id = Long.valueOf(idParam);
            ContributorIdDTO contributorIdDTO = new ContributorIdDTO(id);

            UserDTO user = ctx.attribute("user");
            boolean isAdmin = user != null && user.getRoles().contains("ADMIN");

            ProfileDTO profileDTO = contributorService.getContributorById(contributorIdDTO, isAdmin);
            ctx.status(200).json(profileDTO);
            return;
        }

        //If non path params are set it will return all contributors
        GitHubScreenNameListsDTO gitHubScreenNameListsDTO = contributorService.getAllContributors();
        ctx.status(200).json(gitHubScreenNameListsDTO);
    }


    //TODO: GET /contributors/contributions
    public void contributorsByMostContributions(Context ctx){
        GitHubScreenNameListsDTO gitHubScreenNameListsDTO = contributorService.sortByMostContributions();
        ctx.status(200).json(gitHubScreenNameListsDTO);
    }


    //TODO: PUT /contributors/{name}
    public void updateAContributor(Context ctx){
        // 1) Extract path parameter (name from the URL)
        String name = ctx.pathParam("name");
        ContributorNameDTO contributorNameDTO = new ContributorNameDTO(name);

        // 2) Extract request body
        ProfileDTO payload = ctx.bodyAsClass(ProfileDTO.class);

        // 3) Extract authenticated user context (from security middleware)
        UserDTO user = ctx.attribute("user");
        boolean isAdmin = user != null && user.getRoles().contains("ADMIN");
        Long authenticatedContributorId = contributorService.getContributorIdForUser(user);

        // 4) Call service, returns a ProfileDTO
        ProfileDTO updatedContributor = contributorService.updateContributor(
                contributorNameDTO,
                payload,
                authenticatedContributorId,
                isAdmin);

        // 5) Respond with 200 success and ProfileDTO
        ctx.status(200).json(updatedContributor);
    }


    //TODO: DELETE /contributors/{name}
    public void deleteAContributor(Context ctx){
        //1) Extract path param (name from URL)
        String name = ctx.pathParam("name");
        ContributorNameDTO contributorNameDTO = new ContributorNameDTO(name);

        //2) Extract authenticated user context (from security middleware)
        UserDTO userDTO = ctx.attribute("user");
        boolean isAdmin = userDTO != null && userDTO.getRoles().contains("ADMIN");
        Long authenticatedContributorId = contributorService.getContributorIdForUser(userDTO);


        //3) Call the server method
        contributorService.deleteContributor(contributorNameDTO,
                authenticatedContributorId, isAdmin);

        //4) Respond with 200 success
        ctx.status(200).json(Map.of("Your contributor account was deleted: ", isAdmin));
    }



}
