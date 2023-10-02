package io.github.genie.sql.core;

import io.github.genie.sql.core.Expression.Meta;
import io.github.genie.sql.core.ExpressionBuilder.RootImpl;
import io.github.genie.sql.core.ExpressionOps.*;
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

    public static <T, U> PathExpr<T, U> get(Path<T, U> path) {
        return Q.<T>of().get(path);
    }

    public static <T> StringExpr<T> get(StringPath<T> path) {
        return Q.<T>of().get(path);
    }

    public static <T, U extends Number & Comparable<U>>
    NumberExpr<T, U> get(NumberPath<T, U> path) {
        return Q.<T>of().get(path);
    }

    public static <T, V extends Comparable<V>>
    ComparableExpr<T, V> get(ComparablePath<T, V> path) {
        return Q.<T>of().get(path);
    }

    public static <T> Predicate<T> get(BooleanPath<T> path) {
        return Q.<T>of().get(path);
    }


    public static <T, E extends Number & Comparable<E>>
    NumberOps<T, E, Predicate<T>> min(NumberPath<T, E> path) {
        return get(path).min();
    }

    public static <T, V extends Number & Comparable<V>>
    NumberOps<T, V, Predicate<T>> max(NumberPath<T, V> path) {
        return get(path).max();
    }

    public static <T, E extends Number & Comparable<E>>
    NumberOps<T, E, Predicate<T>> sum(NumberPath<T, E> path) {
        return get(path).sum();
    }

    public static <T, E extends Number & Comparable<E>>
    NumberOps<T, E, Predicate<T>> avg(NumberPath<T, E> path) {
        return get(path).avg();
    }

    public static <T>
    NumberOps<T, Integer, Predicate<T>> count(Path<T, ?> path) {
        return get(path).count();
    }

    @SafeVarargs
    public static <T> Predicate<T> and(Expression<T, Boolean> predicate,
                                       Expression<T, Boolean>... predicates) {
        List<Meta> metas = Arrays.stream(predicates).map(Expression::meta).toList();
        Meta meta = Metas.operate(predicate.meta(), AND, metas);
        return ExpressionBuilder.ofBoolOps(meta);
    }

    @SafeVarargs
    public static <T> Predicate<T> or(Expression<T, Boolean> predicate,
                                      Expression<T, Boolean>... predicates) {
        List<Meta> metas = Arrays.stream(predicates).map(Expression::meta).toList();
        Meta meta = Metas.operate(predicate.meta(), OR, metas);
        return ExpressionBuilder.ofBoolOps(meta);
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


    public static <T> Predicate<T> not(Expression<T, Boolean> lt) {
        Meta meta = Metas.operate(lt.meta(), NOT);
        return ExpressionBuilder.ofBoolOps(meta);
    }

    private Q() {
    }
}
