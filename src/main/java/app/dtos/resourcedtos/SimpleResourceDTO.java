package app.dtos.resourcedtos;

import app.enums.FormatCategory;
import app.enums.SubCategory;

public record SimpleResourceDTO(
        Integer learningId,
        String learningResourceLink,
        FormatCategory formatCategory,
        SubCategory subCategory,
        String description
) {
}
