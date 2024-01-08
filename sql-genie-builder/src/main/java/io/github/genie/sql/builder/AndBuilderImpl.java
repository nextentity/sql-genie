package io.github.genie.sql.builder;

import io.github.genie.sql.api.Expression;
import io.github.genie.sql.api.ExpressionHolder;
import io.github.genie.sql.api.ExpressionOperator.ComparableOperator;
import io.github.genie.sql.api.ExpressionOperator.NumberOperator;
import io.github.genie.sql.api.ExpressionOperator.PathOperator;
import io.github.genie.sql.api.ExpressionOperator.StringOperator;
import io.github.genie.sql.api.LockModeType;
import io.github.genie.sql.api.Order;
import io.github.genie.sql.api.Path;
import io.github.genie.sql.api.Query.AndBuilder0;
import io.github.genie.sql.api.Query.Collector;
import io.github.genie.sql.api.Query.Having;
import io.github.genie.sql.api.Query.QueryStructureBuilder;
import io.github.genie.sql.builder.DefaultExpressionOperator.ComparableOpsImpl;
import io.github.genie.sql.builder.DefaultExpressionOperator.Context;
import io.github.genie.sql.builder.DefaultExpressionOperator.NumberOpsImpl;
import io.github.genie.sql.builder.DefaultExpressionOperator.StringOpsImpl;
import io.github.genie.sql.builder.QueryStructures.QueryStructureImpl;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

class AndBuilderImpl<T, U> implements AndBuilder0<T, U>, AbstractCollector<U> {
    private final QueryConditionBuilder<T, U> queryBuilder;
    final DefaultExpressionOperator<T, U, AndBuilderImpl<T, U>> expressionBuilder;

    AndBuilderImpl(QueryConditionBuilder<T, U> queryBuilder, Context<AndBuilder0<T, U>> context) {
        expressionBuilder = new DefaultExpressionOperator<>(TypeCastUtil.unsafeCast(context));
        this.queryBuilder = queryBuilder;
    }

    @Override
    public <N> PathOperator<T, N, AndBuilder0<T, U>> and(Path<T, N> path) {
        List<Expression> expressions = expressionBuilder.context.expressions;
        Expression left = expressionBuilder.merge();
        Expression right = Expressions.of(path);
        return new DefaultExpressionOperator<>(new Context<>(expressions, left, right, this::update));
    }

    @Override
    public <N extends Number & Comparable<N>> NumberOperator<T, N, AndBuilder0<T, U>> and(Path.NumberPath<T, N> path) {
        List<Expression> expressions = expressionBuilder.context.expressions;
        Expression left = expressionBuilder.merge();
        Expression right = Expressions.of(path);
        return new NumberOpsImpl<>(new Context<>(expressions, left, right, this::update));
    }

    @Override
    public <N extends Comparable<N>> ComparableOperator<T, N, AndBuilder0<T, U>> and(Path.ComparablePath<T, N> path) {
        Context<AndBuilder0<T, U>> ctx = getAggAndBuilderMetadata(path);
        return new ComparableOpsImpl<>(ctx);
    }

    @NotNull
    private <N extends Comparable<N>> DefaultExpressionOperator.Context<AndBuilder0<T, U>> getAggAndBuilderMetadata(Path<T, N> path) {
        List<Expression> expressions = expressionBuilder.context.expressions;
        Expression left = expressionBuilder.merge();
        Expression right = Expressions.of(path);
        return new Context<>(expressions, left, right, this::update);
    }

    @Override
    public StringOperator<T, AndBuilder0<T, U>> and(Path.StringPath<T> path) {
        List<Expression> expressions = expressionBuilder.context.expressions;
        Expression left = expressionBuilder.merge();
        Expression right = Expressions.of(path);
        return new StringOpsImpl<>(new Context<>(expressions, left, right, this::update));
    }

    @Override
    public AndBuilder0<T, U> and(Path.BooleanPath<T> path) {
        List<Expression> expressions = expressionBuilder.context.expressions;
        Expression left = expressionBuilder.merge();
        Expression right = Expressions.of(path);
        return update(new Context<>(expressions, left, right, this::update));
    }

    @Override
    public AndBuilder0<T, U> and(ExpressionHolder<T, Boolean> predicate) {
        List<Expression> expressions = expressionBuilder.context.expressions;
        Expression left = expressionBuilder.merge();
        Expression right = Expressions.of(predicate);
        return update(new Context<>(expressions, left, right, this::update));
    }

    @NotNull
    private AndBuilderImpl<T, U> update(Context<AndBuilder0<T, U>> context) {
        return new AndBuilderImpl<>(queryBuilder, context);
    }

    private QueryConditionBuilder<T, U> getQueryBuilder() {
        QueryStructureImpl structure = queryBuilder.queryStructure().copy();
        structure.where = expressionBuilder.expression();
        return queryBuilder.update(structure);
    }

    @Override
    public Collector<U> orderBy(List<? extends Order<T>> orders) {
        return getQueryBuilder().orderBy(orders);
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
    public Having<T, U> groupBy(List<? extends ExpressionHolder<T, ?>> expressions) {
        return getQueryBuilder().groupBy(expressions);
    }

    @Override
    public Having<T, U> groupBy(Path<T, ?> path) {
        return getQueryBuilder().groupBy(path);
    }

    @Override
    public Having<T, U> groupBy(Collection<Path<T, ?>> paths) {
        return groupBy(Expressions.toExpressionList(paths));
    }
}
