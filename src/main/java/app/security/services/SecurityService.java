package app.security.services;

import app.configs.HibernateConfig;
import app.security.daos.SecurityDAO;
import jakarta.persistence.EntityManagerFactory;

public class SecurityService {
    private final EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();
    private final SecurityDAO userDAO = SecurityDAO.getInstance(emf);


    public SecurityService(){

    }

    public void populateUsers(){

    }


}
