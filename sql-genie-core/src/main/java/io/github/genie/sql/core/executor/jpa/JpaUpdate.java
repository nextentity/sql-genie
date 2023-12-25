package io.github.genie.sql.core.executor.jpa;

import io.github.genie.sql.core.Expression.Meta;
import io.github.genie.sql.core.Expression.Paths;
import io.github.genie.sql.core.Expressions;
import io.github.genie.sql.core.Metas;
import io.github.genie.sql.core.Operator;
import io.github.genie.sql.core.Query;
import io.github.genie.sql.core.Update;
import io.github.genie.sql.core.mapping.ReflectUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TransactionRequiredException;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.SingularAttribute;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Slf4j
public class JpaUpdate implements Update {

    private final EntityManager entityManager;
    private final Query query;

    public JpaUpdate(EntityManager entityManager, JpaQueryExecutor jpaQueryExecutor) {
        this.entityManager = entityManager;
        this.query = Query.createQuery(jpaQueryExecutor);
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
        List<Meta> ids = new ArrayList<>();
        Set<Object> uniqueValues = new HashSet<>();
        for (T entity : entities) {
            Object id = requireId(entity);
            if (uniqueValues.add(id)) {
                ids.add(Metas.of(id));
            } else {
                throw new IllegalArgumentException("duplicate id");
            }
        }
        if (!ids.isEmpty()) {
            EntityType<T> entity = entityManager.getMetamodel().entity(entityType);
            SingularAttribute<? super T, ?> id = entity.getId(entity.getIdType().getJavaType());
            String name = id.getName();
            Paths idPath = Expressions.ofPath(name);
            Meta operate = Expressions.operate(idPath, Operator.IN, ids);
            List<T> dbList = query.from(entityType)
                    .where(() -> operate)
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
