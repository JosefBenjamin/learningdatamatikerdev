package app.daos;

import app.entities.Contributor;
import app.exceptions.ApiException;
import app.exceptions.DatabaseException;
import jakarta.persistence.*;

import java.util.List;

public class ContributorDAO {
    private static EntityManagerFactory emf;
    private static ContributorDAO instance;
    private static final String ENTITY_NAME = Contributor.class.getSimpleName();


    public static ContributorDAO getInstance(EntityManagerFactory emf){
        if(null == instance){
            instance = new ContributorDAO();
            ContributorDAO.emf = emf;
        }
        return instance;
    }

    /**
     * All persistence is handled by the SecurityDAO.
     * The connection between User <-> Contributor is handled in the service layer
     */



    //READ
    public Contributor findById(Long id) {
        if(id == null){
            throw new IllegalArgumentException("You must insert a Long id for the " + ENTITY_NAME + " entity");
        }
        try(EntityManager em = emf.createEntityManager()){
            return em.find(Contributor.class, id);
        }
    }

    public List<Contributor> retrieveAll() {
        try(EntityManager em = emf.createEntityManager()){
            TypedQuery<Contributor> entities = em.createQuery(
                    "SELECT c " +
                            "FROM Contributor c " +
                            "ORDER BY COALESCE(c.githubProfile, c.screenName) " +
                            "DESC ",
                    Contributor.class);

            return entities.getResultList();
        }
    }


    public List<Contributor> findByMostContributions(){
        try(EntityManager em = emf.createEntityManager()){
            TypedQuery<Contributor> entities = em.createQuery(
                    "SELECT c " +
                            "FROM Contributor c " +
                            "ORDER BY c.contributions DESC ", Contributor.class);

            //Must use List instead of Set because list preserves order integrity
            return entities.getResultList();
        }
    }

    public Contributor findByName(String name){
        if(name == null || name.isBlank()){
            throw new IllegalArgumentException("You must insert a valid GitHub or screen name for the " + ENTITY_NAME + " entity");
        }
        String normalized = name.trim().toLowerCase();
        try(EntityManager em = emf.createEntityManager()){
            try {
                return em.createQuery(
                        "SELECT c " +
                                "FROM Contributor c " +
                                "WHERE LOWER(c.githubProfile) = :value " +
                                "OR LOWER(c.screenName) = :value", Contributor.class)
                        .setParameter("value", normalized)
                        .getSingleResult();
            } catch (NoResultException e) {
                throw new EntityNotFoundException("Could not find a user with that github or screen name in the " + ENTITY_NAME + " entity");
            }
        }
    }

    public Contributor findByUsername(String username){
        if(username == null || username.isBlank()){
            throw new IllegalArgumentException("You must insert a valid username for the " + ENTITY_NAME + " entity");
        }
        String normalized = username.trim().toLowerCase();
        try(EntityManager em = emf.createEntityManager()){
            try {
                return em.createQuery(
                                "SELECT c " +
                                        "FROM Contributor c " +
                                        "WHERE LOWER(c.user.username) = :value ", Contributor.class)
                        .setParameter("value", normalized)
                        .getSingleResult();
            } catch (NoResultException e) {
                throw new EntityNotFoundException("Could not find a user with that username in the " + ENTITY_NAME + " entity");
            }
        }
    }


    //UPDATE
    public Contributor update(Contributor entity) {
        if(entity == null){
            throw new EntityNotFoundException("The " + ENTITY_NAME +  " entity you want to update cannot be null");
        }
        try(EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            try {
                em.merge(entity);
                em.getTransaction().commit();
                return entity;
            } catch (RuntimeException e) {
                if(em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }
                throw new DatabaseException(500, "Could not update the " + ENTITY_NAME + " entity");
            }
        }
    }


    //DELETE
    public boolean delete(Long id) {
        if(id == null){
            throw new IllegalArgumentException("The id for the " + ENTITY_NAME + " entity you want to delete, cannot be null");
        }
        try(EntityManager em = emf.createEntityManager()){
            em.getTransaction().begin();
            Contributor foundEntity = em.find(Contributor.class, id);
            if(foundEntity == null){
                throw new EntityNotFoundException("Could not find a " + ENTITY_NAME + " entity to delete");
            }
            try{
                em.remove(foundEntity);
                em.getTransaction().commit();
                return true;
            } catch(RuntimeException e){
                if(em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                throw new ApiException(500, "Failed to delete the " + ENTITY_NAME + " entity");
            }
        }
    }



}
