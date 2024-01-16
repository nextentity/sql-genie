package io.github.genie.sql.api;

import io.github.genie.sql.api.Path.BooleanPath;
import io.github.genie.sql.api.Path.ComparablePath;
import io.github.genie.sql.api.Path.NumberPath;
import io.github.genie.sql.api.Path.StringPath;
import io.github.genie.sql.api.TypedExpression.BooleanExpression;
import io.github.genie.sql.api.TypedExpression.ComparableExpression;
import io.github.genie.sql.api.TypedExpression.NumberExpression;
import io.github.genie.sql.api.TypedExpression.PathExpression;
import io.github.genie.sql.api.TypedExpression.StringExpression;

public interface EntityRoot<T> {

    <U> ExpressionHolder<T, U> of(U value);

    <U> PathExpression<T, U> get(Path<T, U> path);

    StringExpression<T> get(StringPath<T> path);

    <U extends Number & Comparable<U>> NumberExpression<T, U> get(NumberPath<T, U> path);

    <U extends Comparable<U>> ComparableExpression<T, U> get(ComparablePath<T, U> path);

    BooleanExpression<T> get(BooleanPath<T> path);

    default <U extends Number & Comparable<U>> NumberExpression<T, U> min(NumberPath<T, U> path) {
        return get(path).min();
    }

    default <U extends Number & Comparable<U>> NumberExpression<T, U> max(NumberPath<T, U> path) {
        return get(path).max();
    }

    default <U extends Number & Comparable<U>> NumberExpression<T, U> sum(NumberPath<T, U> path) {
        return get(path).sum();
    }

    default <U extends Number & Comparable<U>> NumberExpression<T, U> avg(NumberPath<T, U> path) {
        return get(path).avg();
    }

    default NumberExpression<T, Long> count(Path<T, ?> path) {
        return get(path).count();
    }

}
