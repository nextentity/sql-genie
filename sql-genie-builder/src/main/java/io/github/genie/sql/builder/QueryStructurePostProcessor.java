package io.github.genie.sql.builder;

import io.github.genie.sql.api.QueryStructure;

public interface QueryStructurePostProcessor {

    QueryStructurePostProcessor NONE = new QueryStructurePostProcessor() {
    };

    default QueryStructure preCountQuery(QueryBuilder<?, ?> builder, QueryStructure queryStructure) {
        return queryStructure;
    }

    default QueryStructure preListQuery(QueryBuilder<?, ?> builder, QueryStructure queryStructure) {
        return queryStructure;
    }


    default QueryStructure preExistQuery(QueryBuilder<?, ?> builder, QueryStructure queryStructure) {
        return queryStructure;
    }


}
