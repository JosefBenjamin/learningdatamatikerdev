package app.converters;

import app.dtos.contributordtos.ProfileDTO;
import app.dtos.resourcedtos.SimpleResourceDTO;
import app.entities.Contributor;

import java.util.List;

public class ConvertToContributorDTO implements IConverter<Contributor, ProfileDTO> {

    private final ConvertToResourceDTO resourceConverter = new ConvertToResourceDTO();

    @Override
    public ProfileDTO convert(Contributor source) {
        if (source == null) {
            return null;
        }
        List<SimpleResourceDTO> resourceDTOs = resourceConverter.convertList(source.getResources());
        return new ProfileDTO(
                source.getGithubProfile(),
                source.getScreenName(),
                source.getContributions(),
                resourceDTOs
        );
    }
}
