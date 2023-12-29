package io.github.genie.sql.executor.jpa;

import io.github.genie.sql.builder.AbstractQueryExecutor;
import io.github.genie.sql.api.Expression;
import io.github.genie.sql.api.Column;
import io.github.genie.sql.builder.ExpressionBuilders;
import io.github.genie.sql.api.Order;
import io.github.genie.sql.api.Order.SortOrder;
import io.github.genie.sql.api.QueryStructure;
import io.github.genie.sql.api.Selection;
import io.github.genie.sql.api.Selection.MultiColumn;
import io.github.genie.sql.api.Selection.SingleColumn;
import io.github.genie.sql.builder.executor.ProjectionUtil;
import io.github.genie.sql.builder.meta.Attribute;
import io.github.genie.sql.builder.meta.Metamodel;
import io.github.genie.sql.builder.meta.Projection;
import io.github.genie.sql.builder.meta.ProjectionAttribute;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Fetch;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Root;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public class JpaQueryExecutor implements AbstractQueryExecutor {

    private final EntityManager entityManager;
    private final Metamodel mappers;


    public JpaQueryExecutor(EntityManager entityManager, Metamodel mappers) {
        this.entityManager = entityManager;
        this.mappers = mappers;
    }

    @Override
    public <T> List<T> getList(@NotNull QueryStructure queryStructure) {
        Selection selected = queryStructure.select();
        if (selected instanceof SingleColumn singleColumn) {
            List<Object[]> objectsList = getObjectsList(queryStructure, List.of(singleColumn.column()));
            List<Object> result = objectsList.stream().map(objects -> objects[0]).toList();
            return castList(result);
        } else if (selected instanceof MultiColumn multiColumn) {
            List<Object[]> objectsList = getObjectsList(queryStructure, multiColumn.columns());
            return castList(objectsList);
        } else {
            Class<?> resultType = queryStructure.select().resultType();
            if (resultType == queryStructure.from()) {
                List<?> resultList = getEntityResultList(queryStructure);
                return castList(resultList);
            } else {
                Projection projectionMapping = mappers
                        .getProjection(queryStructure.from(), resultType);
                List<ProjectionAttribute> fields = projectionMapping.attributes();
                List<Column> columns = fields.stream()
                        .map(projectionField -> {
                            String fieldName = projectionField.baseField().name();
                            return ExpressionBuilders.fromPath(fieldName);
                        })
                        .toList();
                List<Object[]> objectsList = getObjectsList(queryStructure, columns);
                List<Attribute> list = fields.stream().map(ProjectionAttribute::field).toList();
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


    private List<?> getEntityResultList(@NotNull QueryStructure structure) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<?> query = cb.createQuery(structure.from());
        Root<?> root = query.from(structure.from());
        return new EntityBuilder(root, cb, query, structure).getResultList();
    }

    private List<Object[]> getObjectsList(@NotNull QueryStructure structure, List<? extends Expression> columns) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<?> query = cb.createQuery(Object[].class);
        Root<?> root = query.from(structure.from());
        return new ObjectArrayBuilder(root, cb, query, structure, columns).getResultList();
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
                                  QueryStructure structure,
                                  List<? extends Expression> selects) {
            super(root, cb, query, structure);
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
        public EntityBuilder(Root<?> root, CriteriaBuilder cb, CriteriaQuery<?> query, QueryStructure structure) {
            super(root, cb, query, structure);
        }

        @Override
        protected TypedQuery<?> getTypedQuery() {
            return entityManager.createQuery(query);
        }

    }


    protected static abstract class Builder extends PredicateBuilder {
        protected final QueryStructure structure;
        protected final CriteriaQuery<?> query;

        public Builder(Root<?> root, CriteriaBuilder cb, CriteriaQuery<?> query, QueryStructure structure) {
            super(root, cb);
            this.structure = structure;
            this.query = query;
        }


        protected void setOrderBy(List<? extends Order<?>> orderBy) {
            if (orderBy != null && !orderBy.isEmpty()) {
                List<jakarta.persistence.criteria.Order> orders = orderBy.stream()
                        .map(o -> o.order() == SortOrder.DESC
                                ? cb.desc(toExpression(o.expression()))
                                : cb.asc(toExpression(o.expression())))
                        .collect(Collectors.toList());
                query.orderBy(orders);
            }
        }

        protected void setWhere(Expression where) {
            if (where != null && !ExpressionBuilders.isTrue(where)) {
                query.where(toPredicate(where));
            }
        }

        protected void setGroupBy(List<? extends Expression> groupBy) {
            if (groupBy != null && !groupBy.isEmpty()) {
                List<jakarta.persistence.criteria.Expression<?>> grouping = groupBy.stream().map(this::toExpression).collect(Collectors.toList());
                query.groupBy(grouping);
            }
        }

        protected void setFetch(List<? extends Column> fetchPaths) {
            if (fetchPaths != null) {
                for (Column path : fetchPaths) {
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
            setWhere(structure.where());
            setGroupBy(structure.groupBy());
            setOrderBy(structure.orderBy());
            setFetch(structure.fetch());
            TypedQuery<?> objectsQuery = getTypedQuery();
            Integer offset = structure.offset();
            if (offset != null && offset > 0) {
                objectsQuery = objectsQuery.setFirstResult(offset);
            }
            Integer maxResult = structure.limit();
            if (maxResult != null && maxResult > 0) {
                objectsQuery = objectsQuery.setMaxResults(maxResult);
            }
            LockModeType lockModeType = LockModeTypeAdapter.of(structure.lockType());
            if (lockModeType != null) {
                objectsQuery.setLockMode(lockModeType);
            }
            return objectsQuery.getResultList();
        }

        protected abstract TypedQuery<?> getTypedQuery();

    }


}
