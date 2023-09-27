package io.github.genie.sql.core;

import io.github.genie.sql.core.Expression.TypedExpression;
import lombok.Data;

@Data
final class OrderingImpl<T> implements Ordering<T> {
    private final Expression.Meta expression;
    private final SortOrder order;

    OrderingImpl(TypedExpression<T, ?> expression, SortOrder order) {
        this.expression = expression.meta();
        this.order = order;
    }

    public OrderingImpl(Expression.Meta expression, SortOrder order) {
        this.expression = expression;
        this.order = order;
    }

    @Override
    public Expression.Meta expression() {
        return expression;
    }

    @Override
    public SortOrder order() {
        return order;
    }


}
