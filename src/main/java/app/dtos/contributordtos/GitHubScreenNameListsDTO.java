package app.dtos.contributordtos;

import java.util.List;

public record GitHubScreenNameListsDTO(
        List<GithubProfileDTO> githubProfileDTOS,
        List<ScreenNameProfileDTO> screenNameProfileDTOS
) {
}
