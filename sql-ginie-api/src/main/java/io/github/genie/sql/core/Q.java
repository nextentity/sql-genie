package io.github.genie.sql.core;

import io.github.genie.sql.core.ExpressionBuilder.Expression;
import io.github.genie.sql.core.DefaultExpressionBuilder.RootImpl;
import io.github.genie.sql.core.ExpressionOperator.*;
import io.github.genie.sql.core.Models.OrderingImpl;
import io.github.genie.sql.core.Path.BooleanPath;
import io.github.genie.sql.core.Path.ComparablePath;
import io.github.genie.sql.core.Path.NumberPath;
import io.github.genie.sql.core.Path.StringPath;

import java.util.Arrays;
import java.util.List;

import static io.github.genie.sql.core.Operator.*;
import static io.github.genie.sql.core.Ordering.SortOrder.ASC;
import static io.github.genie.sql.core.Ordering.SortOrder.DESC;

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
        Expression meta = Metas.operate(predicate.build(), AND, metas);
        return DefaultExpressionBuilder.ofBoolOps(meta);
    }

    @SafeVarargs
    public static <T> Predicate<T> or(ExpressionBuilder<T, Boolean> predicate,
                                      ExpressionBuilder<T, Boolean>... predicates) {
        List<Expression> metas = Arrays.stream(predicates).map(ExpressionBuilder::build).toList();
        Expression meta = Metas.operate(predicate.build(), OR, metas);
        return DefaultExpressionBuilder.ofBoolOps(meta);
    }

    public static <T> Ordering<T> desc(Path<T, ? extends Comparable<?>> path) {
        return new OrderingImpl<>(Metas.of(path), DESC);
    }

    public static <T> Ordering<T> asc(Path<T, ? extends Comparable<?>> path) {
        return new OrderingImpl<>(Metas.of(path), ASC);
    }

    @SafeVarargs
    public static <T> List<Ordering<T>> desc(Path<T, ? extends Comparable<?>>... paths) {
        return Arrays.stream(paths).map(Q::desc).toList();
    }

    @SafeVarargs
    public static <T> List<Ordering<T>> asc(Path<T, Comparable<?>>... paths) {
        return Arrays.stream(paths).map(Q::asc).toList();
    }


    public static <T> Predicate<T> not(ExpressionBuilder<T, Boolean> lt) {
        Expression meta = Metas.operate(lt.build(), NOT);
        return DefaultExpressionBuilder.ofBoolOps(meta);
    }

    private Q() {
    }
}
