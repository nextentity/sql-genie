package io.github.genie.sql.executor.jpa;

import io.github.genie.sql.api.*;
import io.github.genie.sql.builder.ExpressionHolders;
import io.github.genie.sql.builder.Expressions;
import io.github.genie.sql.builder.meta.ReflectUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TransactionRequiredException;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.SingularAttribute;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Slf4j
public class JpaUpdate implements Update {

    private final EntityManager entityManager;
    private final Query query;

    public JpaUpdate(EntityManager entityManager, JpaQueryExecutor jpaQueryExecutor) {
        this.entityManager = entityManager;
        this.query = jpaQueryExecutor.createQuery();
    }

    @Override
    public <T> List<T> insert(List<T> entities, Class<T> entityType) {
        requiredTransaction();
        for (T entity : entities) {
            entityManager.persist(entity);
        }
        return entities;
    }

    private void requiredTransaction() {
        if (!entityManager.getTransaction().isActive()) {
            throw new TransactionRequiredException();
        }
    }

    @Override
    public <T> List<T> update(List<T> entities, Class<T> entityType) {
        requiredTransaction();
        List<Expression> ids = new ArrayList<>();
        Set<Object> uniqueValues = new HashSet<>();
        for (T entity : entities) {
            Object id = requireId(entity);
            if (uniqueValues.add(id)) {
                ids.add(Expressions.of(id));
            } else {
                throw new IllegalArgumentException("duplicate id");
            }
        }
        if (!ids.isEmpty()) {
            EntityType<T> entity = entityManager.getMetamodel().entity(entityType);
            SingularAttribute<? super T, ?> id = entity.getId(entity.getIdType().getJavaType());
            String name = id.getName();
            Column idPath = Expressions.column(name);
            Expression operate = Expressions.operate(idPath, Operator.IN, ids);
            List<T> dbList = query.from(entityType)
                    .where(ExpressionHolders.of(operate))
                    .getList();
            if (dbList.size() != entities.size()) {
                throw new IllegalArgumentException("some id not found");
            }
        }
        List<T> list = new ArrayList<>(entities.size());
        for (T entity : entities) {
            T merge = entityManager.merge(entity);
            list.add(merge);
        }
        return list;
    }

    @Override
    public <T> void delete(List<T> entities, Class<T> entityType) {
        requiredTransaction();
        for (T entity : entities) {
            entityManager.remove(entity);
        }
    }

    @Override
    public <T> T updateNonNullColumn(T entity, Class<T> entityType) {
        requiredTransaction();
        Object id = requireId(entity);
        T t = entityManager.find(entityType, id);
        if (t == null) {
            throw new IllegalArgumentException("id not found");
        }
        ReflectUtil.copyTargetNullFields(t, entity, entityType);
        return entityManager.merge(entity);
    }

    private <T> Object requireId(T entity) {
        Object id = entityManager.getEntityManagerFactory()
                .getPersistenceUnitUtil()
                .getIdentifier(entity);
        return Objects.requireNonNull(id);
    }

}
