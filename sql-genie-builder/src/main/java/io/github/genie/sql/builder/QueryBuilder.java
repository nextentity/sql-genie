package io.github.genie.sql.builder;

import io.github.genie.sql.api.Column;
import io.github.genie.sql.api.Expression;
import io.github.genie.sql.api.ExpressionHolder;
import io.github.genie.sql.api.ExpressionHolder.ColumnHolder;
import io.github.genie.sql.api.ExpressionOperator.ComparableOperator;
import io.github.genie.sql.api.ExpressionOperator.NumberOperator;
import io.github.genie.sql.api.ExpressionOperator.PathOperator;
import io.github.genie.sql.api.ExpressionOperator.StringOperator;
import io.github.genie.sql.api.LockModeType;
import io.github.genie.sql.api.Order;
import io.github.genie.sql.api.Path;
import io.github.genie.sql.api.Path.BooleanPath;
import io.github.genie.sql.api.Path.ComparablePath;
import io.github.genie.sql.api.Path.NumberPath;
import io.github.genie.sql.api.Path.StringPath;
import io.github.genie.sql.api.Query.AndBuilder0;
import io.github.genie.sql.api.Query.Collector;
import io.github.genie.sql.api.Query.Fetch;
import io.github.genie.sql.api.Query.Having;
import io.github.genie.sql.api.Query.OrderOperator;
import io.github.genie.sql.api.Query.QueryStructureBuilder;
import io.github.genie.sql.api.Query.Select;
import io.github.genie.sql.api.Query.Where0;
import io.github.genie.sql.api.QueryExecutor;
import io.github.genie.sql.api.TypedExpression;
import io.github.genie.sql.api.TypedExpression.AndOperator;
import io.github.genie.sql.api.TypedExpression.BooleanExpression;
import io.github.genie.sql.builder.DefaultExpressionOperator.ComparableOperatorImpl;
import io.github.genie.sql.builder.DefaultExpressionOperator.NumberOperatorImpl;
import io.github.genie.sql.builder.DefaultExpressionOperator.PathOperatorImpl;
import io.github.genie.sql.builder.DefaultExpressionOperator.StringOperatorImpl;
import io.github.genie.sql.builder.QueryStructures.MultiColumnSelect;
import io.github.genie.sql.builder.QueryStructures.QueryStructureImpl;
import io.github.genie.sql.builder.QueryStructures.SelectClauseImpl;
import io.github.genie.sql.builder.QueryStructures.SingleColumnSelect;
import io.github.genie.sql.builder.exception.BeanReflectiveException;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("PatternVariableCanBeUsed")
public class QueryBuilder<T> extends QueryConditionBuilder<T, T> implements Select<T>, Fetch<T> {
    public QueryBuilder(QueryExecutor queryExecutor, Class<T> type) {
        super(queryExecutor, type);
    }

    public QueryBuilder(QueryExecutor queryExecutor, Class<T> type, QueryStructurePostProcessor structurePostProcessor) {
        super(queryExecutor, type, structurePostProcessor);
    }

    public Where0<T, T> fetch(List<ColumnHolder<T, ?>> expressions) {
        QueryStructureImpl structure = queryStructure.copy();
        List<Column> list = new ArrayList<>(expressions.size());
        for (ColumnHolder<T, ?> expression : expressions) {
            Expression expr = expression.expression();
            if (expr instanceof Column) {
                Column column = (Column) expr;
                list.add(column);
            }
        }
        structure.fetch = list;
        return update(structure);
    }

    public Where0<T, T> fetch(Collection<Path<T, ?>> paths) {
        return fetch(Expressions.toExpressionList(paths));
    }

    @Override
    public <R> Where0<T, R> selectDistinct(Class<R> projectionType) {
        return select(true, projectionType);
    }

    @Override
    public <R> Where0<T, R> select(Class<R> projectionType) {
        return select(false, projectionType);
    }

    public <R> Where0<T, R> select(boolean distinct, Class<R> projectionType) {
        QueryStructureImpl structure = queryStructure.copy();
        structure.select = new SelectClauseImpl(projectionType, distinct);
        return update(structure);
    }

    public <R> Where0<T, R> selectDistinct(Path<T, ? extends R> path) {
        return select(true, path);
    }

    public <R> Where0<T, R> select(Path<T, ? extends R> path) {
        return select(false, path);
    }

    public <R> Where0<T, R> select(boolean distinct, Path<T, ? extends R> path) {
        QueryStructureImpl structure = queryStructure.copy();
        Expression paths = Expressions.of(path);
        Class<?> type = getType(path);
        structure.select = new SingleColumnSelect(type, paths, distinct);
        return update(structure);
    }

    public Where0<T, Object[]> selectDistinct(Collection<Path<T, ?>> paths) {
        return selectDistinct(Expressions.toExpressionList(paths));
    }

    public Where0<T, Object[]> select(Collection<Path<T, ?>> paths) {
        return select(Expressions.toExpressionList(paths));
    }

    public Where0<T, Object[]> selectDistinct(List<? extends ExpressionHolder<T, ?>> expressions) {
        return select(true, expressions);
    }

    public Where0<T, Object[]> select(List<? extends ExpressionHolder<T, ?>> expressions) {
        return select(false, expressions);
    }

    public Where0<T, Object[]> select(boolean distinct, List<? extends ExpressionHolder<T, ?>> expressions) {
        QueryStructureImpl structure = queryStructure.copy();
        structure.select = new MultiColumnSelect(expressions.stream()
                .map(ExpressionHolder::expression)
                .collect(Collectors.toList()), distinct);
        return update(structure);
    }

    public <R> Where0<T, R> selectDistinct(ExpressionHolder<T, R> paths) {
        return select(true, paths);
    }

    public <R> Where0<T, R> select(ExpressionHolder<T, R> paths) {
        return select(false, paths);
    }

    public <R> Where0<T, R> select(boolean distinct, ExpressionHolder<T, R> paths) {
        QueryStructureImpl structure = queryStructure.copy();
        Expression expression = paths.expression();
        Class<?> type = Object.class;
        structure.select = new SingleColumnSelect(type, expression, distinct);
        return update(structure);
    }

    protected Class<?> getType(Path<?, ?> path) {
        Class<?> fromClause = queryStructure.from().type();
        String name = Util.getReferenceMethodName(path);
        Method method;
        try {
            method = fromClause.getDeclaredMethod(name);
        } catch (NoSuchMethodException e) {
            throw new BeanReflectiveException(e);
        }
        return method.getReturnType();
    }

    static class AndBuilderImpl<T, U> implements AndBuilder0<T, U>, AbstractCollector<U> {
        private final QueryConditionBuilder<T, U> queryBuilder;
        protected final BooleanExpression<T> base;


        public AndBuilderImpl(QueryConditionBuilder<T, U> queryBuilder, BooleanExpression<T> base) {
            this.queryBuilder = queryBuilder;
            this.base = base;
        }

        @Override
        public <N> PathOperator<T, N, AndBuilder0<T, U>> and(Path<T, N> path) {
            PathOperatorImpl<T, N, AndOperator<T>> and = (PathOperatorImpl<T, N, AndOperator<T>>) base.and(path);
            return new PathOperatorImpl<>(and.base(), this::newAndBuilder);
        }

        @NotNull
        private AndBuilderImpl<T, U> newAndBuilder(TypedExpression<?, ?> baseExpression) {
            return new AndBuilderImpl<>(queryBuilder, TypeCastUtil.unsafeCast(baseExpression));
        }

        @Override
        public <N extends Number & Comparable<N>> NumberOperator<T, N, AndBuilder0<T, U>> and(NumberPath<T, N> path) {
            NumberOperatorImpl<T, N, AndOperator<T>> and = (NumberOperatorImpl<T, N, AndOperator<T>>) base.and(path);
            return new NumberOperatorImpl<>(and.base(), this::newAndBuilder);
        }

        @Override
        public <N extends Comparable<N>> ComparableOperator<T, N, AndBuilder0<T, U>> and(ComparablePath<T, N> path) {
            ComparableOperatorImpl<T, N, AndOperator<T>> and = (ComparableOperatorImpl<T, N, AndOperator<T>>) base.and(path);
            return new ComparableOperatorImpl<>(and.base(), this::newAndBuilder);
        }

        @Override
        public AndBuilder0<T, U> and(BooleanPath<T> path) {
            BooleanExpression<T> and = (BooleanExpression<T>) base.and(path);
            return new AndBuilderImpl<>(queryBuilder, and);
        }

        @Override
        public StringOperator<T, AndBuilder0<T, U>> and(StringPath<T> path) {
            StringOperatorImpl<T, AndOperator<T>> and = (StringOperatorImpl<T, AndOperator<T>>) base.and(path);
            return new StringOperatorImpl<>(and.base(), this::newAndBuilder);
        }

        @Override
        public AndBuilder0<T, U> and(ExpressionHolder<T, Boolean> predicate) {
            BooleanExpression<T> and = (BooleanExpression<T>) base.and(predicate);
            return new AndBuilderImpl<>(queryBuilder, and);
        }

        private QueryConditionBuilder<T, U> getQueryBuilder() {
            QueryStructureImpl structure = queryBuilder.queryStructure().copy();
            structure.where = base.expression();
            return queryBuilder.update(structure);
        }

        @Override
        public Collector<U> orderBy(List<? extends Order<T>> orders) {
            return getQueryBuilder().orderBy(orders);
        }

        @Override
        public OrderOperator<T, U> orderBy(Collection<Path<T, Comparable<?>>> paths) {
            return getQueryBuilder().orderBy(paths);
        }

        @Override
        public long count() {
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
}
