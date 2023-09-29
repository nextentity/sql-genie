package io.github.genie.sql.core;

import io.github.genie.sql.core.ExpressionOps.NumberOps;
import io.github.genie.sql.core.ExpressionOps.PredicateOps;
import io.github.genie.sql.core.Path.ComparablePath;
import io.github.genie.sql.core.Path.NumberPath;
import io.github.genie.sql.core.Path.StringPath;

import java.util.Arrays;
import java.util.List;

public class Q {

    public static <T> ExpressionOps.RootPath<T> of() {
        return new ExpressionBuilder.RootImpl<>();
    }

    public static <T, V> ExpressionOps.PathOps<T, V, PredicateOps<T>> get(Path<T, V> path) {
        return Q.<T>of().get(path);
    }

    public static <T> ExpressionOps.StringOps<T, PredicateOps<T>> get(StringPath<T> path) {
        return Q.<T>of().get(path);
    }

    public static <T, V extends Number & Comparable<V>> NumberOps<T, V, PredicateOps<T>> get(NumberPath<T, V> path) {
        return Q.<T>of().get(path);
    }

    public static <T, V extends Comparable<V>> ExpressionOps.ComparableOps<T, V, PredicateOps<T>> get(ComparablePath<T, V> path) {
        return Q.<T>of().get(path);
    }

    public static <T> PredicateOps<T> get(Path.BooleanPath<T> path) {
        return Q.<T>of().get(path);
    }


    public static <T, E extends Number & Comparable<E>> NumberOps<T, E, PredicateOps<T>> min(NumberPath<T, E> path) {
        return get(path).min();
    }

    public static <T, V extends Number & Comparable<V>> NumberOps<T, V, PredicateOps<T>> max(NumberPath<T, V> path) {
        return get(path).max();
    }

    public static <T, E extends Number & Comparable<E>> NumberOps<T, E, PredicateOps<T>> sum(NumberPath<T, E> path) {
        return get(path).sum();
    }

    public static <T, E extends Number & Comparable<E>> NumberOps<T, E, PredicateOps<T>> avg(NumberPath<T, E> path) {
        return get(path).avg();
    }

    public static <T> NumberOps<T, Integer, PredicateOps<T>> count(Path<T, ?> path) {
        return get(path).count();
    }

    @SafeVarargs
    public static <T> PredicateOps<T> and(Predicate<T> predicate, Predicate<T>... predicates) {
        Expression.Meta meta = Metas.operate(predicate.meta(), Operator.AND, Arrays.stream(predicates).map(Expression::meta).toList());
        return ExpressionBuilder.ofBoolOps(meta);
    }

    @SafeVarargs
    public static <T> PredicateOps<T> or(Predicate<T> predicate, Predicate<T>... predicates) {
        Expression.Meta meta = Metas.operate(predicate.meta(), Operator.OR, Arrays.stream(predicates).map(Expression::meta).toList());
        return ExpressionBuilder.ofBoolOps(meta);
    }

    public static <T> Ordering<T> desc(Path<T, ? extends Comparable<?>> path) {
        return new Models.OrderingImpl<>(Metas.of(path), Ordering.SortOrder.DESC);
    }

    public static <T> Ordering<T> asc(Path<T, ? extends Comparable<?>> path) {
        return new Models.OrderingImpl<>(Metas.of(path), Ordering.SortOrder.ASC);
    }

    @SafeVarargs
    public static <T> List<Ordering<T>> desc(Path<T, ? extends Comparable<?>>... paths) {
        return Arrays.stream(paths)
                .map(Q::desc)
                .toList();
    }

    @SafeVarargs
    public static <T> List<Ordering<T>> asc(Path<T, Comparable<?>>... paths) {
        return Arrays.stream(paths)
                .map(Q::asc)
                .toList();
    }


    public static <T> PredicateOps<T> not(Predicate<T> lt) {
        Expression.Meta meta = Metas.operate(lt.meta(), Operator.NOT, List.of());
        return ExpressionBuilder.ofBoolOps(meta);
    }

}
