package app.dtos.resourcedtos;

import app.dtos.contributordtos.SimpleContributorDTO;
import app.enums.FormatCategory;
import app.enums.SubCategory;

import java.time.LocalDateTime;

public record SimpleResourceDTO(
        Integer learningId,
        String learningResourceLink,
        String title,
        FormatCategory formatCategory,
        SubCategory subCategory,
        String description,
        SimpleContributorDTO simpleContributorDTO,
        LocalDateTime createdAt,
        LocalDateTime modifiedAt
) {
}
