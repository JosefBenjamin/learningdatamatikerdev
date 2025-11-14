package app.security.dtos;

public record SignupRequestDTO(
        String username,
        String password,
        String githubProfile,
        String screenName
) {
}
