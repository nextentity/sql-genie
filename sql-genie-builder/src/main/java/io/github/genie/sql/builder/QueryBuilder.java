package io.github.genie.sql.builder;

import io.github.genie.sql.api.Column;
import io.github.genie.sql.api.Expression;
import io.github.genie.sql.api.ExpressionBuilder;
import io.github.genie.sql.api.ExpressionHolder;
import io.github.genie.sql.api.ExpressionHolder.ColumnHolder;
import io.github.genie.sql.api.Lists;
import io.github.genie.sql.api.Path;
import io.github.genie.sql.api.Query.Fetch;
import io.github.genie.sql.api.Query.Select;
import io.github.genie.sql.api.Query.Where0;
import io.github.genie.sql.api.QueryExecutor;
import io.github.genie.sql.api.tuple.Tuple;
import io.github.genie.sql.api.tuple.Tuple10;
import io.github.genie.sql.api.tuple.Tuple2;
import io.github.genie.sql.api.tuple.Tuple3;
import io.github.genie.sql.api.tuple.Tuple4;
import io.github.genie.sql.api.tuple.Tuple5;
import io.github.genie.sql.api.tuple.Tuple6;
import io.github.genie.sql.api.tuple.Tuple7;
import io.github.genie.sql.api.tuple.Tuple8;
import io.github.genie.sql.api.tuple.Tuple9;
import io.github.genie.sql.builder.QueryStructures.MultiSelectedImpl;
import io.github.genie.sql.builder.QueryStructures.ProjectionSelectedImpl;
import io.github.genie.sql.builder.QueryStructures.QueryStructureImpl;
import io.github.genie.sql.builder.QueryStructures.SingleSelectedImpl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;


public class QueryBuilder<T> extends QueryConditionBuilder<T, T> implements Select<T>, Fetch<T> {

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
    public Where0<T, Tuple> select(ExpressionBuilder<T> selectBuilder) {
        return select(selectBuilder.apply(RootImpl.of()));
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
        if (projectionType == queryStructure.from.type()) {
            return update(queryStructure);
        }
        QueryStructureImpl structure = queryStructure.copy();
        structure.select = new ProjectionSelectedImpl(projectionType, distinct);
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
        structure.select = new SingleSelectedImpl(type, paths, distinct);
        return update(structure);
    }

    public Where0<T, Tuple> selectDistinct(Collection<Path<T, ?>> paths) {
        return selectDistinct(Expressions.toExpressionList(paths));
    }

    public Where0<T, Tuple> select(Collection<Path<T, ?>> paths) {
        return select(Expressions.toExpressionList(paths));
    }

    @Override
    public <A, B> Where0<T, Tuple2<A, B>> select(Path<T, A> a, Path<T, B> b) {
        return selectTuple(false, Lists.of(a, b));
    }

    @Override
    public <A, B, C> Where0<T, Tuple3<A, B, C>> select(Path<T, A> a, Path<T, B> b, Path<T, C> c) {
        return selectTuple(false, Lists.of(a, b, c));
    }

    @Override
    public <A, B, C, D> Where0<T, Tuple4<A, B, C, D>> select(Path<T, A> a, Path<T, B> b, Path<T, C> c, Path<T, D> d) {
        return selectTuple(false, Lists.of(a, b, c, d));
    }

    @Override
    public <A, B, C, D, E> Where0<T, Tuple5<A, B, C, D, E>> select(Path<T, A> a, Path<T, B> b, Path<T, C> c, Path<T, D> d, Path<T, E> e) {
        return selectTuple(false, Lists.of(a, b, c, d, e));
    }

    @Override
    public <A, B, C, D, E, F> Where0<T, Tuple6<A, B, C, D, E, F>>
    select(Path<T, A> a, Path<T, B> b, Path<T, C> c, Path<T, D> d, Path<T, E> e, Path<T, F> f) {
        return selectTuple(false, Lists.of(a, b, c, d, e, f));
    }

    @Override
    public <A, B, C, D, E, F, G> Where0<T, Tuple7<A, B, C, D, E, F, G>>
    select(Path<T, A> a, Path<T, B> b, Path<T, C> c, Path<T, D> d, Path<T, E> e, Path<T, F> f, Path<T, G> g) {
        return selectTuple(false, Lists.of(a, b, c, d, e, f, g));
    }

    @Override
    public <A, B, C, D, E, F, G, H> Where0<T, Tuple8<A, B, C, D, E, F, G, H>>
    select(Path<T, A> a, Path<T, B> b, Path<T, C> c, Path<T, D> d, Path<T, E> e, Path<T, F> f, Path<T, G> g, Path<T, H> h) {
        return selectTuple(false, Lists.of(a, b, c, d, e, f, g, h));
    }

    @Override
    public <A, B, C, D, E, F, G, H, I> Where0<T, Tuple9<A, B, C, D, E, F, G, H, I>>
    select(Path<T, A> a, Path<T, B> b, Path<T, C> c, Path<T, D> d, Path<T, E> e, Path<T, F> f, Path<T, G> g, Path<T, H> h, Path<T, I> i) {
        return selectTuple(false, Lists.of(a, b, c, d, e, f, g, h, i));
    }

    @Override
    public <A, B, C, D, E, F, G, H, I, J> Where0<T, Tuple10<A, B, C, D, E, F, G, H, I, J>>
    select(Path<T, A> a, Path<T, B> b, Path<T, C> c, Path<T, D> d, Path<T, E> e, Path<T, F> f, Path<T, G> g, Path<T, H> h, Path<T, I> i, Path<T, J> j) {
        return selectTuple(false, Lists.of(a, b, c, d, e, f, g, h, i, j));
    }

    public Where0<T, Tuple> selectDistinct(List<? extends ExpressionHolder<T, ?>> expressions) {
        return select(true, expressions);
    }

    @Override
    public Where0<T, Tuple> selectDistinct(ExpressionBuilder<T> selectBuilder) {
        return selectDistinct(selectBuilder.apply(RootImpl.of()));
    }

    @Override
    public <A, B> Where0<T, Tuple2<A, B>> selectDistinct(Path<T, A> a, Path<T, B> b) {
        return selectTuple(true, Lists.of(a, b));
    }

    @Override
    public <A, B, C> Where0<T, Tuple3<A, B, C>> selectDistinct(Path<T, A> a, Path<T, B> b, Path<T, C> c) {
        return selectTuple(true, Lists.of(a, b, c));
    }

    @Override
    public <A, B, C, D> Where0<T, Tuple4<A, B, C, D>> selectDistinct(Path<T, A> a, Path<T, B> b, Path<T, C> c, Path<T, D> d) {
        return selectTuple(true, Lists.of(a, b, c, d));
    }

    @Override
    public <A, B, C, D, E> Where0<T, Tuple5<A, B, C, D, E>> selectDistinct(Path<T, A> a, Path<T, B> b, Path<T, C> c, Path<T, D> d, Path<T, E> e) {
        return selectTuple(true, Lists.of(a, b, c, d, e));
    }

    @Override
    public <A, B, C, D, E, F> Where0<T, Tuple6<A, B, C, D, E, F>>
    selectDistinct(Path<T, A> a, Path<T, B> b, Path<T, C> c, Path<T, D> d, Path<T, E> e, Path<T, F> f) {
        return selectTuple(true, Lists.of(a, b, c, d, e, f));
    }

    @Override
    public <A, B, C, D, E, F, G> Where0<T, Tuple7<A, B, C, D, E, F, G>>
    selectDistinct(Path<T, A> a, Path<T, B> b, Path<T, C> c, Path<T, D> d, Path<T, E> e, Path<T, F> f, Path<T, G> g) {
        return selectTuple(true, Lists.of(a, b, c, d, e, f, g));
    }

    @Override
    public <A, B, C, D, E, F, G, H> Where0<T, Tuple8<A, B, C, D, E, F, G, H>>
    selectDistinct(Path<T, A> a, Path<T, B> b, Path<T, C> c, Path<T, D> d, Path<T, E> e, Path<T, F> f, Path<T, G> g, Path<T, H> h) {
        return selectTuple(true, Lists.of(a, b, c, d, e, f, g, h));
    }

    @Override
    public <A, B, C, D, E, F, G, H, I> Where0<T, Tuple9<A, B, C, D, E, F, G, H, I>>
    selectDistinct(Path<T, A> a, Path<T, B> b, Path<T, C> c, Path<T, D> d, Path<T, E> e, Path<T, F> f, Path<T, G> g, Path<T, H> h, Path<T, I> i) {
        return selectTuple(true, Lists.of(a, b, c, d, e, f, g, h, i));
    }

    @Override
    public <A, B, C, D, E, F, G, H, I, J> Where0<T, Tuple10<A, B, C, D, E, F, G, H, I, J>>
    selectDistinct(Path<T, A> a, Path<T, B> b, Path<T, C> c, Path<T, D> d, Path<T, E> e, Path<T, F> f, Path<T, G> g, Path<T, H> h, Path<T, I> i, Path<T, J> j) {
        return selectTuple(true, Lists.of(a, b, c, d, e, f, g, h, i, j));
    }

    public Where0<T, Tuple> select(List<? extends ExpressionHolder<T, ?>> expressions) {
        return select(false, expressions);
    }

    public Where0<T, Tuple> select(boolean distinct, List<? extends ExpressionHolder<T, ?>> expressions) {
        QueryStructureImpl structure = queryStructure.copy();
        List<Expression> selectExpressions = expressions.stream()
                .map(ExpressionHolder::expression)
                .collect(Collectors.toList());
        structure.select = new MultiSelectedImpl(selectExpressions, distinct);
        return update(structure);
    }

    public <R extends Tuple> Where0<T, R> selectTuple(boolean distinct, List<? extends Path<T, ?>> paths) {
        QueryStructureImpl structure = queryStructure.copy();
        List<Expression> selectExpressions = paths.stream()
                .map(Expressions::of)
                .collect(Collectors.toList());
        structure.select = new MultiSelectedImpl(selectExpressions, distinct);
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
        structure.select = new SingleSelectedImpl(type, expression, distinct);
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
