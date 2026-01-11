package app.converters;

import app.dtos.contributordtos.SimpleContributorDTO;
import app.dtos.resourcedtos.SimpleResourceDTO;
import app.entities.Contributor;
import app.entities.Resource;

import java.util.List;

public class ConvertToResourceDTO implements IConverter<Resource, SimpleResourceDTO> {

    @Override
    public SimpleResourceDTO convert(Resource source) {
        return convert(source, 0, null);
    }

    public SimpleResourceDTO convert(Resource source, int likeCount, Boolean isLikedByCurrentUser) {
        if (source == null) {
            return null;
        }

        Contributor contributor = source.getContributor();

        SimpleContributorDTO simpleContributorDTO = contributor == null ? null : new SimpleContributorDTO(
                source.getContributor().getId(),
                source.getContributor().getGithubProfile(),
                source.getContributor().getScreenName(),
                source.getContributor().getContributions()
        );

        return new SimpleResourceDTO(
                source.getLearningId(),
                source.getLearningResourceLink(),
                source.getTitle(),
                source.getFormatCategory(),
                source.getSubCategory(),
                source.getDescription(),
                simpleContributorDTO,
                source.getCreatedAt(),
                source.getModifiedAt(),
                likeCount,
                isLikedByCurrentUser
        );
    }

    @Override
    public List<SimpleResourceDTO> convertList(List<Resource> sources) {
        if (sources == null) {
            return null;
        }
        return sources.stream()
                .map(this::convert)
                .toList();
    }
}
