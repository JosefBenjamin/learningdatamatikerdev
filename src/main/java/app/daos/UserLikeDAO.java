package app.daos;

import app.entities.Resource;
import app.entities.UserLike;
import app.exceptions.DatabaseException;
import app.security.entities.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

public class UserLikeDAO {
    private static EntityManagerFactory emf;
    private static UserLikeDAO instance;

    public static UserLikeDAO getInstance(EntityManagerFactory emf) {
        if (instance == null) {
            instance = new UserLikeDAO();
            UserLikeDAO.emf = emf;
        }
        return instance;
    }

    public UserLike addLike(String username, Long resourceId) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            try {
                User user = em.find(User.class, username);
                Resource resource = em.find(Resource.class, resourceId);

                if (user == null) {
                    throw new IllegalArgumentException("User not found");
                }
                if (resource == null) {
                    throw new IllegalArgumentException("Resource not found");
                }

                // Check if already liked
                Long existingCount = em.createQuery(
                                "SELECT COUNT(ul) FROM UserLike ul WHERE ul.user.username = :username AND ul.resource.id = :resourceId",
                                Long.class)
                        .setParameter("username", username)
                        .setParameter("resourceId", resourceId)
                        .getSingleResult();

                if (existingCount > 0) {
                    throw new IllegalStateException("User already liked this resource");
                }

                UserLike like = new UserLike(user, resource);
                em.persist(like);
                em.getTransaction().commit();
                return like;
            } catch (RuntimeException e) {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                throw e;
            }
        }
    }

    public boolean removeLike(String username, Long resourceId) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            try {
                int deleted = em.createQuery(
                                "DELETE FROM UserLike ul WHERE ul.user.username = :username AND ul.resource.id = :resourceId")
                        .setParameter("username", username)
                        .setParameter("resourceId", resourceId)
                        .executeUpdate();

                em.getTransaction().commit();
                return deleted > 0;
            } catch (RuntimeException e) {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                throw new DatabaseException(500, "Could not remove like");
            }
        }
    }

    public boolean userLikesResource(String username, Long resourceId) {
        try (EntityManager em = emf.createEntityManager()) {
            Long count = em.createQuery(
                            "SELECT COUNT(ul) FROM UserLike ul WHERE ul.user.username = :username AND ul.resource.id = :resourceId",
                            Long.class)
                    .setParameter("username", username)
                    .setParameter("resourceId", resourceId)
                    .getSingleResult();
            return count > 0;
        }
    }

    public int getLikeCount(Long resourceId) {
        try (EntityManager em = emf.createEntityManager()) {
            Long count = em.createQuery(
                            "SELECT COUNT(ul) FROM UserLike ul WHERE ul.resource.id = :resourceId",
                            Long.class)
                    .setParameter("resourceId", resourceId)
                    .getSingleResult();
            return count.intValue();
        }
    }
}
