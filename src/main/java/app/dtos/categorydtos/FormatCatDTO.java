package app.dtos.categorydtos;

import app.enums.FormatCategory;

import java.util.List;

public record FormatCatDTO(
        List<FormatCategory> formatCategoryList
) {
}
