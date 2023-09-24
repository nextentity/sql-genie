package io.github.genie.sql.core.executor;

import java.util.List;

public record PreparedSqlImpl(String sql, List<?> args,
                              List<JdbcQueryExecutor.ColumnProjection> projectionPaths)
        implements JdbcQueryExecutor.PreparedSql {
}
