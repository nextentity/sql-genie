package io.github.genie.sql.builder;

import io.github.genie.sql.api.Expression;
import io.github.genie.sql.api.ExpressionBuilder;
import io.github.genie.sql.api.Order;
import io.github.genie.sql.api.Path;
import io.github.genie.sql.builder.DefaultExpressionOperator.RootImpl;
import io.github.genie.sql.api.ExpressionOperator.ComparableOperator;
import io.github.genie.sql.api.ExpressionOperator.NumberOperator;
import io.github.genie.sql.api.ExpressionOperator.PathOperator;
import io.github.genie.sql.api.ExpressionOperator.Predicate;
import io.github.genie.sql.api.ExpressionOperator.Root;
import io.github.genie.sql.api.ExpressionOperator.StringOperator;
import io.github.genie.sql.builder.QueryStructures.OrderImpl;
import io.github.genie.sql.api.Path.BooleanPath;
import io.github.genie.sql.api.Path.ComparablePath;
import io.github.genie.sql.api.Path.NumberPath;
import io.github.genie.sql.api.Path.StringPath;

import java.util.Arrays;
import java.util.List;

import static io.github.genie.sql.api.Operator.AND;
import static io.github.genie.sql.api.Operator.NOT;
import static io.github.genie.sql.api.Operator.OR;
import static io.github.genie.sql.api.Order.SortOrder.ASC;
import static io.github.genie.sql.api.Order.SortOrder.DESC;

public final class Q {

    public static <T> Root<T> of() {
        return new RootImpl<>();
    }

    public static <T, U> PathOperator<T, U, Predicate<T>> get(Path<T, U> path) {
        return Q.<T>of().get(path);
    }

    public static <T> StringOperator<T, Predicate<T>> get(StringPath<T> path) {
        return Q.<T>of().get(path);
    }

    public static <T, U extends Number & Comparable<U>>
    NumberOperator<T, U, Predicate<T>> get(NumberPath<T, U> path) {
        return Q.<T>of().get(path);
    }

    public static <T, V extends Comparable<V>>
    ComparableOperator<T, V, Predicate<T>> get(ComparablePath<T, V> path) {
        return Q.<T>of().get(path);
    }

    public static <T> Predicate<T> get(BooleanPath<T> path) {
        return Q.<T>of().get(path);
    }


    public static <T, E extends Number & Comparable<E>>
    NumberOperator<T, E, Predicate<T>> min(NumberPath<T, E> path) {
        return get(path).min();
    }

    public static <T, V extends Number & Comparable<V>>
    NumberOperator<T, V, Predicate<T>> max(NumberPath<T, V> path) {
        return get(path).max();
    }

    public static <T, E extends Number & Comparable<E>>
    NumberOperator<T, E, Predicate<T>> sum(NumberPath<T, E> path) {
        return get(path).sum();
    }

    public static <T, E extends Number & Comparable<E>>
    NumberOperator<T, E, Predicate<T>> avg(NumberPath<T, E> path) {
        return get(path).avg();
    }

    public static <T>
    NumberOperator<T, Integer, Predicate<T>> count(Path<T, ?> path) {
        return get(path).count();
    }

    @SafeVarargs
    public static <T> Predicate<T> and(ExpressionBuilder<T, Boolean> predicate,
                                       ExpressionBuilder<T, Boolean>... predicates) {
        List<Expression> metas = Arrays.stream(predicates).map(ExpressionBuilder::build).toList();
        Expression meta = ExpressionBuilders.operate(predicate.build(), AND, metas);
        return DefaultExpressionOperator.ofBoolOps(meta);
    }

    @SafeVarargs
    public static <T> Predicate<T> or(ExpressionBuilder<T, Boolean> predicate,
                                      ExpressionBuilder<T, Boolean>... predicates) {
        List<Expression> metas = Arrays.stream(predicates).map(ExpressionBuilder::build).toList();
        Expression meta = ExpressionBuilders.operate(predicate.build(), OR, metas);
        return DefaultExpressionOperator.ofBoolOps(meta);
    }

    public static <T> Order<T> desc(Path<T, ? extends Comparable<?>> path) {
        return new OrderImpl<>(ExpressionBuilders.of(path), DESC);
    }

    public static <T> Order<T> asc(Path<T, ? extends Comparable<?>> path) {
        return new OrderImpl<>(ExpressionBuilders.of(path), ASC);
    }

    @SafeVarargs
    public static <T> List<Order<T>> desc(Path<T, ? extends Comparable<?>>... paths) {
        return Arrays.stream(paths).map(Q::desc).toList();
    }

    @SafeVarargs
    public static <T> List<Order<T>> asc(Path<T, Comparable<?>>... paths) {
        return Arrays.stream(paths).map(Q::asc).toList();
    }


    public static <T> Predicate<T> not(ExpressionBuilder<T, Boolean> lt) {
        Expression meta = ExpressionBuilders.operate(lt.build(), NOT);
        return DefaultExpressionOperator.ofBoolOps(meta);
    }

    private Q() {
    }
}
