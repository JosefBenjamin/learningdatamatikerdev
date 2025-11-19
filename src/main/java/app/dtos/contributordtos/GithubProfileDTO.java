package app.dtos.contributordtos;

import app.dtos.resourcedtos.SimpleResourceDTO;

import java.util.List;

public record GithubProfileDTO(
        String githubProfile,
        Integer contributions,
        List<SimpleResourceDTO> resources
) {
}
