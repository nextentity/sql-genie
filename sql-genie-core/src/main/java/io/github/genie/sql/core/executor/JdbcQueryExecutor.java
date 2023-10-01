package io.github.genie.sql.core.executor;

import io.github.genie.sql.core.QueryExecutor;
import io.github.genie.sql.core.QueryMetadata;
import io.github.genie.sql.core.SelectClause;
import io.github.genie.sql.core.exception.SqlExecuteException;
import io.github.genie.sql.core.mapping.FieldMapping;
import io.github.genie.sql.core.mapping.MappingFactory;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Data
public class JdbcQueryExecutor implements QueryExecutor {

    @NotNull
    private MappingFactory mappings;
    @NotNull
    private SqlBuilder sqlBuilder;
    @NotNull
    private SqlExecutor sqlExecutor;
    @NotNull
    private ResultCollector collector;

    @Override
    @NotNull
    public <R> List<R> getList(@NotNull QueryMetadata queryMetadata) {
        try {
            PreparedSql sql = sqlBuilder.build(queryMetadata, mappings);
            ResultSet resultSet = sqlExecutor.execute(sql.sql(), sql.args());
            try (resultSet) {
                List<R> result = new ArrayList<>();
                while (resultSet.next()) {
                    R row = collector.collect(resultSet,
                            queryMetadata.select(),
                            queryMetadata.from(),
                            sql.projectionPaths());
                    result.add(row);
                }
                return result;
            }
        } catch (SQLException e) {
            throw new SqlExecuteException(e);
        }
    }


    public interface SqlBuilder {
        PreparedSql build(QueryMetadata metadata, MappingFactory mappings);

    }


    public interface PreparedSql {

        String sql();

        List<?> args();

        List<FieldMapping> projectionPaths();

    }

    public interface SqlExecutor {
        ResultSet execute(String sql, List<?> args) throws SQLException;
    }

    public interface ResultCollector {
        <R> R collect(@NotNull ResultSet resultSet,
                      @NotNull SelectClause selectClause,
                      @NotNull Class<?> fromType,
                      @NotNull List<? extends FieldMapping> projectionPaths)
                throws SQLException;

    }
}

