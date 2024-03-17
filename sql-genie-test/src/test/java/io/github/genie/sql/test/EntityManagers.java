package io.github.genie.sql.test;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.HashMap;
import java.util.Map;

public class EntityManagers {
    private static final EntityManagerFactory factory = getEntityManagerFactory();

    private static final EntityManager ENTITY_MANAGER = doGetEntityManager();

    private EntityManagers() {
    }

    public static EntityManager getEntityManager() {
        return ENTITY_MANAGER;
    }

    private static EntityManager doGetEntityManager() {
        return factory.createEntityManager();
    }

    private static EntityManagerFactory getEntityManagerFactory() {
        DataSourceConfig config = new DataSourceConfig();

        Map<String, String> properties = new HashMap<>();
        properties.put("javax.persistence.jdbc.url", config.getUrl());
        properties.put("javax.persistence.jdbc.user", config.getUser());
        properties.put("javax.persistence.jdbc.password", config.getPassword());

        return Persistence.createEntityManagerFactory("org.hibernate.jpa", properties);
    }
}
