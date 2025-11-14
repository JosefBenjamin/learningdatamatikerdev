package app.dtos.exceptiondtos;

public record ErrorMessageDTO(
        int status,
        String message) {}
