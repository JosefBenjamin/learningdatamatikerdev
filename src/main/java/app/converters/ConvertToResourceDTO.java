package app.converters;

import app.dtos.resourcedtos.SimpleResourceDTO;
import app.entities.Resource;

public class ConvertToResourceDTO implements IConverter<Resource, SimpleResourceDTO> {

    @Override
    public SimpleResourceDTO convert(Resource source) {
        if (source == null) {
            return null;
        }
        return new SimpleResourceDTO(
                source.getLearningId(),
                source.getLearningResourceLink(),
                source.getTitle(),
                source.getFormatCategory(),
                source.getSubCategory(),
                source.getDescription()
        );
    }
}
