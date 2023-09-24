package io.github.genie.sql.core.executor;

import io.github.genie.sql.core.QueryMetadata;
import io.github.genie.sql.core.mapping.MappingFactory;

public class MySqlSqlBuilder implements JdbcQueryExecutor.SqlBuilder {
    @Override
    public JdbcQueryExecutor.PreparedSql build(QueryMetadata metadata, MappingFactory mappings) {
        return new SqlEditor(metadata, metadata.fromClause(), mappings).build();
    }



}
