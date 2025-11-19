package app.converters;

import app.dtos.resourcedtos.SimpleResourceDTO;
import app.entities.Resource;

public class ConvertToResourceEntity implements IConverter<SimpleResourceDTO, Resource> {

    @Override
    public Resource convert(SimpleResourceDTO source) {
        if (source == null) {
            return null;
        }
        return Resource.builder()
                .learningId(source.learningId())
                .learningResourceLink(source.learningResourceLink())
                .title(source.title())
                .formatCategory(source.formatCategory())
                .subCategory(source.subCategory())
                .description(source.description())
                .build();
    }
}
