package app.security.exceptions;

//Unchecked exception (runtime)
/**
 * Purpose: To handle validation exceptions in the API
 * Author: Thomas Hartmann
 */
public class ValidationException extends RuntimeException {
    public ValidationException(String message) {
        super(message);
    }
}
