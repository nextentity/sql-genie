package io.github.genie.sql.executor.jdbc;

import io.github.genie.sql.api.LockModeType;
import io.github.genie.sql.api.QueryStructure;
import io.github.genie.sql.builder.AbstractQueryExecutor;
import io.github.genie.sql.builder.exception.SqlExecuteException;
import io.github.genie.sql.builder.exception.TransactionRequiredException;
import io.github.genie.sql.builder.meta.Attribute;
import io.github.genie.sql.builder.meta.Metamodel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Slf4j
@Getter
@Setter
public class JdbcQueryExecutor implements AbstractQueryExecutor {

    @NotNull
    private Metamodel metamodel;
    @NotNull
    private QuerySqlBuilder sqlBuilder;
    @NotNull
    private ConnectionProvider connectionProvider;
    @NotNull
    private ResultCollector collector;

    public JdbcQueryExecutor(@NotNull Metamodel metamodel, @NotNull QuerySqlBuilder sqlBuilder, @NotNull ConnectionProvider connectionProvider, @NotNull ResultCollector collector) {
        this.metamodel = metamodel;
        this.sqlBuilder = sqlBuilder;
        this.connectionProvider = connectionProvider;
        this.collector = collector;
    }

    @Override
    @NotNull
    public <R> List<R> getList(@NotNull QueryStructure queryStructure) {
        PreparedSql sql = sqlBuilder.build(queryStructure, metamodel);
        printSql(sql);
        try {
            return connectionProvider.execute(connection -> {
                LockModeType locked = queryStructure.lockType();
                if (locked != null && locked != LockModeType.NONE && connection.getAutoCommit()) {
                    throw new TransactionRequiredException();
                }
                // noinspection SqlSourceToSinkFlow
                try (PreparedStatement statement = connection.prepareStatement(sql.sql())) {
                    JdbcUtil.setParam(statement, sql.args());
                    try (ResultSet resultSet = statement.executeQuery()) {
                        return collector.resolve(resultSet, sql.selected(), queryStructure);
                    }
                }
            });
        } catch (SQLException e) {
            throw new SqlExecuteException(e);
        }
    }

    private static void printSql(PreparedSql sql) {
        log.debug("SQL: {}", sql.sql());
        if (!sql.args().isEmpty()) {
            log.debug("ARGS: {}", sql.args());
        }
    }

    public interface QuerySqlBuilder {
        PreparedSql build(QueryStructure structure, Metamodel metamodel);

    }

    public interface PreparedSql {

        String sql();

        List<?> args();

        List<Attribute> selected();

    }

    public interface ResultCollector {
        <T> List<T> resolve(ResultSet resultSet, List<? extends Attribute> selected, QueryStructure structure) throws SQLException;
    }
}

