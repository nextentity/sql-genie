package io.github.genie.sql.builder;

import io.github.genie.sql.api.Expression;
import io.github.genie.sql.api.ExpressionHolder;
import io.github.genie.sql.api.EntityRoot;
import io.github.genie.sql.api.Order;
import io.github.genie.sql.api.Order.SortOrder;
import io.github.genie.sql.api.Path;
import io.github.genie.sql.api.Path.BooleanPath;
import io.github.genie.sql.api.Path.ComparablePath;
import io.github.genie.sql.api.Path.NumberPath;
import io.github.genie.sql.api.Path.StringPath;
import io.github.genie.sql.api.TypedExpression.BooleanExpression;
import io.github.genie.sql.api.TypedExpression.ComparableExpression;
import io.github.genie.sql.api.TypedExpression.NumberExpression;
import io.github.genie.sql.api.TypedExpression.PathExpression;
import io.github.genie.sql.api.TypedExpression.StringExpression;
import io.github.genie.sql.builder.QueryStructures.OrderImpl;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static io.github.genie.sql.api.Operator.AND;
import static io.github.genie.sql.api.Operator.NOT;
import static io.github.genie.sql.api.Operator.OR;
import static io.github.genie.sql.api.Order.SortOrder.ASC;
import static io.github.genie.sql.api.Order.SortOrder.DESC;
import static io.github.genie.sql.builder.EntityRootImpl.ofBooleanExpression;

public final class Q {

    public static <T> EntityRoot<T> of() {
        return EntityRootImpl.of();
    }

    public static <T, U> PathExpression<T, U> get(Path<T, U> path) {
        return Q.<T>of().get(path);
    }

    public static <T> StringExpression<T> get(StringPath<T> path) {
        return Q.<T>of().get(path);
    }

    public static <T, U extends Number & Comparable<U>>
    NumberExpression<T, U> get(NumberPath<T, U> path) {
        return Q.<T>of().get(path);
    }

    public static <T, V extends Comparable<V>> ComparableExpression<T, V> get(ComparablePath<T, V> path) {
        return Q.<T>of().get(path);
    }

    public static <T> BooleanExpression<T> get(BooleanPath<T> path) {
        return Q.<T>of().get(path);
    }

    public static <T, E extends Number & Comparable<E>> NumberExpression<T, E> min(NumberPath<T, E> path) {
        return Q.<T>of().min(path);
    }

    public static <T, E extends Number & Comparable<E>> NumberExpression<T, E> max(NumberPath<T, E> path) {
        return Q.<T>of().max(path);
    }

    public static <T, E extends Number & Comparable<E>> NumberExpression<T, E> sum(NumberPath<T, E> path) {
        return Q.<T>of().sum(path);
    }

    public static <T, E extends Number & Comparable<E>> NumberExpression<T, E> avg(NumberPath<T, E> path) {
        return Q.<T>of().avg(path);
    }

    public static <T> NumberExpression<T, Integer> count(Path<T, ?> path) {
        return Q.<T>of().count(path);
    }

    @SafeVarargs
    public static <T> BooleanExpression<T> and(ExpressionHolder<T, Boolean> predicate,
                                               ExpressionHolder<T, Boolean>... predicates) {
        List<Expression> metas = Arrays.stream(predicates)
                .map(ExpressionHolder::expression)
                .collect(Collectors.toList());
        Expression expression = Expressions.operate(predicate.expression(), AND, metas);
        return ofBooleanExpression(expression);
    }

    @SafeVarargs
    public static <T> BooleanExpression<T> or(ExpressionHolder<T, Boolean> predicate,
                                              ExpressionHolder<T, Boolean>... predicates) {
        List<Expression> metas = Arrays.stream(predicates)
                .map(ExpressionHolder::expression)
                .collect(Collectors.toList());
        Expression expression = Expressions.operate(predicate.expression(), OR, metas);
        return ofBooleanExpression(expression);
    }

    public static <T> Order<T> desc(Path<T, ? extends Comparable<?>> path) {
        return orderBy(path, DESC);
    }

    public static <T> Order<T> asc(Path<T, ? extends Comparable<?>> path) {
        return orderBy(path, ASC);
    }

    @NotNull
    public static <T> Order<T> orderBy(Path<T, ? extends Comparable<?>> path, SortOrder sortOrder) {
        return new OrderImpl<>(Expressions.of(path), sortOrder);
    }

    @SafeVarargs
    public static <T> List<Order<T>> desc(Path<T, ? extends Comparable<?>>... paths) {
        return Arrays.stream(paths)
                .map(Q::desc)
                .collect(Collectors.toList());
    }

    @SafeVarargs
    public static <T> List<Order<T>> asc(Path<T, Comparable<?>>... paths) {
        return Arrays.stream(paths)
                .map(Q::asc)
                .collect(Collectors.toList());
    }

    public static <T> Order<T> orderBy(ExpressionHolder<T, ? extends Comparable<?>> expression, SortOrder order) {
        return new OrderImpl<>(expression.expression(), order);
    }

    public static <T> BooleanExpression<T> not(ExpressionHolder<T, Boolean> lt) {
        Expression expression = Expressions.operate(lt.expression(), NOT);
        return ofBooleanExpression(expression);
    }

    private Q() {
    }
}
