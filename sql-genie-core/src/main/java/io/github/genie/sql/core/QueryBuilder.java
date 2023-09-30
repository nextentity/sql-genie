package io.github.genie.sql.core;

import io.github.genie.sql.core.Expression.Meta;
import io.github.genie.sql.core.Expression.TypedExpression;
import io.github.genie.sql.core.Models.MultiColumnSelect;
import io.github.genie.sql.core.Models.QueryMetadataImpl;
import io.github.genie.sql.core.Models.SelectClauseImpl;
import io.github.genie.sql.core.Models.SingleColumnSelect;
import io.github.genie.sql.core.Query.*;
import io.github.genie.sql.core.exception.BeanReflectiveException;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.List;

public class QueryBuilder<T, U> implements Select0<T, U>, AggWhere0<T, U>, Having0<T, U> {

    public static final Meta CONSTANT_1 = Metas.of(1);

    static final SingleColumnSelect SELECT_1 =
            new SingleColumnSelect(Integer.class, CONSTANT_1);

    static final SingleColumnSelect COUNT_1 =
            new SingleColumnSelect(Integer.class, Metas.operate(CONSTANT_1, Operator.COUNT, List.of()));


    private final QueryExecutor queryExecutor;
    private final QueryMetadataImpl queryMetadata;


    public QueryBuilder(QueryExecutor queryExecutor, Class<T> type) {
        this(queryExecutor, new QueryMetadataImpl(type));
    }

    QueryBuilder(QueryExecutor queryExecutor, QueryMetadataImpl queryMetadata) {
        this.queryExecutor = queryExecutor;
        this.queryMetadata = queryMetadata;
    }

    <X, Y> QueryBuilder<X, Y> update(QueryMetadataImpl queryMetadata) {
        return new QueryBuilder<>(queryExecutor, queryMetadata);
    }

    @Override
    public <R> Where0<T, R> select(Class<R> projectionType) {
        QueryMetadataImpl metadata = queryMetadata.copy();
        metadata.select = new SelectClauseImpl(projectionType);
        return update(metadata);
    }


    @Override
    public <R> AggWhere0<T, R> select(Path<T, ? extends R> expression) {
        QueryMetadataImpl metadata = queryMetadata.copy();
        Meta paths = Metas.of(expression);
        Class<?> type = getType(expression);
        metadata.select = new SingleColumnSelect(type, paths);
        return update(metadata);
    }

    @Override
    public AggWhere0<T, Object[]> select(List<? extends TypedExpression<T, ?>> expressions) {
        QueryMetadataImpl metadata = queryMetadata.copy();
        metadata.select = new MultiColumnSelect(expressions.stream().map(Expression::meta).toList());
        return update(metadata);
    }

    private Class<?> getType(Path<?, ?> path) {
        Class<?> fromClause = queryMetadata.from;
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
        QueryMetadataImpl metadata = queryMetadata.copy();
        metadata.where = predicate.meta();
        return update(metadata);
    }

    @Override
    public Collector<U> orderBy(List<? extends Ordering<T>> builder) {
        QueryMetadataImpl metadata = queryMetadata.copy();
        metadata.orderBy = builder;
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
        metadata.select = COUNT_1;
        metadata.lockType = LockModeType.NONE;
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
        metadata.lockType = lockModeType;
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
        metadata.select = SELECT_1;
        metadata.offset = offset;
        metadata.limit = 1;
        return metadata;
    }

    @Override
    public MetadataBuilder buildMetadata() {
        return new MetadataBuilder() {
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
    public OrderBy0<T, U> groupBy(List<? extends TypedExpression<T, ?>> expressions) {
        QueryMetadataImpl metadata = queryMetadata.copy();
        metadata.groupBy = expressions.stream().map(Expression::meta).toList();
        return update(metadata);
    }

    @Override
    public Having0<T, U> groupBy(Path<T, ?> path) {
        QueryMetadataImpl metadata = queryMetadata.copy();
        metadata.groupBy = List.of(Metas.of(path));
        return update(metadata);
    }

    @Override
    public GroupBy0<T, T> fetch(List<PathExpression<T, ?>> expressions) {
        QueryMetadataImpl metadata = queryMetadata.copy();
        metadata.fetch = expressions
                .stream()
                .map(PathExpression::meta)
                .toList();
        return update(metadata);
    }

    @Override
    public OrderBy0<T, U> having(TypedExpression<T, Boolean> predicate) {
        QueryMetadataImpl metadata = queryMetadata.copy();
        metadata.havingClause = predicate.meta();
        return update(metadata);
    }


}
