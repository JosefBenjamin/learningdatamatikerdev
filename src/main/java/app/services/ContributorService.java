package app.services;

import app.configs.HibernateConfig;
import app.converters.ConvertToContributorDTO;
import app.daos.ContributorDAO;
import app.daos.ResourceDAO;
import app.dtos.contributordtos.*;
import app.entities.Contributor;
import app.security.daos.SecurityDAO;
import dk.bugelhartmann.UserDTO;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityNotFoundException;

import java.util.ArrayList;
import java.util.List;

public class ContributorService {
    private final EntityManagerFactory EMF = HibernateConfig.getEntityManagerFactory();
    private final ContributorDAO CONTRIBUTOR_DAO = ContributorDAO.getInstance(EMF);
    private final ResourceDAO RESOURCE_DAO = ResourceDAO.getInstance(EMF);
    private final SecurityDAO SECURITY_DAO = SecurityDAO.getInstance(EMF);
    private final ConvertToContributorDTO convertToContributeDTO = new ConvertToContributorDTO();


    //Empty constructor
    public ContributorService(){

    }

    //HELPER METHODS
    public Long getContributorIdForUser(UserDTO user) {
        if (user == null){
            return null;
        }
        Contributor contributor = CONTRIBUTOR_DAO.findByUsername(user.getUsername());
        return contributor.getId();
    }

    //SERVICE METHODS

    //TODO: GET /contributors <--> get all contributors
    public GitHubScreenNameListsDTO getAllContributors(){
        List<GithubProfileDTO> githubList = new ArrayList<>();
        List<ScreenNameProfileDTO> screeNameList = new ArrayList<>();

        for(Contributor c : CONTRIBUTOR_DAO.retrieveAll()){
            ProfileDTO x = convertToContributeDTO.convert(c);
            if(x.githubProfile() == null){
                screeNameList.add(new ScreenNameProfileDTO(x.screenName(), x.contributions(), x.resources()));
            }
            if(x.screenName() == null){
                githubList.add(new GithubProfileDTO(x.githubProfile(), x.contributions(), x.resources()));
            }
        }
        return new GitHubScreenNameListsDTO(githubList, screeNameList);
    }

    //TODO: GET /contributors/contributions
    public GitHubScreenNameListsDTO sortByMostContributions(){
        List<GithubProfileDTO> githubList = new ArrayList<>();
        List<ScreenNameProfileDTO> screeNameList = new ArrayList<>();

        for(Contributor c : CONTRIBUTOR_DAO.findByMostContributions()){
            ProfileDTO x = convertToContributeDTO.convert(c);
            if(x.githubProfile() == null){
                screeNameList.add(new ScreenNameProfileDTO(x.screenName(), x.contributions(), x.resources()));
            }
            if(x.screenName() == null){
                githubList.add(new GithubProfileDTO(x.githubProfile(), x.contributions(), x.resources()));
            }
        }
        return new GitHubScreenNameListsDTO(githubList, screeNameList);
    }


    //TODO: GET /contributors/{id}
    public ProfileDTO getContributorById(ContributorIdDTO contributorIdDTO, boolean isAdmin){
        if(!isAdmin){
            throw new RuntimeException("You must be escalated access rights to make this query");
        }
        Contributor contributor = CONTRIBUTOR_DAO.findById(contributorIdDTO.contributorId());
        if (contributor == null) {
            throw new EntityNotFoundException("No contributor found with id " + contributorIdDTO.contributorId());
        }
        return convertToContributeDTO.convert(contributor);
    }


    //TODO: GET /contributors/{name}
    public ProfileDTO getContributorByName(ContributorNameDTO contributorNameDTO){
        Contributor contributor = CONTRIBUTOR_DAO.findByName(contributorNameDTO.name());
        if (contributor == null) {
            throw new EntityNotFoundException("No contributor found with name " + contributorNameDTO.name());
        }
        return convertToContributeDTO.convert(contributor);
    }


    //TODO: PUT /contributors/{name}
    public ProfileDTO updateContributor(ContributorNameDTO contributorNameDTO, ProfileDTO profileDTO, Long authenticatedContributorId, boolean isAdmin){
        if (contributorNameDTO == null || contributorNameDTO.name() == null || contributorNameDTO.name().isBlank()) {
            throw new IllegalArgumentException("Contributor name is required for update");
        }
        if (profileDTO == null) {
            throw new IllegalArgumentException("Payload is required for update");
        }

        //true if names aren't null AND they aren't blank
        boolean hasGithub = profileDTO.githubProfile() != null && !profileDTO.githubProfile().isBlank();
        boolean hasScreenName = profileDTO.screenName() != null && !profileDTO.screenName().isBlank();

        //if both hasGithub and hasScreenName are false this block will run
        if (!hasGithub && !hasScreenName) {
            throw new IllegalArgumentException("You must provide either a GitHub profile or a screen name");
        }


        Contributor contributor = CONTRIBUTOR_DAO.findByName(contributorNameDTO.name());
        if (contributor == null) {
            throw new EntityNotFoundException("No contributor found with name " + contributorNameDTO.name());
        }

        if (!isAdmin && authenticatedContributorId == null) {
            throw new RuntimeException("You must have a contributor profile to update");
        }

        //if isAdmin is false and id is not authenticated, this block runs
        if (!isAdmin && !contributor.getId().equals(authenticatedContributorId)) {
            throw new RuntimeException("You are not allowed to update this contributor");
        }

        contributor.setGithubProfile(hasGithub ? profileDTO.githubProfile().trim() : null);
        contributor.setScreenName(hasScreenName ? profileDTO.screenName().trim() : null);

        Contributor updatedC = CONTRIBUTOR_DAO.update(contributor);
        return convertToContributeDTO.convert(updatedC);
    }



    //TODO: DELETE /contributors/{name}
    public boolean deleteContributor(ContributorNameDTO contributorNameDTO, Long authenticatedContributorId, boolean isAdmin){
        if (contributorNameDTO == null || contributorNameDTO.name() == null || contributorNameDTO.name().isBlank()) {
            throw new IllegalArgumentException("Contributor name is required for deletion");
        }
        Contributor contributor = CONTRIBUTOR_DAO.findByName(contributorNameDTO.name());

        if (contributor == null) {
            throw new EntityNotFoundException("No contributor found with name " + contributorNameDTO.name() + " for deletion");
        }

        if (!isAdmin && authenticatedContributorId == null) {
            throw new RuntimeException("You must have a contributor profile to delete");
        }

        //if isAdmin is false and id is not authenticated, this block runs
        if (!isAdmin && !contributor.getId().equals(authenticatedContributorId)) {
            throw new RuntimeException("You are not allowed to delete this contributor");
        }

        return CONTRIBUTOR_DAO.delete(contributor.getId());
    }


}
