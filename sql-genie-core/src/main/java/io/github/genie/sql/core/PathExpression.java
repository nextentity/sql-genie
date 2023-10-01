package io.github.genie.sql.core;

import io.github.genie.sql.core.Expression.TypedExpression;

public interface PathExpression<T, U> extends TypedExpression<T, U> {

    @Override
    Paths meta();

    <V> PathExpression<T, V> get(Path<U, V> path);


}
