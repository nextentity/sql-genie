package io.github.genie.sql.builder;

import io.github.genie.sql.api.Expression;
import io.github.genie.sql.api.ExpressionHolder;

public interface ExpressionHolders {

    static <T, U> ExpressionHolder<T, U> of(Expression expression) {
        return TypeCastUtil.cast(expression);
    }

    static <T, U> ExpressionHolder<T, U> ofTrue() {
        return of(Expressions.TRUE);
    }

}
