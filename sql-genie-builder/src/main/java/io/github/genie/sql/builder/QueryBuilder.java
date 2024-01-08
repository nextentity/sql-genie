package io.github.genie.sql.builder;

import io.github.genie.sql.api.Column;
import io.github.genie.sql.api.Expression;
import io.github.genie.sql.api.ExpressionHolder;
import io.github.genie.sql.api.ExpressionOperator.PathOperator;
import io.github.genie.sql.api.ExpressionOperator.Predicate;
import io.github.genie.sql.api.Path;
import io.github.genie.sql.api.Query.Fetch;
import io.github.genie.sql.api.Query.Select;
import io.github.genie.sql.api.Query.Where0;
import io.github.genie.sql.api.QueryExecutor;
import io.github.genie.sql.builder.QueryStructures.MultiColumnSelect;
import io.github.genie.sql.builder.QueryStructures.QueryStructureImpl;
import io.github.genie.sql.builder.QueryStructures.SelectClauseImpl;
import io.github.genie.sql.builder.QueryStructures.SingleColumnSelect;
import io.github.genie.sql.builder.exception.BeanReflectiveException;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class QueryBuilder<T> extends QueryConditionBuilder<T, T> implements Select<T>, Fetch<T> {
    public QueryBuilder(QueryExecutor queryExecutor, Class<T> type) {
        super(queryExecutor, type);
    }

    public QueryBuilder(QueryExecutor queryExecutor, Class<T> type, QueryStructurePostProcessor structurePostProcessor) {
        super(queryExecutor, type, structurePostProcessor);
    }

    public Where0<T, T> fetch(List<PathOperator<T, ?, Predicate<T>>> expressions) {
        QueryStructureImpl metadata = queryStructure.copy();
        List<Column> list = new ArrayList<>(expressions.size());
        for (PathOperator<T, ?, Predicate<T>> expression : expressions) {
            Expression expr = expression.expression();
            if (expr instanceof Column column) {
                list.add(column);
            }
        }
        metadata.fetch = list;
        return update(metadata);
    }

    public Where0<T, T> fetch(Collection<Path<T, ?>> paths) {
        return fetch(Expressions.toExpressionList(paths));
    }

    public <R> Where0<T, R> select(Class<R> projectionType) {
        QueryStructureImpl metadata = queryStructure.copy();
        metadata.select = new SelectClauseImpl(projectionType);
        return update(metadata);
    }


    public <R> Where0<T, R> select(Path<T, ? extends R> expression) {
        QueryStructureImpl metadata = queryStructure.copy();
        Expression paths = Expressions.of(expression);
        Class<?> type = getType(expression);
        metadata.select = new SingleColumnSelect(type, paths);
        return update(metadata);
    }

    public Where0<T, Object[]> select(Collection<Path<T, ?>> paths) {
        return select(Expressions.toExpressionList(paths));
    }

    public Where0<T, Object[]> select(List<? extends ExpressionHolder<T, ?>> expressions) {
        QueryStructureImpl metadata = queryStructure.copy();
        metadata.select = new MultiColumnSelect(expressions.stream().map(ExpressionHolder::expression).toList());
        return update(metadata);
    }

    public <R> Where0<T, R> select(ExpressionHolder<T, R> paths) {
        QueryStructureImpl metadata = queryStructure.copy();
        Expression expression = paths.expression();
        Class<?> type = Object.class;
        metadata.select = new SingleColumnSelect(type, expression);
        return update(metadata);
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
}
