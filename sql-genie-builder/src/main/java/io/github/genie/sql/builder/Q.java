package io.github.genie.sql.builder;

import io.github.genie.sql.api.Expression;
import io.github.genie.sql.api.ExpressionBuilder;
import io.github.genie.sql.api.ExpressionBuilder.OperableBoolean;
import io.github.genie.sql.api.ExpressionBuilder.OperableComparable;
import io.github.genie.sql.api.ExpressionBuilder.OperableNumber;
import io.github.genie.sql.api.ExpressionBuilder.OperablePath;
import io.github.genie.sql.api.ExpressionBuilder.OperableString;
import io.github.genie.sql.api.ExpressionHolder;
import io.github.genie.sql.api.ExpressionOperator.Predicate;
import io.github.genie.sql.api.Order;
import io.github.genie.sql.api.Order.SortOrder;
import io.github.genie.sql.api.Path;
import io.github.genie.sql.api.Path.BooleanPath;
import io.github.genie.sql.api.Path.ComparablePath;
import io.github.genie.sql.api.Path.NumberPath;
import io.github.genie.sql.api.Path.StringPath;
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

public final class Q {

    public static <T> ExpressionBuilder<T> of() {
        return new ExpressionBuilderImpl<>();
    }

    public static <T, U> OperablePath<T, U> get(Path<T, U> path) {
        return Q.<T>of().get(path);
    }

    public static <T> OperableString<T> get(StringPath<T> path) {
        return Q.<T>of().get(path);
    }

    public static <T, U extends Number & Comparable<U>>
    OperableNumber<T, U> get(NumberPath<T, U> path) {
        return Q.<T>of().get(path);
    }

    public static <T, V extends Comparable<V>> OperableComparable<T, V> get(ComparablePath<T, V> path) {
        return Q.<T>of().get(path);
    }

    public static <T> OperableBoolean<T> get(BooleanPath<T> path) {
        return Q.<T>of().get(path);
    }

    public static <T, E extends Number & Comparable<E>> OperableNumber<T, E> min(NumberPath<T, E> path) {
        return get(path).min();
    }

    public static <T, E extends Number & Comparable<E>> OperableNumber<T, E> max(NumberPath<T, E> path) {
        return get(path).max();
    }

    public static <T, E extends Number & Comparable<E>> OperableNumber<T, E> sum(NumberPath<T, E> path) {
        return get(path).sum();
    }

    public static <T, E extends Number & Comparable<E>> OperableNumber<T, E> avg(NumberPath<T, E> path) {
        return get(path).avg();
    }

    public static <T> OperableNumber<T, Integer> count(Path<T, ?> path) {
        return get(path).count();
    }

    @SafeVarargs
    public static <T> Predicate<T> and(ExpressionHolder<T, Boolean> predicate,
                                       ExpressionHolder<T, Boolean>... predicates) {
        List<Expression> metas = Arrays.stream(predicates)
                .map(ExpressionHolder::expression)
                .collect(Collectors.toList());
        Expression expression = Expressions.operate(predicate.expression(), AND, metas);
        return DefaultExpressionOperator.ofBoolOps(expression);
    }

    @SafeVarargs
    public static <T> Predicate<T> or(ExpressionHolder<T, Boolean> predicate,
                                      ExpressionHolder<T, Boolean>... predicates) {
        List<Expression> metas = Arrays.stream(predicates)
                .map(ExpressionHolder::expression)
                .collect(Collectors.toList());
        Expression expression = Expressions.operate(predicate.expression(), OR, metas);
        return DefaultExpressionOperator.ofBoolOps(expression);
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

    public static <T> Predicate<T> not(ExpressionHolder<T, Boolean> lt) {
        Expression expression = Expressions.operate(lt.expression(), NOT);
        return DefaultExpressionOperator.ofBoolOps(expression);
    }

    private Q() {
    }
}
