package app.converters;

import app.dtos.contributordtos.ProfileDTO;
import app.dtos.resourcedtos.SimpleResourceDTO;
import app.entities.Contributor;
import app.entities.Resource;

import java.util.List;

public class ConvertToContributorEntity implements IConverter<ProfileDTO, Contributor> {

    private final ConvertToResourceEntity resourceConverter = new ConvertToResourceEntity();

    @Override
    public Contributor convert(ProfileDTO source) {
        if (source == null) {
            return null;
        }

        Contributor contributor = Contributor.builder()
                .githubProfile(source.githubProfile())
                .screenName(source.screenName())
                .contributions(source.contributions())
                .build();

        List<SimpleResourceDTO> resourceDTOs = source.resources();
        if (resourceDTOs != null && !resourceDTOs.isEmpty()) {
            List<Resource> resources = resourceConverter.convertList(resourceDTOs);
            resources.stream()
                    .filter(r -> r != null)
                    .forEach(contributor::addResource);
        }

        return contributor;
    }
}
