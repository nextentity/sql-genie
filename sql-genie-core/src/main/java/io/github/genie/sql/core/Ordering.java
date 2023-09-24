package io.github.genie.sql.core;

public interface Ordering<T> {

    Expression.Meta expression();

    SortOrder order();

    enum SortOrder {
        ASC, DESC
    }
}
