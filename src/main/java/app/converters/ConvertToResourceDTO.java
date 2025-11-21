package app.converters;

import app.dtos.contributordtos.SimpleContributorDTO;
import app.dtos.resourcedtos.SimpleResourceDTO;
import app.entities.Contributor;
import app.entities.Resource;

public class ConvertToResourceDTO implements IConverter<Resource, SimpleResourceDTO> {

    @Override
    public SimpleResourceDTO convert(Resource source) {
        if (source == null) {
            return null;
        }

        Contributor contributor = source.getContributor();
        SimpleContributorDTO simpleContributorDTO = contributor == null ? null : new SimpleContributorDTO(
                source.getContributor().getId(),
                source.getContributor().getGithubProfile(),
                source.getContributor().getScreenName(),
                source.getContributor().getContributions());

        return new SimpleResourceDTO(
                source.getLearningId(),
                source.getLearningResourceLink(),
                source.getTitle(),
                source.getFormatCategory(),
                source.getSubCategory(),
                source.getDescription(),
                simpleContributorDTO
        );
    }

    /**
     *         Integer learningId,
     *         String learningResourceLink,
     *         String title,
     *         FormatCategory formatCategory,
     *         SubCategory subCategory,
     *         String description,
     *         SimpleContributorDTO simpleContributorDTO
     */
}
