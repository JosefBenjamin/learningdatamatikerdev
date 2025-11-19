package app.dtos.resourcedtos;

import app.enums.FormatCategory;
import app.enums.SubCategory;

public record ResourceDTO(
        Integer learningId,
        String learningResourceLink,
        String title,
        FormatCategory formatCategory,
        SubCategory subCategory,
        String description

) {
}
