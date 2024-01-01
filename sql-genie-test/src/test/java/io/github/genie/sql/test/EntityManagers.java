package io.github.genie.sql.test;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

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
