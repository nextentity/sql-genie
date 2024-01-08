package io.github.genie.sql.api;

import java.io.Serializable;

public interface From extends Serializable {

    Class<?> type();

    interface Entity extends From {

    }

    interface SubQuery extends From {
        QueryStructure queryStructure();

        @Override
        default Class<?> type() {
            return queryStructure().select().resultType();
        }
    }

}
