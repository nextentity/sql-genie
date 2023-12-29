package io.github.genie.sql.builder;

import io.github.genie.sql.api.Expression;
import io.github.genie.sql.api.ExpressionHolder;
import io.github.genie.sql.api.LockModeType;
import io.github.genie.sql.api.Order;
import io.github.genie.sql.api.Path;
import io.github.genie.sql.api.Query;
import io.github.genie.sql.builder.DefaultExpressionOperator.ComparableOpsImpl;
import io.github.genie.sql.builder.DefaultExpressionOperator.Metadata;
import io.github.genie.sql.builder.DefaultExpressionOperator.NumberOpsImpl;
import io.github.genie.sql.builder.DefaultExpressionOperator.StringOpsImpl;
import io.github.genie.sql.builder.QueryStructures.QueryStructureImpl;
import io.github.genie.sql.api.ExpressionOperator.ComparableOperator;
import io.github.genie.sql.api.ExpressionOperator.NumberOperator;
import io.github.genie.sql.api.ExpressionOperator.PathOperator;
import io.github.genie.sql.api.ExpressionOperator.StringOperator;
import io.github.genie.sql.api.Query.AggAndBuilder;
import io.github.genie.sql.api.Query.Having0;
import io.github.genie.sql.api.Query.QueryStructureBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

class AndBuilderImpl<T, U> implements AggAndBuilder<T, U>, AbstractCollector<U> {
    private final QueryBuilder<T, U> queryBuilder;
    DefaultExpressionOperator<T, U, AndBuilderImpl<T, U>> expressionBuilder;

    AndBuilderImpl(QueryBuilder<T, U> queryBuilder, Metadata<AggAndBuilder<T, U>> metadata) {
        expressionBuilder = new DefaultExpressionOperator<>(cast(metadata));
        this.queryBuilder = queryBuilder;
    }

    @Override
    public <N> PathOperator<T, N, AggAndBuilder<T, U>> and(Path<T, N> path) {
        List<Expression> expressions = expressionBuilder.metadata.expressions;
        Expression left = expressionBuilder.merge();
        Expression right = ExpressionBuilders.of(path);
        return new DefaultExpressionOperator<>(new Metadata<>(expressions, left, right, this::update));
    }

    @Override
    public <N extends Number & Comparable<N>> NumberOperator<T, N, AggAndBuilder<T, U>> and(Path.NumberPath<T, N> path) {
        List<Expression> expressions = expressionBuilder.metadata.expressions;
        Expression left = expressionBuilder.merge();
        Expression right = ExpressionBuilders.of(path);
        return new NumberOpsImpl<>(new Metadata<>(expressions, left, right, this::update));
    }

    @Override
    public <N extends Comparable<N>> ComparableOperator<T, N, AggAndBuilder<T, U>> and(Path.ComparablePath<T, N> path) {
        Metadata<AggAndBuilder<T, U>> metadata = getAggAndBuilderMetadata(path);
        return new ComparableOpsImpl<>(metadata);
    }

    @NotNull
    private <N extends Comparable<N>> Metadata<AggAndBuilder<T, U>> getAggAndBuilderMetadata(Path<T, N> path) {
        List<Expression> expressions = expressionBuilder.metadata.expressions;
        Expression left = expressionBuilder.merge();
        Expression right = ExpressionBuilders.of(path);
        return new Metadata<>(expressions, left, right, this::update);
    }

    @Override
    public StringOperator<T, AggAndBuilder<T, U>> and(Path.StringPath<T> path) {
        List<Expression> expressions = expressionBuilder.metadata.expressions;
        Expression left = expressionBuilder.merge();
        Expression right = ExpressionBuilders.of(path);
        return new StringOpsImpl<>(new Metadata<>(expressions, left, right, this::update));
    }


    @Override
    public AggAndBuilder<T, U> and(Path.BooleanPath<T> path) {
        List<Expression> expressions = expressionBuilder.metadata.expressions;
        Expression left = expressionBuilder.merge();
        Expression right = ExpressionBuilders.of(path);
        return update(new Metadata<>(expressions, left, right, this::update));
    }

    @Override
    public AggAndBuilder<T, U> and(ExpressionHolder<T, Boolean> predicate) {
        List<Expression> expressions = expressionBuilder.metadata.expressions;
        Expression left = expressionBuilder.merge();
        Expression right = ExpressionBuilders.of(predicate);
        return update(new Metadata<>(expressions, left, right, this::update));
    }

    @NotNull
    private AndBuilderImpl<T, U> update(Metadata<AggAndBuilder<T, U>> metadata) {
        return new AndBuilderImpl<>(queryBuilder, metadata);
    }

    private static <R> R cast(Object result) {
        // noinspection unchecked
        return (R) result;
    }


    private QueryBuilder<T, U> getQueryBuilder() {
        QueryStructureImpl structure = queryBuilder.queryStructure().copy();
        structure.where = expressionBuilder.expression();
        return queryBuilder.update(structure);
    }

    @Override
    public Query.Collector<U> orderBy(List<? extends Order<T>> path) {
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
    public QueryStructureBuilder buildMetadata() {
        return getQueryBuilder().buildMetadata();
    }

    @Override
    public Query.Having0<T, U> groupBy(List<? extends ExpressionHolder<T, ?>> expressions) {
        return getQueryBuilder().groupBy(expressions);
    }

    @Override
    public Query.Having0<T, U> groupBy(Path<T, ?> path) {
        return getQueryBuilder().groupBy(path);
    }

    @Override
    public Having0<T, U> groupBy(Collection<Path<T, ?>> paths) {
        return groupBy(ExpressionBuilders.toExpressionList(paths));
    }
}
