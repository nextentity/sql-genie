package io.github.genie.sql.core;

import io.github.genie.sql.core.exception.OptimisticLockException;
import io.github.genie.sql.core.exception.SqlExecuteException;
import io.github.genie.sql.core.exception.TransactionRequiredException;
import io.github.genie.sql.core.executor.jdbc.ConnectionProvider;
import io.github.genie.sql.core.executor.jdbc.ConnectionProvider.ConnectionCallback;
import io.github.genie.sql.core.executor.jdbc.JdbcUpdateSqlBuilder;
import io.github.genie.sql.core.executor.jdbc.JdbcUpdateSqlBuilder.PreparedSql;
import io.github.genie.sql.core.executor.jdbc.JdbcUtil;
import io.github.genie.sql.core.mapping.ColumnMapping;
import io.github.genie.sql.core.mapping.FieldMapping;
import io.github.genie.sql.core.mapping.MappingFactory;
import io.github.genie.sql.core.mapping.TableMapping;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Slf4j
public class JdbcUpdate implements Update {

    private final JdbcUpdateSqlBuilder sqlBuilder;
    private final ConnectionProvider connectionProvider;
    private final MappingFactory mappingFactory;

    public JdbcUpdate(JdbcUpdateSqlBuilder sqlBuilder,
                      ConnectionProvider connectionProvider,
                      MappingFactory mappingFactory) {
        this.sqlBuilder = sqlBuilder;
        this.connectionProvider = connectionProvider;
        this.mappingFactory = mappingFactory;
    }

    @Override
    public <T> List<T> insert(List<T> entities, Class<T> entityType) {
        TableMapping mapping = mappingFactory.getMapping(entityType);
        PreparedSql sql = sqlBuilder.buildInsert(mapping);
        return execute(connection -> doInsert(entities, mapping, connection, sql));
    }

    @Override
    public <T> void update(List<T> entities, Class<T> entityType) {
        PreparedSql preparedSql = sqlBuilder.buildUpdate(mappingFactory.getMapping(entityType));
        execute(connection -> {
            String sql = preparedSql.sql();
            log.debug(sql);
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                setArgs(entities, preparedSql.columns(), statement);
                int[] updateRowCounts = statement.executeBatch();
                List<ColumnMapping> columnMappings = preparedSql.versionColumns();
                boolean hasVersion = isNotEmpty(columnMappings);
                for (int rowCount : updateRowCounts) {
                    if (rowCount != 1) {
                        if (hasVersion) {
                            throw new OptimisticLockException("id not found or concurrent modified");
                        } else {
                            throw new IllegalStateException("id not found");
                        }
                    }
                }
                if (hasVersion) {
                    for (T entity : entities) {
                        setNewVersion(entity, preparedSql.versionColumns());
                    }
                }
                return null;
            }
        });
    }

    private static boolean isNotEmpty(List<?> columnMappings) {
        return columnMappings != null && !columnMappings.isEmpty();
    }

    @Override
    public <T> void updateNonNullColumn(T entity, Class<T> entityType) {
        TableMapping mapping = mappingFactory.getMapping(entityType);

        List<ColumnMapping> nonNullColumn;
        nonNullColumn = getNonNullColumn(entity, mapping);
        if (nonNullColumn.isEmpty()) {
            log.warn("no field to update");
            return;
        }
        PreparedSql preparedSql = sqlBuilder.buildUpdate(mapping, nonNullColumn);
        FieldMapping version = mapping.version();
        Object versionValue = version.invokeGetter(entity);
        if (versionValue == null) {
            throw new IllegalArgumentException("version field must not be null");
        }
        execute(connection -> {
            String sql = preparedSql.sql();
            log.debug(sql);
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                setArgs(List.of(entity), preparedSql.columns(), statement);
                int i = statement.executeUpdate();
                List<ColumnMapping> versions = preparedSql.versionColumns();
                boolean hasVersion = isNotEmpty(versions);
                if (i == 0) {
                    if (hasVersion) {
                        throw new OptimisticLockException("id not found or concurrent modified");
                    } else {
                        throw new IllegalStateException("id not found");
                    }
                } else if (i != 1) {
                    throw new IllegalStateException("update rows error: " + i);
                }
                if (hasVersion) {
                    setNewVersion(entity, versions);
                }
            }
            return true;
        });
    }

    private static void setNewVersion(Object entity, List<ColumnMapping> versions) {
        for (ColumnMapping column : versions) {
            Object version = column.invokeGetter(entity);
            if (version instanceof Integer) {
                version = (Integer) version + 1;
            } else if (version instanceof Long) {
                version = (Long) version + 1;
            } else {
                throw new IllegalStateException();
            }
            column.invokeSetter(entity, version);
        }
    }

    private static <T> List<ColumnMapping> getNonNullColumn(T entity, TableMapping mapping) {
        List<ColumnMapping> columns = new ArrayList<>();
        for (FieldMapping it : mapping.fields()) {
            if (it instanceof ColumnMapping column) {
                Object invoke = column.invokeGetter(entity);
                if (invoke != null) {
                    columns.add(column);
                }
            }
        }
        return columns;
    }


    private <T> List<T> doInsert(List<T> entities,
                                 TableMapping tableMapping,
                                 Connection connection,
                                 PreparedSql preparedSql)
            throws SQLException {
        String sql = preparedSql.sql();
        log.debug(sql);
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            List<ColumnMapping> columns = preparedSql.columns();
            setArgs(entities, columns, statement);
            statement.executeBatch();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                Iterator<T> iterator = entities.iterator();
                while (keys.next()) {
                    T entity = iterator.next();
                    FieldMapping idField = tableMapping.id();
                    Object key = JdbcUtil.getValue(keys, 1, idField.javaType());
                    idField.invokeSetter(entity, key);
                }
            }
        }
        return entities;
    }

    private static <T> void setArgs(List<T> entities,
                                    List<ColumnMapping> columns,
                                    PreparedStatement statement)
            throws SQLException {
        for (T entity : entities) {
            int i = 0;
            for (ColumnMapping column : columns) {
                Object v = column.invokeGetter(entity);
                statement.setObject(++i, v);
            }
            statement.addBatch();
        }
    }

    private <T> T execute(ConnectionCallback<T> action) {
        try {
            return connectionProvider.execute(connection -> {
                if (connection.getAutoCommit()) {
                    throw new TransactionRequiredException();
                }
                return action.doInConnection(connection);
            });
        } catch (SQLException e) {
            throw new SqlExecuteException(e);
        }
    }

}
