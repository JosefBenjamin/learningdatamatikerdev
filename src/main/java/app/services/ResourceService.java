package app.services;

import app.configs.HibernateConfig;
import app.converters.ConvertToResourceDTO;
import app.converters.ResourceToResourceDTO;
import app.daos.ContributorDAO;
import app.daos.ResourceDAO;
import app.daos.UserLikeDAO;
import app.dtos.categorydtos.SingleFormatCatDTO;
import app.dtos.categorydtos.SingleSubCategoryDTO;
import app.dtos.PageDTO;
import app.dtos.contributordtos.ContributorNameDTO;
import app.dtos.contributordtos.SimpleContributorDTO;
import app.dtos.resourcedtos.*;
import app.entities.Contributor;
import app.entities.Resource;
import app.exceptions.ApiException;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityNotFoundException;

import java.util.ArrayList;
import java.util.List;

public class ResourceService {
    private final EntityManagerFactory EMF = HibernateConfig.getEntityManagerFactory();
    private final ContributorDAO CONTRIBUTOR_DAO = ContributorDAO.getInstance(EMF);
    private final ResourceDAO RESOURCE_DAO = ResourceDAO.getInstance(EMF);
    private final UserLikeDAO USER_LIKE_DAO = UserLikeDAO.getInstance(EMF);
    private final ConvertToResourceDTO convertToResourceDTO = new ConvertToResourceDTO();
    private final ResourceToResourceDTO resourceToResourceDTO = new ResourceToResourceDTO();


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

        contributor.setContributions(contributor.getContributions() + 1);

        Resource resource = Resource.builder()
                .learningResourceLink(simpleResourceDTO.learningResourceLink().trim())
                .title(simpleResourceDTO.title().trim())
                .formatCategory(simpleResourceDTO.formatCategory())
                .subCategory(simpleResourceDTO.subCategory())
                .description(simpleResourceDTO.description().trim())
                .contributor(contributor)
                .build();

        Resource persisted = RESOURCE_DAO.persist(resource);
        CONTRIBUTOR_DAO.update(contributor);

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
                contributorDTO,
                persisted.getCreatedAt(),
                persisted.getModifiedAt(),
                0,
                null
                );
    }


    //READ

    //TODO: GET resources/{id}
    public SimpleResourceDTO findResourceById(ResourceIdDTO resourceIdDTO){
        if(resourceIdDTO == null){
            throw new IllegalArgumentException("You must enter a valid resource id");
        }

        Resource resource = RESOURCE_DAO.findById(resourceIdDTO.id());
        if(resource == null){
            throw new EntityNotFoundException("Could not find a resource with that id");
        }

        return convertToResourceDTO.convert(resource);
    }

    //TODO: GET resources/{learning_id}
    public SimpleResourceDTO findResourceById(LearningIdDTO learningIdDTO){
        if(learningIdDTO == null){
            throw new IllegalArgumentException("You must enter a valid learning id");
        }

        Resource resource = RESOURCE_DAO.findByLearningId(learningIdDTO.learningId());
        if(resource == null){
            throw new EntityNotFoundException("Could not find a resource with that id");
        }
        return convertToResourceDTO.convert(resource);
    }

    //Getting all resources you must be logged in
    //TODO: GET resources/  <-- retrieve all sort by format category
    public List<SimpleResourceDTO> getAllResources(){
        return new ArrayList<>(convertToResourceDTO.convertList(RESOURCE_DAO.retrieveAll()));
    }

    //TODO: GET resources?page=0&limit=20
    public PageDTO<SimpleResourceDTO> getAllResourcesPaginated(int page, int limit){
        List<Resource> resources = RESOURCE_DAO.retrieveAllPaginated(page, limit);
        long totalElements = RESOURCE_DAO.countAll();
        List<SimpleResourceDTO> content = convertToResourceDTO.convertList(resources);
        return PageDTO.of(content, page, limit, totalElements);
    }

    //TODO: GET resources/newest
    public List<SimpleResourceDTO> getNewestResources() {
        List<Resource> resources = RESOURCE_DAO.retrieveSortAllNewest();
        return convertToResourceDTO.convertList(resources);
    }

    //TODO: GET resources/updated
    public List<SimpleResourceDTO> getRecentlyUpdatedResources() {
        List<Resource> resources = RESOURCE_DAO.findRecentlyUpdated();
        return convertToResourceDTO.convertList(resources);
    }

    //Getting all resources by format cat you must be logged in
    //TODO: GET resources/{format_category}
    public List<SimpleResourceDTO> getAllResourcesInFormatCat(SingleFormatCatDTO singleFormatCatDTO, Long authenticatedContributorId, boolean isAdmin){
        if (!isAdmin && authenticatedContributorId == null) {
            throw new RuntimeException("You must be logged in to request all resources in a category");
        }

        if(singleFormatCatDTO == null || singleFormatCatDTO.formatCategory() == null){
            return getAllResources();
        }

        return new ArrayList<>(convertToResourceDTO.convertList(RESOURCE_DAO.findByFormatCat(singleFormatCatDTO.formatCategory())));
    }

    //Getting all resources by sub cat you must be logged in
    //TODO: GET resources/{sub_category}
    public List<SimpleResourceDTO> getAllResourcesInSubCat(SingleSubCategoryDTO singleSubCategoryDTO, Long authenticatedContributorId, boolean isAdmin){
        if (!isAdmin && authenticatedContributorId == null) {
            throw new RuntimeException("You must be logged in to request all resources in a category");
        }

        if(singleSubCategoryDTO == null || singleSubCategoryDTO.subCategory() == null){
            return getAllResources();
        }

        return new ArrayList<>(convertToResourceDTO.convertList(RESOURCE_DAO.findBySubCat(singleSubCategoryDTO.subCategory())));
    }


    //TODO: GET resources/{title}
    public SimpleResourceDTO findByTitle(ResourceTitleDTO resourceTitleDTO){
        if(resourceTitleDTO == null){
            throw new IllegalArgumentException("You must enter a valid title");
        }

        Resource resource = RESOURCE_DAO.findByTitle(resourceTitleDTO.title());
        if(resource == null){
            throw new EntityNotFoundException("Could not find a resource with that title");
        }
        return convertToResourceDTO.convert(resource);
    }

    //TODO: GET resources/{contributor}
    public List<SimpleResourceDTO> findByContributor(ContributorNameDTO contributorNameDTO){
        if(contributorNameDTO == null){
            throw new IllegalArgumentException("You must enter a valid GitHub or screen name");
        }

        Contributor contributor = CONTRIBUTOR_DAO.findByName(contributorNameDTO.name());
        if(contributor == null){
            throw new EntityNotFoundException("Could not find a contributor with Github or scree name");
        }

        List<Resource> resourceList = new ArrayList<>(RESOURCE_DAO.findByContributor(contributor.getId()));
        if(resourceList == null){
            throw  new EntityNotFoundException("Could not find a list of resources from that contributor");
        }

        return convertToResourceDTO.convertList(resourceList);
    }

    //TODO: GET resources/{keyword}
    public List<SimpleResourceDTO> findByKeyword(ResourceKeywordDTO resourceKeywordDTO){
        if(resourceKeywordDTO == null){
            throw new IllegalArgumentException("You must enter a valid keyword");
        }

        List<Resource> resourceList = new ArrayList<>(RESOURCE_DAO.findByKeyword(resourceKeywordDTO.keyword()));
        if(resourceList == null){
            throw new EntityNotFoundException("Could not find any resources matching that keyword");
        }
        return convertToResourceDTO.convertList(resourceList);
    }


    //UPDATE

    //TODO: PUT resources/{learning_id}
    public SimpleResourceDTO updateResource(SimpleResourceDTO simpleResourceDTO, boolean isAdmin, Long authenticatedContributorId){
        if(simpleResourceDTO == null || simpleResourceDTO.learningId() == null){
            throw new IllegalArgumentException("Learning id must be provided in order to update learning resource");
        }

        if(!isAdmin && authenticatedContributorId == null){
            throw new ApiException(403, "You must have a contributor profile to create resources");
        }

        Resource resource = RESOURCE_DAO.findByLearningId(simpleResourceDTO.learningId());
        if(resource == null){
            throw new ApiException(404, "Couldn't find the learning resource in our database");
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

        if (!isAdmin && !resource.getContributor().getId().equals(authenticatedContributorId)) {
            throw new ApiException(403, "You are not allowed to update this resource");
        }

        Resource updatedResource = RESOURCE_DAO.update(resource);

        SimpleContributorDTO simpleContributorDTO = new SimpleContributorDTO(updatedResource.getContributor().getId(), updatedResource.getContributor().getGithubProfile(),
                updatedResource.getContributor().getScreenName(), updatedResource.getContributor().getContributions());

        int likeCount = USER_LIKE_DAO.getLikeCount(updatedResource.getId());
        return new SimpleResourceDTO(updatedResource.getLearningId(), updatedResource.getLearningResourceLink(),
                updatedResource.getTitle(), updatedResource.getFormatCategory(), updatedResource.getSubCategory(),
                updatedResource.getDescription(), simpleContributorDTO,
                updatedResource.getCreatedAt(), updatedResource.getModifiedAt(),
                likeCount, null);
    }


    //DELETE

    //TODO: DELETE resources/{learning_id}
    public boolean deleteResource(LearningIdDTO learningIdDTO, boolean isAdmin, Long authenticatedContributorId){
        if(learningIdDTO.learningId() == null){
            throw new IllegalArgumentException("You must enter a valid learning id");
        }

        if (!isAdmin && authenticatedContributorId == null) {
            throw new ApiException(403, "You must have a contributor profile to delete a resource");
        }

        Resource resource = RESOURCE_DAO.findByLearningId(learningIdDTO.learningId());
        if(resource == null || resource.getLearningId() == null){
            throw new IllegalArgumentException("There is no resource with this learning id");
        }

        //if isAdmin is false and id is not authenticated, this block runs
        if (!isAdmin && !resource.getContributor().getId().equals(authenticatedContributorId)) {
            throw new ApiException(403, "You are not allowed to delete this resource");
        }


        resource.getContributor().setContributions(resource.getContributor().getContributions() - 1);

        if(resource.getContributor().getContributions() < 0){
            throw new ApiException(400, "You cannot have negative contributions, something went wrong!");
        }

        CONTRIBUTOR_DAO.update(resource.getContributor());
        return RESOURCE_DAO.delete(resource.getId());
    }


    //LIKES

    public void likeResource(Long resourceId, String username) {
        if (resourceId == null) {
            throw new IllegalArgumentException("Resource ID is required");
        }
        if (username == null || username.isBlank()) {
            throw new ApiException(403, "You must be logged in to like a resource");
        }
        USER_LIKE_DAO.addLike(username, resourceId);
    }

    public boolean unlikeResource(Long resourceId, String username) {
        if (resourceId == null) {
            throw new IllegalArgumentException("Resource ID is required");
        }
        if (username == null || username.isBlank()) {
            throw new ApiException(403, "You must be logged in to unlike a resource");
        }
        return USER_LIKE_DAO.removeLike(username, resourceId);
    }

    public SimpleResourceDTO findResourceByIdWithLikes(Long resourceId, String username) {
        Resource resource = RESOURCE_DAO.findById(resourceId);
        if (resource == null) {
            throw new EntityNotFoundException("Could not find a resource with that id");
        }

        int likeCount = USER_LIKE_DAO.getLikeCount(resourceId);
        Boolean isLikedByCurrentUser = username != null ? USER_LIKE_DAO.userLikesResource(username, resourceId) : null;

        return convertToResourceDTO.convert(resource, likeCount, isLikedByCurrentUser);
    }
}
