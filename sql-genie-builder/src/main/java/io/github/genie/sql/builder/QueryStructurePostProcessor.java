package io.github.genie.sql.builder;

import io.github.genie.sql.api.QueryStructure;

public interface QueryStructurePostProcessor {

    QueryStructurePostProcessor NONE = new QueryStructurePostProcessor() {
    };

    default QueryStructure preCountQuery(QueryConditionBuilder<?, ?> builder, QueryStructure queryStructure) {
        return queryStructure;
    }

    default QueryStructure preListQuery(QueryConditionBuilder<?, ?> builder, QueryStructure queryStructure) {
        return queryStructure;
    }

    default QueryStructure preExistQuery(QueryConditionBuilder<?, ?> builder, QueryStructure queryStructure) {
        return queryStructure;
    }

}
