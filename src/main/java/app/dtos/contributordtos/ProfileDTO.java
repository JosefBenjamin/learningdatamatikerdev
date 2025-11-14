package app.dtos.contributordtos;

import app.dtos.resourcedtos.SimpleResourceDTO;

import java.util.List;

public record ProfileDTO(
        String githubProfile,
        String screenName,
        Integer contributions,
        List<SimpleResourceDTO> resources
) {
}
