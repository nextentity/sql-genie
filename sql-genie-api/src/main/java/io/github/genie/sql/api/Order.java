package io.github.genie.sql.api;

import java.io.Serializable;

@SuppressWarnings("unused")
public interface Order<T> extends Serializable {

    Expression expression();

    SortOrder order();

    enum SortOrder {
        ASC, DESC
    }
}
