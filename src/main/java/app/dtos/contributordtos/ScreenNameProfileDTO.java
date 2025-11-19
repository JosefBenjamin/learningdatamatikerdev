package app.dtos.contributordtos;

import app.dtos.resourcedtos.SimpleResourceDTO;

import java.util.List;

public record ScreenNameProfileDTO(
        String screenName,
        Integer contributions,
        List<SimpleResourceDTO> resources
) {
}
