package io.github.genie.sql.core.executor;

import io.github.genie.sql.core.*;
import io.github.genie.sql.core.executor.JdbcQueryExecutor.PreparedSql;
import io.github.genie.sql.core.executor.JdbcQueryExecutor.SqlBuilder;
import io.github.genie.sql.core.mapping.*;

public class MySqlSqlBuilder implements SqlBuilder {
    @Override
    public PreparedSql build(QueryMetadata metadata, MappingFactory mappings) {
        return new SqlEditor(metadata, metadata.from(), mappings).build();
    }


}
