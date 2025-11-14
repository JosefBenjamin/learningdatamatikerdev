package app.dtos.categorydtos;

import app.enums.SubCategory;

import java.util.List;

public record SubCatDTO(
        List<SubCategory> subCategoryList
) {
}
