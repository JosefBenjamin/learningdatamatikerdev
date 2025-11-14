package app.dtos.categorydtos;

import java.util.List;

public record BothCategoriesDTO(
        List<FormatCatDTO> formatCatDTOList,
        List<SubCatDTO> subCatDTOList
) {
}
