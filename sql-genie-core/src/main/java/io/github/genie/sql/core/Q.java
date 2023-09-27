package io.github.genie.sql.core;

import io.github.genie.sql.core.OperateableExpression.BooleanExpression;
import io.github.genie.sql.core.OperateableExpression.ComparableExpression;
import io.github.genie.sql.core.OperateableExpression.NumberExpression;
import io.github.genie.sql.core.OperateableExpression.StringExpression;

import java.util.Arrays;
import java.util.List;

public class Q {

    public static <T> PathVisitor.RootPath<T> of() {
        return ExpressionBuilders.of();
    }

    public static <T, V, R extends PathVisitor<T, V> & OperateableExpression<T, V>>
    R get(Path<T, V> path) {
        return Q.<T>of().get(path);
    }

    public static <T> StringExpression<T> get(Path.StringPath<T> path) {
        return () -> BasicExpressions.of(path);
    }

    public static <T, V extends Number & Comparable<V>>
    NumberExpression<T, V> get(Path.NumberPath<T, V> path) {
        return () -> BasicExpressions.of(path);
    }

    public static <T, V extends Comparable<V>>
    ComparableExpression<T, V> get(Path.ComparablePath<T, V> path) {
        return () -> BasicExpressions.of(path);
    }

    public static <T> BooleanExpression<T> get(Path.BooleanPath<T> path) {
        return () -> BasicExpressions.of(path);
    }


    public static <T, E extends Number & Comparable<E>> NumberExpression<T, E> min(Path.NumberPath<T, E> path) {
        return get(path).min();
    }

    public static <T, E extends Number & Comparable<E>> NumberExpression<T, E> max(Path.NumberPath<T, E> path) {
        return get(path).max();
    }

    public static <T, E extends Number & Comparable<E>> NumberExpression<T, E> sum(Path.NumberPath<T, E> path) {
        return get(path).sum();
    }

    public static <T, E extends Number & Comparable<E>> NumberExpression<T, E> avg(Path.NumberPath<T, E> path) {
        return get(path).avg();
    }

    public static <T> NumberExpression<T, Integer> count(Path<T, ?> path) {
        return get(path).count();
    }

    @SafeVarargs
    public static <T> BooleanExpression<T> and(Predicate<T> predicate, Predicate<T>... predicates) {
        return () -> BasicExpressions.operate(predicate, Operator.AND, List.of(predicates));
    }

    @SafeVarargs
    public static <T> BooleanExpression<T> or(Predicate<T> predicate, Predicate<T>... predicates) {
        return () -> BasicExpressions.operate(predicate, Operator.OR, List.of(predicates));
    }

    public static <T> Ordering<T> desc(Path<T, ? extends Comparable<?>> path) {
        return new OrderingImpl<>(Metas.of(path), Ordering.SortOrder.DESC);
    }

    public static <T> Ordering<T> asc(Path<T, ? extends Comparable<?>> path) {
        return new OrderingImpl<>(Metas.of(path), Ordering.SortOrder.ASC);
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


    public static <T> BooleanExpression<T> not(Predicate<T> lt) {
        return () -> BasicExpressions.operate(lt, Operator.NOT, List.of());
    }

}
