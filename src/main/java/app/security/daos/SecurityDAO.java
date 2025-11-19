package app.security.daos;


import app.entities.Contributor;
import app.exceptions.ApiException;
import app.security.dtos.SignupRequestDTO;
import app.security.entities.Role;
import app.security.entities.User;
import app.security.exceptions.ValidationException;
import dk.bugelhartmann.UserDTO;
import jakarta.persistence.*;

import java.util.stream.Collectors;

/**
 * Purpose: To handle security in the API
 * Author: Thomas Hartmann
 */
public class SecurityDAO implements ISecurityDAO {

    private static SecurityDAO instance;
    private static EntityManagerFactory emf;
    // private static PasswordHasher passwordHasher = new PasswordHasher();


    private SecurityDAO() {

    }

    public static SecurityDAO getInstance(EntityManagerFactory emf){
        if(instance == null){
            instance = new SecurityDAO();
            SecurityDAO.emf = emf;
        }
        return instance;
    }

    private EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    @Override
    public UserDTO getVerifiedUser(String username, String password) throws ValidationException {
        try (EntityManager em = getEntityManager()) {
            User user = em.find(User.class, username);
            if (user == null) {
                throw new EntityNotFoundException("No user found with username: " + username); //RuntimeException
            }
            user.getRoles().size(); // force roles to be fetched from db

            if (!user.verifyPass(password)) {
                throw new ValidationException("Wrong password");
            }

            return new UserDTO(user.getUsername(), user.getRoles().stream().map(r -> r.getRoleName()).collect(Collectors.toSet()));
        }
    }

    /**
     * LEGACY method
     *
    @Override
    public User createUser(String username, String password) {
        try (EntityManager em = getEntityManager()) {
            User userEntity = em.find(User.class, username);
            if (userEntity != null)
                throw new EntityExistsException("User with username: " + username + " already exists");
            userEntity = new User(username, password);
            em.getTransaction().begin();
            Role userRole = em.find(Role.class, "user");
            if (userRole == null)
                userRole = new Role("user");
            em.persist(userRole);
            userEntity.addRole(userRole);
            em.persist(userEntity);
            em.getTransaction().commit();
            return userEntity;
        }catch (Exception e){
            e.printStackTrace();
            throw new ApiException(400, e.getMessage());
        }
    }
    */

    public User createUserWithContributor(SignupRequestDTO dto) {
        try (EntityManager em = getEntityManager()) {
            if (em.find(User.class, dto.username()) != null) {
                throw new EntityExistsException("User with username: " + dto.username() + " already exists");
            }

            //either these have a value or are null
            String github = blankToNull(dto.githubProfile());
            String screenName = blankToNull(dto.screenName());

            //if one of these isn't null the try block is executed
            if (github != null || screenName != null) {
                try {
                    em.createQuery(
                                    "SELECT c FROM Contributor c " +
                                            "WHERE (:github IS NOT NULL AND LOWER(c.githubProfile) = LOWER(:github)) " +
                                            "OR (:screen IS NOT NULL AND LOWER(c.screenName) = LOWER(:screen))",
                                    Contributor.class)
                            .setParameter("github", github)
                            .setParameter("screen", screenName)
                            .getSingleResult();

                    throw new EntityExistsException("GitHub handle or screen name already exists");
                } catch (NoResultException ignored) {
                    // No duplicates found, continue
                }
            }

            EntityTransaction tx = em.getTransaction();
            try {
                tx.begin();

                User user = new User(dto.username(), dto.password());

                Contributor contributor = Contributor.builder()
                        .githubProfile(github)
                        .screenName(screenName)
                        .contributions(0)
                        .build();
                contributor.attachUser(user);                     // keeps both sides in sync

                Role userRole = ensureRole(em, "user");      // reuse same role for everyone
                user.addRole(userRole);

                em.persist(user);                                  // persist both sides (contributor owns the FK)
                em.persist(contributor);

                tx.commit();
                return user;
            } catch (RuntimeException e) {
                if (tx.isActive()) {
                    tx.rollback();
                }
                throw new ApiException(500, "Could not create a user");
            }
        }
    }

    //Helper methods for createUserWithContributor
    private String blankToNull(String value) {
        //If value is null or blank return null other return value
        return (value == null || value.isBlank()) ? null : value;
    }

    private Role ensureRole(EntityManager em, String roleName) {
        Role role = em.find(Role.class, roleName);
        if (role == null) {
            role = new Role(roleName);
            em.persist(role);
        }
        return role;
    }


    @Override
    public User addRole(UserDTO userDTO, String newRole) {
        try (EntityManager em = getEntityManager()) {
            User user = em.find(User.class, userDTO.getUsername());
            if (user == null)
                throw new EntityNotFoundException("No user found with username: " + userDTO.getUsername());
            em.getTransaction().begin();
            Role role = em.find(Role.class, newRole);
            if (role == null) {
                role = new Role(newRole);
                em.persist(role);
            }
            user.addRole(role);
            //em.merge(user);
            em.getTransaction().commit();
            return user;
        }
    }
}
