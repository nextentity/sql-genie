package io.github.genie.sql.core.executor;

import io.github.genie.sql.core.executor.JdbcQueryExecutor.PreparedSql;
import io.github.genie.sql.core.mapping.FieldMapping;

import java.util.List;

public record PreparedSqlImpl(String sql, List<?> args, List<FieldMapping> selectedFields) implements PreparedSql {
}
