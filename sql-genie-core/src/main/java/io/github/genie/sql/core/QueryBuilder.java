package io.github.genie.sql.core;

import io.github.genie.sql.core.Expression.TypedExpression;
import io.github.genie.sql.core.Models.MultiColumnSelect;
import io.github.genie.sql.core.Models.SingleColumnSelect;
import io.github.genie.sql.core.Query.*;
import io.github.genie.sql.core.exception.BeanReflectiveException;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;

import static io.github.genie.sql.core.Expression.Paths;

public class QueryBuilder<T, U> implements Select0<T, U>, AggWhere0<T, U>, Having0<T, U> {

    public static final Expression.Meta CONSTANT_1 = Metas.of(1);

    static final SingleColumnSelect SELECT_1 =
            new SingleColumnSelect(Integer.class, CONSTANT_1);

    static final SingleColumnSelect COUNT_1 =
            new SingleColumnSelect(Integer.class, Metas.operate(CONSTANT_1, Operator.COUNT, List.of()));


    private final QueryExecutor queryExecutor;
    private final Models.QueryMetadataImpl queryMetadata;


    public QueryBuilder(QueryExecutor queryExecutor, Class<T> type) {
        this(queryExecutor, new Models.QueryMetadataImpl(type));
    }

    QueryBuilder(QueryExecutor queryExecutor, Models.QueryMetadataImpl queryMetadata) {
        this.queryExecutor = queryExecutor;
        this.queryMetadata = queryMetadata;
    }

    <X, Y> QueryBuilder<X, Y> update(Models.QueryMetadataImpl queryMetadata) {
        return new QueryBuilder<>(queryExecutor, queryMetadata);
    }

    @Override
    public <R> Where0<T, R> select(Class<R> projectionType) {
        Models.QueryMetadataImpl metadata = queryMetadata.copy();
        metadata.selectClause = new Models.SelectClauseImpl(projectionType);
        return update(metadata);
    }


    @Override
    public <R> AggWhere0<T, R> select(Path<T, ? extends R> expression) {
        Models.QueryMetadataImpl metadata = queryMetadata.copy();
        Expression.Meta paths = Metas.of(expression);
        Class<?> type = getType(expression);
        metadata.selectClause = new SingleColumnSelect(type, paths);
        return update(metadata);
    }

    @Override
    public AggWhere0<T, Object[]> select(List<? extends TypedExpression<T, ?>> paths) {
        Models.QueryMetadataImpl metadata = queryMetadata.copy();
        metadata.selectClause = new MultiColumnSelect(paths.stream().map(Expression::meta).toList());
        return update(metadata);
    }

    private Class<?> getType(Path<?, ?> path) {
        Class<?> fromClause = queryMetadata.fromClause;
        String name = Util.getMethodReferenceName(path);
        Method method;
        try {
            method = fromClause.getDeclaredMethod(name);
        } catch (NoSuchMethodException e) {
            throw new BeanReflectiveException(e);
        }
        return method.getReturnType();
    }

    @Override
    public AggGroupBy0<T, U> where(TypedExpression<T, Boolean> predicate) {
        Models.QueryMetadataImpl metadata = queryMetadata.copy();
        metadata.whereClause = predicate.meta();
        return update(metadata);
    }

    @Override
    public Collector<U> orderBy(List<? extends Ordering<T>> builder) {
        Models.QueryMetadataImpl metadata = queryMetadata.copy();
        metadata.orderByClause = builder;
        return update(metadata);
    }

    @Override
    public int count() {
        Models.QueryMetadataImpl metadata = buildCountData();
        return queryExecutor.<Number>getList(metadata).get(0).intValue();
    }

    @NotNull
    private Models.QueryMetadataImpl buildCountData() {
        Models.QueryMetadataImpl metadata = queryMetadata.copy();
        metadata.selectClause = COUNT_1;
        metadata.lockModeType = LockModeType.NONE;
        return metadata;
    }

    @Override
    public List<U> getList(int offset, int maxResult, LockModeType lockModeType) {
        Models.QueryMetadataImpl metadata = buildListData(offset, maxResult, lockModeType);
        return queryExecutor.getList(metadata);
    }

    @NotNull
    private Models.QueryMetadataImpl buildListData(int offset, int maxResult, LockModeType lockModeType) {
        Models.QueryMetadataImpl metadata = queryMetadata.copy();
        metadata.offset = offset;
        metadata.limit = maxResult;
        metadata.lockModeType = lockModeType;
        return metadata;
    }

    @Override
    public boolean exist(int offset) {
        Models.QueryMetadataImpl metadata = buildExistData(offset);
        return !queryExecutor.getList(metadata).isEmpty();
    }

    @NotNull
    private Models.QueryMetadataImpl buildExistData(int offset) {
        Models.QueryMetadataImpl metadata = queryMetadata.copy();
        metadata.selectClause = SELECT_1;
        metadata.offset = offset;
        metadata.limit = 1;
        return metadata;
    }

    @Override
    public Metadata metadata() {
        return new Metadata() {
            @Override
            public QueryMetadata count() {
                return buildCountData();
            }

            @Override
            public QueryMetadata getList(int offset, int maxResult, LockModeType lockModeType) {
                return buildListData(offset, maxResult, lockModeType);
            }

            @Override
            public QueryMetadata exist(int offset) {
                return buildExistData(offset);
            }

        };
    }


    @Override
    public OrderBy0<T, U> groupBy(List<TypedExpression<T, ?>> expressions) {
        Models.QueryMetadataImpl metadata = queryMetadata.copy();
        metadata.groupByClause = expressions.stream().map(Expression::meta).toList();
        return update(metadata);
    }

    @Override
    public Having0<T, U> groupBy(Path<T, ?> path) {
        Models.QueryMetadataImpl metadata = queryMetadata.copy();
        metadata.groupByClause = List.of(Metas.of(path));
        return update(metadata);
    }

    @Override
    public GroupBy0<T, T> fetch(List<TypedExpression<T, ?>> paths) {
        Models.QueryMetadataImpl metadata = queryMetadata.copy();
        metadata.fetchPaths = paths
                .stream()
                .map(it -> it.meta() instanceof Paths p ? p : null)
                .filter(Objects::nonNull)
                .toList();
        return update(metadata);
    }

    @Override
    public OrderBy0<T, U> having(TypedExpression<T, Boolean> predicate) {
        Models.QueryMetadataImpl metadata = queryMetadata.copy();
        metadata.havingClause = predicate.meta();
        return update(metadata);
    }


}
