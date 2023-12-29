package io.github.genie.sql.core;

import io.github.genie.sql.core.ExpressionBuilder.Expression;

import java.io.Serializable;

public interface Ordering<T> extends Serializable {

    Expression meta();

    SortOrder order();

    enum SortOrder {
        ASC, DESC
    }
}
