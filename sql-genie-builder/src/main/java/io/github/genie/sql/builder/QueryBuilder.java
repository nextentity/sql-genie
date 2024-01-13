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
import io.github.genie.sql.builder.QueryStructures.MultiColumnSelect;
import io.github.genie.sql.builder.QueryStructures.QueryStructureImpl;
import io.github.genie.sql.builder.QueryStructures.SelectClauseImpl;
import io.github.genie.sql.builder.QueryStructures.SingleColumnSelect;
import io.github.genie.sql.builder.exception.BeanReflectiveException;

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

    public <R> Where0<T, R> select(Class<R> projectionType) {
        QueryStructureImpl structure = queryStructure.copy();
        structure.select = new SelectClauseImpl(projectionType);
        return update(structure);
    }

    public <R> Where0<T, R> select(Path<T, ? extends R> path) {
        QueryStructureImpl structure = queryStructure.copy();
        Expression paths = Expressions.of(path);
        Class<?> type = getType(path);
        structure.select = new SingleColumnSelect(type, paths);
        return update(structure);
    }

    public Where0<T, Object[]> select(Collection<Path<T, ?>> paths) {
        return select(Expressions.toExpressionList(paths));
    }

    public Where0<T, Object[]> select(List<? extends ExpressionHolder<T, ?>> expressions) {
        QueryStructureImpl structure = queryStructure.copy();
        structure.select = new MultiColumnSelect(expressions.stream()
                .map(ExpressionHolder::expression)
                .collect(Collectors.toList()));
        return update(structure);
    }

    public <R> Where0<T, R> select(ExpressionHolder<T, R> paths) {
        QueryStructureImpl structure = queryStructure.copy();
        Expression expression = paths.expression();
        Class<?> type = Object.class;
        structure.select = new SingleColumnSelect(type, expression);
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
}
