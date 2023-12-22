package io.github.genie.sql.core.executor.jpa;

import io.github.genie.sql.core.Expression.Meta;
import io.github.genie.sql.core.Expression.Paths;
import io.github.genie.sql.core.*;
import io.github.genie.sql.core.Ordering.SortOrder;
import io.github.genie.sql.core.SelectClause.MultiColumn;
import io.github.genie.sql.core.SelectClause.SingleColumn;
import io.github.genie.sql.core.exception.BeanReflectiveException;
import io.github.genie.sql.core.executor.ProjectionUtil;
import io.github.genie.sql.core.mapping.FieldMapping;
import io.github.genie.sql.core.mapping.MappingFactory;
import io.github.genie.sql.core.mapping.Projection;
import io.github.genie.sql.core.mapping.ProjectionField;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.*;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static io.github.genie.sql.core.executor.ProjectionUtil.newProxyInstance;

public class JpaQueryExecutor implements QueryExecutor {

    private final EntityManager entityManager;
    private final MappingFactory mappers;


    public JpaQueryExecutor(EntityManager entityManager, MappingFactory mappers) {
        this.entityManager = entityManager;
        this.mappers = mappers;
    }

    @Override
    public <T> List<T> getList(@NotNull QueryMetadata queryMetadata) {
        SelectClause selected = queryMetadata.select();
        if (selected instanceof SingleColumn singleColumn) {
            List<Object[]> objectsList = getObjectsList(queryMetadata, List.of(singleColumn.column()));
            List<Object> result = objectsList.stream().map(objects -> objects[0]).toList();
            return castList(result);
        } else if (selected instanceof MultiColumn multiColumn) {
            List<Object[]> objectsList = getObjectsList(queryMetadata, multiColumn.columns());
            return castList(objectsList);
        } else {
            Class<?> resultType = queryMetadata.select().resultType();
            if (resultType == queryMetadata.from()) {
                List<?> resultList = getEntityResultList(queryMetadata);
                return castList(resultList);
            } else {
                Projection projectionMapping = mappers
                        .getProjection(queryMetadata.from(), resultType);
                List<ProjectionField> fields = projectionMapping.fields();
                List<Paths> columns = fields.stream()
                        .map(projectionField -> {
                            String fieldName = projectionField.baseField().fieldName();
                            return Metas.fromPath(fieldName);
                        })
                        .toList();
                List<Object[]> objectsList = getObjectsList(queryMetadata, columns);
                List<FieldMapping> list = fields.stream().map(ProjectionField::field).toList();
                if (resultType.isInterface()) {
                    return objectsList.stream()
                            .<T>map(it -> getInterfaceResult(it, list, resultType))
                            .toList();
                } else if (resultType.isRecord()) {
                    return objectsList.stream()
                            .<T>map(it -> getRecordResult(it, list, resultType))
                            .toList();
                } else {
                    return objectsList.stream()
                            .<T>map(it -> getBeanResult(it, list, resultType))
                            .toList();
                }
            }
        }
    }


    @NotNull
    private <R> R getBeanResult(Object[] resultSet,
                                @NotNull List<? extends FieldMapping> fields,
                                Class<?> resultType) {
        try {
            return ProjectionUtil.getBeanResult((index, resultType1) -> resultSet[index], fields, resultType);
        } catch (ReflectiveOperationException e) {
            throw new BeanReflectiveException(e);
        }
    }

    @NotNull
    private <R> R getRecordResult(@NotNull Object[] resultSet,
                                  @NotNull List<? extends FieldMapping> fields,
                                  Class<?> resultType) {
        Map<String, Object> map = new HashMap<>();
        int i = 0;
        for (FieldMapping attribute : fields) {
            Object value = resultSet[i++];
            map.put(attribute.fieldName(), value);
        }
        try {
            return ProjectionUtil.getRecordResult(resultType, map);
        } catch (ReflectiveOperationException e) {
            throw new BeanReflectiveException(e);
        }
    }


    private <R> R getInterfaceResult(Object[] resultSet, List<? extends FieldMapping> fields, Class<?> resultType) {
        Map<Method, Object> map = new HashMap<>();
        int i = 0;
        for (FieldMapping attribute : fields) {
            Object value = resultSet[i++];
            map.put(attribute.getter(), value);
        }

        Object result = newProxyInstance(fields, resultType, map);
        // noinspection unchecked
        return (R) (result);
    }


    private List<?> getEntityResultList(@NotNull QueryMetadata queryMetadata) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<?> query = cb.createQuery(queryMetadata.from());
        Root<?> root = query.from(queryMetadata.from());
        return new EntityBuilder(root, cb, query, queryMetadata).getResultList();
    }

    private List<Object[]> getObjectsList(@NotNull QueryMetadata queryMetadata, List<? extends Meta> columns) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<?> query = cb.createQuery(Object[].class);
        Root<?> root = query.from(queryMetadata.from());
        return new ObjectArrayBuilder(root, cb, query, queryMetadata, columns).getObjectsList();
    }

    private static <T> List<T> castList(List<?> result) {
        // noinspection unchecked
        return (List<T>) result;
    }

    class ObjectArrayBuilder extends Builder {

        private final List<? extends Meta> selects;

        public ObjectArrayBuilder(Root<?> root, CriteriaBuilder cb, CriteriaQuery<?> query, QueryMetadata metadata, List<? extends Meta> selects) {
            super(root, cb, query, metadata);
            this.selects = selects;
        }

        private List<Object[]> getObjectsList() {
            TypedQuery<?> objectsQuery = getObjectsQuery();
            Integer offset = metadata.offset();
            if (offset != null && offset > 0) {
                objectsQuery = objectsQuery.setFirstResult(offset);
            }
            Integer maxResult = metadata.limit();
            if (maxResult != null && maxResult > 0) {
                objectsQuery = objectsQuery.setMaxResults(maxResult);
            }
            LockModeType lockModeType = LockModeTypeAdapter.of(metadata.lockType());
            if (lockModeType != null) {
                objectsQuery.setLockMode(lockModeType);
            }
            return objectsQuery.getResultList()
                    .stream()
                    .map(it -> {
                        if (it instanceof Object[]) {
                            return (Object[]) it;
                        }
                        return new Object[]{it};
                    })
                    .collect(Collectors.toList());
        }

        private TypedQuery<?> getObjectsQuery() {
            setWhere(metadata.where());
            List<? extends Meta> groupBy = metadata.groupBy();
            if (groupBy != null && !groupBy.isEmpty()) {
                List<Expression<?>> grouping = groupBy.stream().map(this::toExpression).collect(Collectors.toList());
                query.groupBy(grouping);
            }
            setOrderBy(metadata.orderBy());
            CriteriaQuery<?> select = query.multiselect(
                    selects.stream()
                            .map(this::toExpression)
                            .collect(Collectors.toList())
            );

            return entityManager.createQuery(select);
        }
    }

    class EntityBuilder extends Builder {
        public EntityBuilder(Root<?> root, CriteriaBuilder cb, CriteriaQuery<?> query, QueryMetadata metadata) {
            super(root, cb, query, metadata);
        }

        protected List<?> getResultList() {
            Integer offset = metadata.offset();
            Integer maxResult = metadata.limit();
            LockModeType lockModeType = LockModeTypeAdapter.of(metadata.lockType());

            setFetch(metadata.fetch());
            setWhere(metadata.where());
            setOrderBy(metadata.orderBy());

            TypedQuery<?> entityQuery = entityManager.createQuery(query);
            if (offset != null && offset > 0) {
                entityQuery = entityQuery.setFirstResult(offset);
            }
            if (maxResult != null && maxResult > 0) {
                entityQuery = entityQuery.setMaxResults(maxResult);
            }
            if (lockModeType != null) {
                entityQuery.setLockMode(lockModeType);
            }
            return entityQuery.getResultList();
        }

        private void setFetch(List<? extends Paths> fetchPaths) {
            if (fetchPaths != null) {
                for (Paths path : fetchPaths) {
                    List<String> paths = path.paths();
                    Fetch<?, ?> fetch = null;
                    for (int i = 0; i < paths.size(); i++) {
                        Fetch<?, ?> cur = fetch;
                        String stringPath = paths.get(i);
                        fetch = (Fetch<?, ?>) fetched.computeIfAbsent(subPaths(paths, i + 1), k -> {
                            if (cur == null) {
                                return root.fetch(stringPath, JoinType.LEFT);
                            } else {
                                return cur.fetch(stringPath, JoinType.LEFT);
                            }
                        });
                    }
                }
            }
        }

    }


    protected static class Builder extends PredicateBuilder {
        protected final QueryMetadata metadata;
        protected final CriteriaQuery<?> query;

        public Builder(Root<?> root, CriteriaBuilder cb, CriteriaQuery<?> query, QueryMetadata metadata) {
            super(root, cb);
            this.metadata = metadata;
            this.query = query;
        }


        protected void setOrderBy(List<? extends Ordering<?>> orderBy) {
            if (orderBy != null && !orderBy.isEmpty()) {
                List<Order> orders = orderBy.stream()
                        .map(o -> o.order() == SortOrder.DESC
                                ? cb.desc(toExpression(o.meta()))
                                : cb.asc(toExpression(o.meta())))
                        .collect(Collectors.toList());
                query.orderBy(orders);
            }
        }

        protected void setWhere(Meta where) {
            if (where != null && !Metas.isTrue(where)) {
                query.where(toPredicate(where));
            }
        }

    }


}
