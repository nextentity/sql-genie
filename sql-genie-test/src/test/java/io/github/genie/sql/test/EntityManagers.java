package io.github.genie.sql.test;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class EntityManagers {
    private static final EntityManagerFactory factory = Persistence.createEntityManagerFactory("org.hibernate.jpa");
    private static final EntityManager ENTITY_MANAGER = doGetEntityManager();

    private EntityManagers() {
    }

    public static EntityManager getEntityManager() {
        return ENTITY_MANAGER;
    }

    private static EntityManager doGetEntityManager() {
        return factory.createEntityManager();
    }


}
