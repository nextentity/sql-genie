package io.github.genie.sql.builder;

import io.github.genie.sql.api.Query;
import io.github.genie.sql.api.QueryExecutor;

public interface AbstractQueryExecutor extends QueryExecutor {
    default Query createQuery() {
        return createQuery(QueryStructurePostProcessor.NONE);
    }

    default Query createQuery(QueryStructurePostProcessor structurePostProcessor) {
        return new QueryImpl(this, structurePostProcessor);
    }

}
