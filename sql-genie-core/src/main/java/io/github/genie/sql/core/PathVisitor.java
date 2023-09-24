package io.github.genie.sql.core;

import io.github.genie.sql.core.OperateableExpression.BooleanExpression;
import io.github.genie.sql.core.OperateableExpression.ComparableExpression;
import io.github.genie.sql.core.OperateableExpression.NumberExpression;
import io.github.genie.sql.core.OperateableExpression.StringExpression;
import io.github.genie.sql.core.Path.BooleanPath;
import io.github.genie.sql.core.Path.ComparablePath;
import io.github.genie.sql.core.Path.NumberPath;

/**
 * @param <T> root type
 * @param <U> current type
 */
public interface PathVisitor<T, U> {

    <V, R extends PathVisitor<T, V> & OperateableExpression<T, V>> R get(Path<U, V> path);

    StringExpression<T> get(Path.StringPath<T> path);

    <V extends Number & Comparable<V>> NumberExpression<T, V> get(NumberPath<T, V> path);

    <V extends Comparable<V>> ComparableExpression<T, V> get(ComparablePath<T, V> path);

    BooleanExpression<T> get(BooleanPath<T> path);

    interface RootPath<T> extends PathVisitor<T, T> {

        default <E extends Number & Comparable<E>>
        NumberExpression<T, E> min(NumberPath<T, E> path) {
            return get(path).min();
        }

        default <E extends Number & Comparable<E>>
        NumberExpression<T, E> max(NumberPath<T, E> path) {
            return get(path).max();
        }

        default <E extends Number & Comparable<E>>
        NumberExpression<T, E> sum(NumberPath<T, E> path) {
            return get(path).sum();
        }


        default <E extends Number & Comparable<E>>
        NumberExpression<T, E> avg(NumberPath<T, E> path) {
            return get(path).avg();
        }

        default NumberExpression<T, Integer> count(Path<T, ?> path) {
            return get(path).count();
        }

    }
}
