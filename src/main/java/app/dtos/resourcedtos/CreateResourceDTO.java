package app.dtos.resourcedtos;

import app.dtos.contributordtos.SimpleContributorDTO;
import app.enums.FormatCategory;
import app.enums.SubCategory;

public record CreateResourceDTO(
        String learningResourceLink,
        String title,
        FormatCategory formatCategory,
        SubCategory subCategory,
        String description,
        SimpleContributorDTO simpleContributorDTO
) {
}
