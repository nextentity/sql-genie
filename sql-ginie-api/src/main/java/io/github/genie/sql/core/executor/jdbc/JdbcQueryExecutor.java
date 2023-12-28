package io.github.genie.sql.core.executor.jdbc;

import io.github.genie.sql.core.QueryExecutor;
import io.github.genie.sql.core.QueryStructure;
import io.github.genie.sql.core.Selection;
import io.github.genie.sql.core.exception.SqlExecuteException;
import io.github.genie.sql.core.mapping.FieldMapping;
import io.github.genie.sql.core.mapping.MappingFactory;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Data
public class JdbcQueryExecutor implements QueryExecutor {

    @NotNull
    private MappingFactory mappings;
    @NotNull
    private QuerySqlBuilder sqlBuilder;
    @NotNull
    private ConnectionProvider connectionProvider;
    @NotNull
    private ResultCollector collector;

    @Override
    @NotNull
    public <R> List<R> getList(@NotNull QueryStructure queryMetadata) {
        PreparedSql sql = sqlBuilder.build(queryMetadata, mappings);
        try {
            return connectionProvider.execute(connection -> {
                // noinspection SqlSourceToSinkFlow
                try (PreparedStatement statement = connection.prepareStatement(sql.sql())) {
                    JdbcUtil.setParam(statement, sql.args());
                    if (log.isDebugEnabled()) {
                        log.debug("SQL: {}", sql.sql());
                        log.debug("ARGS: {}", sql.args());
                    }
                    try (ResultSet resultSet = statement.executeQuery()) {
                        return resolveResult(queryMetadata, sql, resultSet);
                    }
                }
            });
        } catch (SQLException e) {
            throw new SqlExecuteException(e);
        }
    }

    @NotNull
    private <T> List<T> resolveResult(QueryStructure queryMetadata,
                                      PreparedSql sql,
                                      ResultSet resultSet) throws SQLException {
        int type = resultSet.getType();
        List<T> result;
        if (type != ResultSet.TYPE_FORWARD_ONLY) {
            resultSet.last();
            int size = resultSet.getRow();
            result = new ArrayList<>(size);
            resultSet.beforeFirst();
        } else {
            result = new ArrayList<>();
        }
        while (resultSet.next()) {
            T row = collector.collect(resultSet,
                    queryMetadata.select(),
                    queryMetadata.from(),
                    sql.selectedFields());
            result.add(row);
        }
        return result;
    }


    public interface QuerySqlBuilder {
        PreparedSql build(QueryStructure metadata, MappingFactory mappings);

    }


    public interface PreparedSql {

        String sql();

        List<?> args();

        List<FieldMapping> selectedFields();

    }

    public interface ResultCollector {
        <R> R collect(@NotNull ResultSet resultSet,
                      @NotNull Selection selectClause,
                      @NotNull Class<?> fromType,
                      @NotNull List<? extends FieldMapping> projectionPaths)
                throws SQLException;

    }
}

