package app.daos;

import app.entities.Resource;
import app.enums.FormatCategory;
import app.enums.SubCategory;
import app.exceptions.ApiException;
import app.exceptions.DatabaseException;
import jakarta.persistence.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class ResourceDAO implements ICRUD<Resource> {
    private static EntityManagerFactory emf;
    private static ResourceDAO instance;
    private static final String ENTITY_NAME = Resource.class.getSimpleName();


    public static ResourceDAO getInstance(EntityManagerFactory emf){
        if(null == instance){
            instance = new ResourceDAO();
            ResourceDAO.emf = emf;
        }
        return instance;
    }

    //CREATE
    @Override
    public Resource persist(Resource entity) {
        if (entity == null) {
            throw new IllegalArgumentException("The " + ENTITY_NAME + " entity cannot be null");
        }
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            try {
                if (entity.getLearningId() == null) {
                    Number nextVal = (Number) em.createNativeQuery("SELECT nextval('resource_learning_id_seq')").getSingleResult();
                    entity.setLearningId(nextVal.intValue());
                }
                em.persist(entity);
                em.getTransaction().commit();
                return entity;
            } catch (RuntimeException e) {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                throw new DatabaseException(500, "Could not persist " +  ENTITY_NAME + " entity");
            }
        }
    }

    @Override
    public Set<Resource> persistList(List<Resource> entities) {
        if(entities == null || entities.isEmpty()){
            throw new IllegalArgumentException("List of " + ENTITY_NAME +  " cannot be empty or null");
        }
        try(EntityManager em = emf.createEntityManager()){
            em.getTransaction().begin();
            try{
                for(Resource r : entities){
                    if (r.getLearningId() == null) {
                        Number nextVal = (Number) em.createNativeQuery("SELECT nextval('resource_learning_id_seq')").getSingleResult();
                        r.setLearningId(nextVal.intValue());
                    }
                    em.persist(r);
                }
                em.getTransaction().commit();
                return new HashSet<>(entities);
            } catch (RuntimeException e){
                if(em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }
                throw new DatabaseException(500, "Failed to persist the list of " + ENTITY_NAME + " entities");
            }
        }    
    }

    //READ
    @Override
    public Resource findById(Long id) {
        if(id == null){
            throw new IllegalArgumentException("You must insert a Long id for the " + ENTITY_NAME + " entity");
        }
        try(EntityManager em = emf.createEntityManager()){
            return em.find(Resource.class, id);
        }
    }

    public Resource findByLearningId(Integer id){
        if(id == null){
            throw new IllegalArgumentException("You must insert a integer learning id for the " + ENTITY_NAME + " entity");
        }
        try(EntityManager em = emf.createEntityManager()){
            try {
                return em.createQuery("SELECT r " +
                                "FROM Resource r " +
                                "WHERE r.learningId = :learningId", Resource.class)
                        .setParameter("learningId", id)
                        .getSingleResult();

            } catch(RuntimeException e){
                throw new EntityNotFoundException("Could not find the " + ENTITY_NAME + " entity");
            }
        }
    }

    // Get all resources sorted by newest first
    public List<Resource> retrieveSortAllNewest() {
        try(EntityManager em = emf.createEntityManager()){
            return em.createQuery(
                    "SELECT r FROM Resource r ORDER BY r.createdAt DESC",
                    Resource.class)
                    .setMaxResults(100)
                    .getResultList();
        }
    }

    // Get resources modified recently (e.g., "Recently Updated")
    public List<Resource> findRecentlyUpdated() {
        try(EntityManager em = emf.createEntityManager()){
            return em.createQuery(
                            "SELECT r FROM Resource r ORDER BY r.modifiedAt DESC",
                            Resource.class)
                    .setMaxResults(100)
                    .getResultList();
        }
    }

    @Override
    public Set<Resource> retrieveAll() {
        try(EntityManager em = emf.createEntityManager()){
            TypedQuery<Resource> entities = em.createQuery(
            "SELECT r " +
                    "FROM Resource r " +
                    "ORDER BY r.formatCategory DESC ", Resource.class);
            return new HashSet<>(entities.getResultList());
        }
    }

    public List<Resource> retrieveAllPaginated(int page, int limit) {
        try(EntityManager em = emf.createEntityManager()){
            return em.createQuery(
                    "SELECT r FROM Resource r ORDER BY r.createdAt DESC", Resource.class)
                    .setFirstResult(page * limit)
                    .setMaxResults(limit)
                    .getResultList();
        }
    }

    public long countAll() {
        try(EntityManager em = emf.createEntityManager()){
            return em.createQuery("SELECT COUNT(r) FROM Resource r", Long.class)
                    .getSingleResult();
        }
    }


    public List<Resource> findByFormatCat(FormatCategory formatCategory) {
        if (formatCategory == null) {
            throw new IllegalArgumentException("FormatCategory cannot be null when searching for " + ENTITY_NAME);
        }
        try(EntityManager em = emf.createEntityManager()){
            TypedQuery<Resource> entities = em.createQuery(
            "SELECT r " +
                    "FROM Resource r " +
                    "WHERE r.formatCategory = :cat " +
                    "ORDER BY r.subCategory", Resource.class)
                    .setParameter("cat", formatCategory);

            return entities.getResultList();
        }
    }

    public List<Resource> findBySubCat(SubCategory subCategory) {
        if (subCategory == null) {
            throw new IllegalArgumentException("SubCategory cannot be null when searching for " + ENTITY_NAME);
        }
        try(EntityManager em = emf.createEntityManager()){
            TypedQuery<Resource> entities = em.createQuery(
                            "SELECT r " +
                                    "FROM Resource r " +
                                    "WHERE r.subCategory = :cat " +
                                    "ORDER BY COALESCE(r.contributor.githubProfile, r.contributor.screenName) ", Resource.class)
                    .setParameter("cat", subCategory);

            return entities.getResultList();
        }
    }

    public Resource findByTitle(String title) {
        if (title == null) {
            throw new IllegalArgumentException("Title cannot be null when searching for " + ENTITY_NAME);
        }
        try(EntityManager em = emf.createEntityManager()){
            Resource entity = em.createQuery(
                            "SELECT r " +
                                    "FROM Resource r " +
                                    "WHERE lower(r.title) = lower(:title)", Resource.class)
                    .setParameter("title", title.toLowerCase().trim())
                    .getSingleResult();

            if(entity == null){
                throw new EntityNotFoundException("No " + ENTITY_NAME + " entity with that title");
            }

            return entity;
        }
    }


    public Set<Resource> findByContributor(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Contributor id cannot be null when searching for " + ENTITY_NAME);
        }
        try(EntityManager em = emf.createEntityManager()){
            TypedQuery<Resource> entities = em.createQuery(
                            "SELECT r " +
                                    "FROM Resource r " +
                                    "WHERE r.contributor.id = :contributor_id", Resource.class)
                    .setParameter("contributor_id", id);

            return new HashSet<>(entities.getResultList());
        }
    }

    public Set<Resource> findByKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            throw new IllegalArgumentException("Keyword cannot be null or blank when searching for " + ENTITY_NAME);
        }
        String pattern = "%" + keyword.toLowerCase().trim() + "%";
        try (EntityManager em = emf.createEntityManager()) {
            TypedQuery<Resource> entities = em.createQuery(
                            "SELECT r " +
                                    "FROM Resource r " +
                                    "WHERE LOWER(r.description) LIKE :pattern",
                            Resource.class)
                    .setParameter("pattern", pattern);

            return new HashSet<>(entities.getResultList());
        }
    }

    //UPDATE
    @Override
    public Resource update(Resource entity) {
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
    @Override
    public boolean delete(Long id) {
        if(id == null){
            throw new IllegalArgumentException("The id for the " + ENTITY_NAME + " entity you want to delete, cannot be null");
        }
        try(EntityManager em = emf.createEntityManager()){
            em.getTransaction().begin();
            Resource foundEntity = em.find(Resource.class, id);
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
