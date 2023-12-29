package io.github.genie.sql.core.executor.jpa;

import io.github.genie.sql.core.ExpressionBuilder.Expression;
import io.github.genie.sql.core.ExpressionBuilder.Paths;
import io.github.genie.sql.core.Metas;
import io.github.genie.sql.core.Ordering;
import io.github.genie.sql.core.Ordering.SortOrder;
import io.github.genie.sql.core.QueryExecutor;
import io.github.genie.sql.core.QueryStructure;
import io.github.genie.sql.core.Selection;
import io.github.genie.sql.core.Selection.MultiColumn;
import io.github.genie.sql.core.Selection.SingleColumn;
import io.github.genie.sql.core.executor.ProjectionUtil;
import io.github.genie.sql.core.mapping.FieldMapping;
import io.github.genie.sql.core.mapping.MappingFactory;
import io.github.genie.sql.core.mapping.Projection;
import io.github.genie.sql.core.mapping.ProjectionField;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Fetch;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Root;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public class JpaQueryExecutor implements QueryExecutor {

    private final EntityManager entityManager;
    private final MappingFactory mappers;


    public JpaQueryExecutor(EntityManager entityManager, MappingFactory mappers) {
        this.entityManager = entityManager;
        this.mappers = mappers;
    }

    @Override
    public <T> List<T> getList(@NotNull QueryStructure queryMetadata) {
        Selection selected = queryMetadata.select();
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
                            .<T>map(it -> ProjectionUtil.getInterfaceResult(getResultSet(it), list, resultType))
                            .toList();
                } else if (resultType.isRecord()) {
                    return objectsList.stream()
                            .<T>map(it -> ProjectionUtil.getRecordResult(getResultSet(it), list, resultType))
                            .toList();
                } else {
                    return objectsList.stream()
                            .<T>map(it -> ProjectionUtil.getBeanResult(getResultSet(it), list, resultType))
                            .toList();
                }
            }
        }
    }


    @NotNull
    private static BiFunction<Integer, Class<?>, Object> getResultSet(Object[] resultSet) {
        return (index, resultType1) -> resultSet[index];
    }


    private List<?> getEntityResultList(@NotNull QueryStructure queryMetadata) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<?> query = cb.createQuery(queryMetadata.from());
        Root<?> root = query.from(queryMetadata.from());
        return new EntityBuilder(root, cb, query, queryMetadata).getResultList();
    }

    private List<Object[]> getObjectsList(@NotNull QueryStructure queryMetadata, List<? extends Expression> columns) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<?> query = cb.createQuery(Object[].class);
        Root<?> root = query.from(queryMetadata.from());
        return new ObjectArrayBuilder(root, cb, query, queryMetadata, columns).getResultList();
    }

    private static <T> List<T> castList(List<?> result) {
        // noinspection unchecked
        return (List<T>) result;
    }

    class ObjectArrayBuilder extends Builder {

        private final List<? extends Expression> selects;

        public ObjectArrayBuilder(Root<?> root,
                                  CriteriaBuilder cb,
                                  CriteriaQuery<?> query,
                                  QueryStructure metadata,
                                  List<? extends Expression> selects) {
            super(root, cb, query, metadata);
            this.selects = selects;
        }

        public List<Object[]> getResultList() {
            return super.getResultList()
                    .stream()
                    .map(it -> {
                        if (it instanceof Object[]) {
                            return (Object[]) it;
                        }
                        return new Object[]{it};
                    })
                    .collect(Collectors.toList());
        }

        @Override
        protected TypedQuery<?> getTypedQuery() {
            CriteriaQuery<?> select = query.multiselect(
                    selects.stream()
                            .map(this::toExpression)
                            .collect(Collectors.toList())
            );

            return entityManager.createQuery(select);
        }


    }

    class EntityBuilder extends Builder {
        public EntityBuilder(Root<?> root, CriteriaBuilder cb, CriteriaQuery<?> query, QueryStructure metadata) {
            super(root, cb, query, metadata);
        }

        @Override
        protected TypedQuery<?> getTypedQuery() {
            return entityManager.createQuery(query);
        }

    }


    protected static abstract class Builder extends PredicateBuilder {
        protected final QueryStructure metadata;
        protected final CriteriaQuery<?> query;

        public Builder(Root<?> root, CriteriaBuilder cb, CriteriaQuery<?> query, QueryStructure metadata) {
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

        protected void setWhere(Expression where) {
            if (where != null && !Metas.isTrue(where)) {
                query.where(toPredicate(where));
            }
        }

        protected void setGroupBy(List<? extends Expression> groupBy) {
            if (groupBy != null && !groupBy.isEmpty()) {
                List<jakarta.persistence.criteria.Expression<?>> grouping = groupBy.stream().map(this::toExpression).collect(Collectors.toList());
                query.groupBy(grouping);
            }
        }

        protected void setFetch(List<? extends Paths> fetchPaths) {
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

        protected List<?> getResultList() {
            setWhere(metadata.where());
            setGroupBy(metadata.groupBy());
            setOrderBy(metadata.orderBy());
            setFetch(metadata.fetch());
            TypedQuery<?> objectsQuery = getTypedQuery();
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
            return objectsQuery.getResultList();
        }

        protected abstract TypedQuery<?> getTypedQuery();

    }


}
