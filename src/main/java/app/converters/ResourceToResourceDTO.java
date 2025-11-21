package app.converters;

import app.dtos.contributordtos.ContributorNameDTO;
import app.dtos.resourcedtos.ResourceDTO;
import app.entities.Contributor;
import app.entities.Resource;

public class ResourceToResourceDTO implements IConverter<Resource,
        ResourceDTO> {

    @Override
    public ResourceDTO convert(Resource source) {
        if (source == null) {
            return null;
        }
        Contributor contributor = source.getContributor();
        ContributorNameDTO contributorNameDTO =
                contributor == null || contributor.getGithubProfile() == null ?
                        null : new ContributorNameDTO(contributor.getGithubProfile());

        return new ResourceDTO(
                source.getLearningId(),
                source.getLearningResourceLink(),
                source.getTitle(),
                source.getFormatCategory(),
                source.getSubCategory(),
                source.getDescription(),
                contributorNameDTO
        );
    }
}
