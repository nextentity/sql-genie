package io.github.genie.sql.core;

import io.github.genie.sql.core.Expression.Meta;

public interface Ordering<T> {

    Meta meta();

    SortOrder order();

    enum SortOrder {
        ASC, DESC
    }
}
