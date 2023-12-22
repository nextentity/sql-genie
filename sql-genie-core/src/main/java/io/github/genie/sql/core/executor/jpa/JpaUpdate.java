package io.github.genie.sql.core.executor.jpa;

import io.github.genie.sql.core.Update;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class JpaUpdate implements Update {

    private final EntityManager entityManager;

    public JpaUpdate(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public <T> List<T> insert(List<T> entities, Class<T> entityType) {
        for (T entity : entities) {
            entityManager.persist(entity);
        }
        return entities;
    }

    @Override
    public <T> List<T> update(List<T> entities, Class<T> entityType) {
        List<T> list = new ArrayList<>();
        for (T entity : entities) {
            T merge = entityManager.merge(entity);
            list.add(merge);
        }
        return list;
    }

    @Override
    public <T> void updateNonNullColumn(T entity, Class<T> entityType) {
        // TODO
    }
}
