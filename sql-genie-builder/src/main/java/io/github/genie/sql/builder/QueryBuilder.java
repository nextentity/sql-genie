package io.github.genie.sql.builder;

import io.github.genie.sql.api.Column;
import io.github.genie.sql.api.Expression;
import io.github.genie.sql.api.ExpressionHolder;
import io.github.genie.sql.api.ExpressionHolder.ColumnHolder;
import io.github.genie.sql.api.Path;
import io.github.genie.sql.api.Query.Fetch;
import io.github.genie.sql.api.Query.Select;
import io.github.genie.sql.api.Query.Where0;
import io.github.genie.sql.api.QueryExecutor;
import io.github.genie.sql.api.Root;
import io.github.genie.sql.builder.QueryStructures.MultiColumnImpl;
import io.github.genie.sql.builder.QueryStructures.QueryStructureImpl;
import io.github.genie.sql.builder.QueryStructures.SelectClauseImpl;
import io.github.genie.sql.builder.QueryStructures.SingleColumnImpl;
import io.github.genie.sql.builder.exception.BeanReflectiveException;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
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

    @Override
    public Where0<T, Object[]> select(Function<Root<T>, List<? extends ExpressionHolder<T, ?>>> selectBuilder) {
        return select(selectBuilder.apply(RootImpl.of()));
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
        structure.select = new SingleColumnImpl(type, paths, distinct);
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

    @Override
    public Where0<T, Object[]> selectDistinct(Function<Root<T>, List<? extends ExpressionHolder<T, ?>>> selectBuilder) {
        return selectDistinct(selectBuilder.apply(RootImpl.of()));
    }

    public Where0<T, Object[]> select(List<? extends ExpressionHolder<T, ?>> expressions) {
        return select(false, expressions);
    }

    public Where0<T, Object[]> select(boolean distinct, List<? extends ExpressionHolder<T, ?>> expressions) {
        QueryStructureImpl structure = queryStructure.copy();
        structure.select = new MultiColumnImpl(expressions.stream()
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
        structure.select = new SingleColumnImpl(type, expression, distinct);
        return update(structure);
    }

    protected Class<?> getType(Path<?, ?> path) {
        return PathReference.of(path).getReturnType();
    }

    @Override
    public String toString() {
        return "QueryBuilder[" + queryExecutor.getClass().getSimpleName() + "]";
    }
}
