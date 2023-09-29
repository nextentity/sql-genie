package io.github.genie.sql.core.executor;

import io.github.genie.sql.core.QueryMetadata;
import io.github.genie.sql.core.executor.JdbcQueryExecutor.PreparedSql;
import io.github.genie.sql.core.executor.JdbcQueryExecutor.SqlBuilder;
import io.github.genie.sql.core.mapping.MappingFactory;

public class MySqlSqlBuilder implements SqlBuilder {
    @Override
    public PreparedSql build(QueryMetadata metadata, MappingFactory mappings) {
        return new SqlEditor(metadata, metadata.fromClause(), mappings).build();
    }


}
