package io.github.genie.sql.core;

public interface Ordering<T> {

    Expression.Meta meta();

    SortOrder order();

    enum SortOrder {
        ASC, DESC
    }
}
