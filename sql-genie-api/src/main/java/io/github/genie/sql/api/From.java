package io.github.genie.sql.api;

public interface From {

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
