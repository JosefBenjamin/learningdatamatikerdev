package app.dtos.contributordtos;

public record SimpleContributorDTO(
        Long contributorId,
        String githubProfile,
        String screenName,
        Integer contributions
) {
}
