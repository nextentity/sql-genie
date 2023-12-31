package io.github.genie.sql.test;

import io.github.genie.sql.api.LockModeType;
import io.github.genie.sql.api.Query.QueryStructureBuilder;
import io.github.genie.sql.api.QueryStructure;
import io.github.genie.sql.builder.QueryBuilder;
import io.github.genie.sql.builder.QueryStructurePostProcessor;
import org.junit.jupiter.api.Assertions;

import java.util.List;

public class TestPostProcessor implements QueryStructurePostProcessor {

    @Override
    public QueryStructure preListQuery(QueryBuilder<?, ?> builder, QueryStructure queryStructure) {
        QueryStructureBuilder b = builder.buildMetadata();
        boolean exist = !builder.queryList(b.exist(-1)).isEmpty();
        int count = builder.<Number>queryList(b.count()).get(0).intValue();
        List<?> list = builder.queryList(b.getList(-1, -1, LockModeType.NONE));
        Assertions.assertEquals(list.size(), count);
        Assertions.assertEquals(exist, !list.isEmpty());
        return QueryStructurePostProcessor.super.preListQuery(builder, queryStructure);
    }
}
