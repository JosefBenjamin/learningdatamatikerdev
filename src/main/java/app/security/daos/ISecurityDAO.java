package app.security.daos;

import app.security.entities.User;
import dk.bugelhartmann.UserDTO;
import io.javalin.validation.ValidationException;

public interface ISecurityDAO {
    UserDTO getVerifiedUser(String username, String password) throws ValidationException;
    // User createUser(String username, String password); <-- legacy method
    User addRole (UserDTO userDTO, String newRole);

}
