package io.github.genie.sql.core;

import io.github.genie.sql.core.Expression.TypedExpression;
import io.github.genie.sql.core.Query.*;
import io.github.genie.sql.core.SelectClauseImpl.MultiColumnSelect;
import io.github.genie.sql.core.SelectClauseImpl.SingleColumnSelect;
import io.github.genie.sql.core.exception.BeanReflectiveException;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;

import static io.github.genie.sql.core.BasicExpressions.of;
import static io.github.genie.sql.core.BasicExpressions.operate;
import static io.github.genie.sql.core.Expression.Constant;
import static io.github.genie.sql.core.Expression.Paths;

public class QueryBuilder<T, U> implements Build<T, U>, AggregatableWhere<T, U>, GroupByBuilder<T, U>, Having<T, U> {

    public static final Constant CONSTANT_1 = of(1);

    public static final SingleColumnSelect SELECT_1 =
            new SingleColumnSelect(Integer.class, () -> CONSTANT_1);

    public static final SingleColumnSelect COUNT_1 =
            new SingleColumnSelect(Integer.class, () -> operate(() -> CONSTANT_1, Operator.COUNT, List.of()));


    private final QueryExecutor queryExecutor;
    private final QueryMetadataImpl queryMetadata;


    public QueryBuilder(QueryExecutor queryExecutor, Class<T> type) {
        this(queryExecutor, new QueryMetadataImpl(type));
    }

    public QueryBuilder(QueryExecutor queryExecutor, QueryMetadataImpl queryMetadata) {
        this.queryExecutor = queryExecutor;
        this.queryMetadata = queryMetadata;
    }

    <X> X update(QueryMetadataImpl queryMetadata) {
        return Util.cast(new QueryBuilder<>(queryExecutor, queryMetadata));
    }

    @Override
    public <R, B extends Where<T, R>
            & OrderBy<T, R>
            & Collector<R>>
    B select(Class<R> projectionType) {
        QueryMetadataImpl metadata = queryMetadata.copy();
        metadata.selectClause = new SelectClauseImpl(projectionType);
        return update(metadata);
    }


    @Override
    public <R, B extends AggregatableWhere<T, R>
            & GroupBy<T, R>
            & OrderBy<T, R>
            & Collector<R>>
    B select(Path<T, ? extends R> expression) {
        QueryMetadataImpl metadata = queryMetadata.copy();
        Paths paths = of(expression);
        Class<?> type = getType(expression);
        metadata.selectClause = new SingleColumnSelect(type, () -> paths);
        return update(metadata);
    }

    @Override
    public <B extends AggregatableWhere<T, Object[]>
            & GroupBy<T, Object[]>
            & OrderBy<T, Object[]>
            & Collector<Object[]>>
    B select(List<? extends TypedExpression<T, ?>> paths) {
        QueryMetadataImpl metadata = queryMetadata.copy();
        metadata.selectClause = new MultiColumnSelect(paths);
        return update(metadata);
    }

    private Class<?> getType(Path<?, ?> path) {
        Class<?> fromClause = queryMetadata.fromClause;
        String name = GetterReferenceName.getMethodReferenceName(path);
        Method method;
        try {
            method = fromClause.getDeclaredMethod(name);
        } catch (NoSuchMethodException e) {
            throw new BeanReflectiveException(e);
        }
        return method.getReturnType();
    }

    @Override
    public GroupByBuilder<T, U> where(TypedExpression<T, Boolean> predicate) {
        QueryMetadataImpl metadata = queryMetadata.copy();
        metadata.whereClause = predicate.meta();
        return update(metadata);
    }

    @Override
    public Collector<U> orderBy(List<? extends Ordering<T>> builder) {
        QueryMetadataImpl metadata = queryMetadata.copy();
        metadata.orderByClause = builder;
        return update(metadata);
    }

    @Override
    public int count() {
        QueryMetadataImpl metadata = buildCountData();
        return queryExecutor.<Number>getList(metadata).get(0).intValue();
    }

    @NotNull
    private QueryMetadataImpl buildCountData() {
        QueryMetadataImpl metadata = queryMetadata.copy();
        metadata.selectClause = COUNT_1;
        metadata.lockModeType = LockModeType.NONE;
        return metadata;
    }

    @Override
    public List<U> getList(int offset, int maxResult, LockModeType lockModeType) {
        QueryMetadataImpl metadata = buildListData(offset, maxResult, lockModeType);
        return queryExecutor.getList(metadata);
    }

    @NotNull
    private QueryMetadataImpl buildListData(int offset, int maxResult, LockModeType lockModeType) {
        QueryMetadataImpl metadata = queryMetadata.copy();
        metadata.offset = offset;
        metadata.limit = maxResult;
        metadata.lockModeType = lockModeType;
        return metadata;
    }

    @Override
    public boolean exist(int offset) {
        QueryMetadataImpl metadata = buildExistData(offset);
        return !queryExecutor.getList(metadata).isEmpty();
    }

    @NotNull
    private QueryMetadataImpl buildExistData(int offset) {
        QueryMetadataImpl metadata = queryMetadata.copy();
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
    public <B extends OrderBy<T, U> & Collector<U>> B groupBy(List<OperateableExpression<T, ?>> expressions) {
        QueryMetadataImpl metadata = queryMetadata.copy();
        metadata.groupByClause = expressions.stream().map(Expression::meta).toList();
        return update(metadata);
    }

    @Override
    public <B extends OrderBy<T, U> & Having<T, U> & Collector<U>> B groupBy(Path<T, ?> path) {
        QueryMetadataImpl metadata = queryMetadata.copy();
        metadata.groupByClause = List.of(BasicExpressions.of(path));
        return update(metadata);
    }

    @Override
    public <B extends Where<T, T> & GroupBy<T, T> & OrderBy<T, T> & Collector<T>>
    B fetch(List<OperateableExpression<T, ?>> paths) {
        QueryMetadataImpl metadata = queryMetadata.copy();
        metadata.fetchPaths = paths
                .stream()
                .map(it -> it.meta() instanceof Paths p ? p : null)
                .filter(Objects::nonNull)
                .toList();
        return update(metadata);
    }

    @Override
    public <B extends OrderBy<T, U> & Collector<U>> B having(TypedExpression<T, Boolean> predicate) {
        QueryMetadataImpl metadata = queryMetadata.copy();
        metadata.havingClause = predicate.meta();
        return update(metadata);
    }
}
