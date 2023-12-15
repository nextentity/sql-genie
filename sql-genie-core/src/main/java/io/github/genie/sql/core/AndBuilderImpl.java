package io.github.genie.sql.core;

import io.github.genie.sql.core.Expression.Meta;
import io.github.genie.sql.core.ExpressionBuilder.ComparableOpsImpl;
import io.github.genie.sql.core.ExpressionBuilder.Metadata;
import io.github.genie.sql.core.ExpressionBuilder.NumberOpsImpl;
import io.github.genie.sql.core.ExpressionBuilder.StringOpsImpl;
import io.github.genie.sql.core.Models.QueryMetadataImpl;
import org.jetbrains.annotations.NotNull;

import java.util.List;

class AndBuilderImpl<T, U> implements Query.OrderBy1<T, U> {
    private final QueryBuilder<T, U> queryBuilder;
    ExpressionBuilder<T, U, AndBuilderImpl<T, U>> expressionBuilder;

    AndBuilderImpl(QueryBuilder<T, U> queryBuilder, Metadata<Query.OrderBy1<T, U>> metadata) {
        expressionBuilder = new ExpressionBuilder<>(cast(metadata));
        this.queryBuilder = queryBuilder;
    }

    @Override
    public <N extends Number & Comparable<N>> ExpressionOps.NumberOps<T, N, Query.OrderBy1<T, U>> and(Path.NumberPath<T, N> path) {
        List<Meta> expressions = expressionBuilder.metadata.expressions;
        Meta left = expressionBuilder.merge();
        Meta right = Metas.of(path);
        return new NumberOpsImpl<>(new Metadata<>(expressions, left, right, this::update));
    }

    @Override
    public <N extends Comparable<N>> ExpressionOps.ComparableOps<T, N, Query.OrderBy1<T, U>> and(Path.ComparablePath<T, N> path) {
        List<Meta> expressions = expressionBuilder.metadata.expressions;
        Meta left = expressionBuilder.merge();
        Meta right = Metas.of(path);
        return new ComparableOpsImpl<>(new Metadata<>(expressions, left, right, this::update));
    }

    @Override
    public ExpressionOps.StringOps<T, Query.OrderBy1<T, U>> and(Path.StringPath<T> path) {
        List<Meta> expressions = expressionBuilder.metadata.expressions;
        Meta left = expressionBuilder.merge();
        Meta right = Metas.of(path);
        return new StringOpsImpl<>(new Metadata<>(expressions, left, right, this::update));
    }


    @Override
    public Query.OrderBy1<T, U> and(Path.BooleanPath<T> path) {
        List<Meta> expressions = expressionBuilder.metadata.expressions;
        Meta left = expressionBuilder.merge();
        Meta right = Metas.of(path);
        return update(new Metadata<>(expressions, left, right, this::update));
    }

    @Override
    public Query.OrderBy1<T, U> and(Expression<T, Boolean> predicate) {
        List<Meta> expressions = expressionBuilder.metadata.expressions;
        Meta left = expressionBuilder.merge();
        Meta right = Metas.of(predicate);
        return update(new Metadata<>(expressions, left, right, this::update));
    }

    @NotNull
    private AndBuilderImpl<T, U> update(Metadata<Query.OrderBy1<T, U>> metadata) {
        return new AndBuilderImpl<>(queryBuilder, metadata);
    }

    private static <R> R cast(Object result) {
        // noinspection unchecked
        return (R) result;
    }


    private Query.OrderBy0<T, U> getQueryBuilder() {
        QueryMetadataImpl queryMetadata = queryBuilder.queryMetadata().copy();
        queryMetadata.where = expressionBuilder.meta();
        return queryBuilder.update(queryMetadata);
    }

    @Override
    public Query.Collector<U> orderBy(List<? extends Ordering<T>> path) {
        return getQueryBuilder().orderBy(path);
    }

    @Override
    public int count() {
        return getQueryBuilder().count();
    }

    @Override
    public List<U> getList(int offset, int maxResult, LockModeType lockModeType) {
        return getQueryBuilder().getList(offset, maxResult, lockModeType);
    }

    @Override
    public boolean exist(int offset) {
        return getQueryBuilder().exist(offset);
    }

    @Override
    public Query.MetadataBuilder buildMetadata() {
        return getQueryBuilder().buildMetadata();
    }
}
