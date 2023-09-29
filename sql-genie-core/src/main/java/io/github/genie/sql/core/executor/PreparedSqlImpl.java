package io.github.genie.sql.core.executor;

import io.github.genie.sql.core.executor.JdbcQueryExecutor.ColumnProjection;

import java.util.List;

public record PreparedSqlImpl(String sql, List<?> args,
                              List<ColumnProjection> projectionPaths)
        implements JdbcQueryExecutor.PreparedSql {
}
