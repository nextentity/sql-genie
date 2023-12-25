package io.github.genie.sql.core;

import io.github.genie.sql.core.Expression.Meta;

import java.io.Serializable;

public interface Ordering<T> extends Serializable {

    Meta meta();

    SortOrder order();

    enum SortOrder {
        ASC, DESC
    }
}
