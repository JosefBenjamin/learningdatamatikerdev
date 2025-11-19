package app.services;

import app.configs.HibernateConfig;
import app.converters.ConvertToContributorDTO;
import app.daos.ContributorDAO;
import app.daos.ResourceDAO;
import app.dtos.contributordtos.SimpleContributorDTO;
import app.dtos.resourcedtos.LearningIdDTO;
import app.dtos.resourcedtos.SimpleResourceDTO;
import app.entities.Contributor;
import app.entities.Resource;
import app.exceptions.ApiException;
import app.security.daos.SecurityDAO;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityNotFoundException;

public class ResourceService {
    private final EntityManagerFactory EMF = HibernateConfig.getEntityManagerFactory();
    private final ContributorDAO CONTRIBUTOR_DAO = ContributorDAO.getInstance(EMF);
    private final ResourceDAO RESOURCE_DAO = ResourceDAO.getInstance(EMF);
    private final SecurityDAO SECURITY_DAO = SecurityDAO.getInstance(EMF);
    private final ConvertToContributorDTO convertToContributeDTO = new ConvertToContributorDTO();


    public ResourceService(){

    }

    //CREATE

    //TODO: POST resources/
    public SimpleResourceDTO createResource(SimpleResourceDTO simpleResourceDTO, Long authenticatedContributorId) {
        if (simpleResourceDTO == null) {
            throw new IllegalArgumentException("Payload required to create resource");
        }
        if (simpleResourceDTO.learningResourceLink() == null || simpleResourceDTO.learningResourceLink().isBlank()) {
            throw new IllegalArgumentException("learningResourceLink is required");
        }
        if (simpleResourceDTO.title() == null || simpleResourceDTO.title().isBlank()) {
            throw new IllegalArgumentException("title is required");
        }
        if (simpleResourceDTO.formatCategory() == null) {
            throw new IllegalArgumentException("formatCategory is required");
        }
        if (simpleResourceDTO.subCategory() == null) {
            throw new IllegalArgumentException("subCategory is required");
        }
        if (simpleResourceDTO.description() == null || simpleResourceDTO.description().isBlank()) {
            throw new IllegalArgumentException("description is required");
        }

        if (authenticatedContributorId == null) {
            throw new ApiException(403, "You must have a contributor profile to create resources");
        }

        Contributor contributor = CONTRIBUTOR_DAO.findById(authenticatedContributorId);
        if (contributor == null) {
            throw new EntityNotFoundException("No contributor found with id " + authenticatedContributorId);
        }

        Resource resource = Resource.builder()
                .learningResourceLink(simpleResourceDTO.learningResourceLink().trim())
                .title(simpleResourceDTO.title().trim())
                .formatCategory(simpleResourceDTO.formatCategory())
                .subCategory(simpleResourceDTO.subCategory())
                .description(simpleResourceDTO.description().trim())
                .contributor(contributor)
                .build();

        Resource persisted = RESOURCE_DAO.persist(resource);
        SimpleContributorDTO contributorDTO = new SimpleContributorDTO(
                contributor.getId(),
                contributor.getGithubProfile(),
                contributor.getScreenName(),
                contributor.getContributions());

        return new SimpleResourceDTO(
                persisted.getLearningId(),
                persisted.getLearningResourceLink(),
                persisted.getTitle(),
                persisted.getFormatCategory(),
                persisted.getSubCategory(),
                persisted.getDescription(),
                contributorDTO);
    }


    //READ



    //UPDATE

    //TODO: PUT resources/{learning_id}
    public SimpleResourceDTO updateResource(SimpleResourceDTO simpleResourceDTO, Long authenticatedContributorId){
        if(simpleResourceDTO == null || simpleResourceDTO.learningId() == null){
            throw new IllegalArgumentException("Learning id must be provided in order to update learning resource");
        }

        if(authenticatedContributorId == null){
            throw new ApiException(403, "You must have a contributor profile to create resources");
        }

        Resource resource = RESOURCE_DAO.findByLearningId(simpleResourceDTO.learningId());
        if(RESOURCE_DAO.findById(resource.getId()) == null){
            throw new ApiException("Couldn't find the learning resource in our database");
        }

        //Null checks
        if(simpleResourceDTO.learningResourceLink() != null) {
            resource.setLearningResourceLink(simpleResourceDTO.learningResourceLink());
        }

        if(simpleResourceDTO.title() != null) {
            resource.setTitle(simpleResourceDTO.title());
        }

        if(simpleResourceDTO.formatCategory() != null) {
            resource.setFormatCategory(simpleResourceDTO.formatCategory());
        }

        if(simpleResourceDTO.subCategory() != null) {
            resource.setSubCategory(simpleResourceDTO.subCategory());
        }

        if(simpleResourceDTO.description() != null) {
            resource.setDescription(simpleResourceDTO.description());
        }

        if (!resource.getContributor().getId().equals(authenticatedContributorId)) {
            throw new ApiException(403, "You are not allowed to update this resource");
        }

        Resource updatedResource = RESOURCE_DAO.update(resource);

        SimpleContributorDTO simpleContributorDTO = new SimpleContributorDTO(updatedResource.getContributor().getId(), updatedResource.getContributor().getGithubProfile(),
                updatedResource.getContributor().getScreenName(), updatedResource.getContributor().getContributions());

        return new SimpleResourceDTO(updatedResource.getLearningId(), updatedResource.getLearningResourceLink(),
                updatedResource.getTitle(), updatedResource.getFormatCategory(), updatedResource.getSubCategory(),
                updatedResource.getDescription(),simpleContributorDTO);
    }


    //DELETE

    //TODO: DELETE resources/{learning_id}
    public boolean deleteResource(LearningIdDTO learningIdDTO, Long authenticatedContributorId){
        if(learningIdDTO.learningId() == null){
            throw new IllegalArgumentException("You must enter a valid ID");
        }

        if (authenticatedContributorId == null) {
            throw new ApiException(403, "You must have a contributor profile to create resources");
        }

        Resource resource = RESOURCE_DAO.findByLearningId(learningIdDTO.learningId());
        if(resource == null || resource.getLearningId() == null){
            throw new IllegalArgumentException("Learning id must be provided in order to update learning resource");
        }

        if (!resource.getContributor().getId().equals(authenticatedContributorId)) {
            throw new ApiException(403, "You are not allowed to delete this resource");
        }

        return RESOURCE_DAO.delete(resource.getId());
    }


}
