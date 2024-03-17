package io.github.genie.sql.executor.jdbc;

import io.github.genie.sql.api.Lists;
import io.github.genie.sql.api.Update;
import io.github.genie.sql.api.Updater;
import io.github.genie.sql.builder.UpdaterImpl;
import io.github.genie.sql.builder.exception.OptimisticLockException;
import io.github.genie.sql.builder.exception.SqlExecuteException;
import io.github.genie.sql.builder.exception.TransactionRequiredException;
import io.github.genie.sql.builder.meta.Attribute;
import io.github.genie.sql.builder.meta.BasicAttribute;
import io.github.genie.sql.builder.meta.EntityType;
import io.github.genie.sql.builder.meta.Metamodel;
import io.github.genie.sql.executor.jdbc.ConnectionProvider.ConnectionCallback;
import io.github.genie.sql.executor.jdbc.JdbcUpdateSqlBuilder.PreparedSql;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

@Slf4j
public class JdbcUpdate implements Update {

    private final JdbcUpdateSqlBuilder sqlBuilder;
    private final ConnectionProvider connectionProvider;
    private final Metamodel metamodel;

    public JdbcUpdate(JdbcUpdateSqlBuilder sqlBuilder,
                      ConnectionProvider connectionProvider,
                      Metamodel metamodel) {
        this.sqlBuilder = sqlBuilder;
        this.connectionProvider = connectionProvider;
        this.metamodel = metamodel;
    }

    @Override
    public <T> List<T> insert(List<T> entities, Class<T> entityType) {
        EntityType entity = metamodel.getEntity(entityType);
        PreparedSql sql = sqlBuilder.buildInsert(entity);
        return execute(connection -> doInsert(entities, entity, connection, sql));
    }

    @Override
    public <T> List<T> update(List<T> entities, Class<T> entityType) {
        PreparedSql preparedSql = sqlBuilder.buildUpdate(metamodel.getEntity(entityType));
        execute(connection -> {
            String sql = preparedSql.sql();
            log.debug(sql);
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                setArgs(entities, preparedSql.columns(), statement);
                int[] updateRowCounts = statement.executeBatch();
                List<BasicAttribute> bindAttributes = preparedSql.versionColumns();
                boolean hasVersion = isNotEmpty(bindAttributes);
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
        return entities;
    }

    @Override
    public <T> void delete(Iterable<T> entities, Class<T> entityType) {
        PreparedSql preparedSql = sqlBuilder.buildDelete(metamodel.getEntity(entityType));
        execute(connection -> {
            String sql = preparedSql.sql();
            log.debug(sql);
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                setArgs(entities, preparedSql.columns(), statement);
                int[] result = statement.executeBatch();
                if (log.isDebugEnabled()) {
                    log.debug("executeBatch result: " + Arrays.toString(result));
                }
                return null;
            }
        });
    }

    private static boolean isNotEmpty(List<?> list) {
        return list != null && !list.isEmpty();
    }

    @Override
    public <T> T updateNonNullColumn(T entity, Class<T> entityType) {
        EntityType meta = metamodel.getEntity(entityType);

        List<BasicAttribute> nonNullColumn;
        nonNullColumn = getNonNullColumn(entity, meta);
        if (nonNullColumn.isEmpty()) {
            log.warn("no field to update");
            return entity;
        }
        PreparedSql preparedSql = sqlBuilder.buildUpdate(meta, nonNullColumn);
        Attribute version = meta.version();
        Object versionValue = version.get(entity);
        if (versionValue == null) {
            throw new IllegalArgumentException("version field must not be null");
        }
        return execute(connection -> {
            String sql = preparedSql.sql();
            log.debug(sql);
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                setArgs(Lists.of(entity), preparedSql.columns(), statement);
                int i = statement.executeUpdate();
                List<BasicAttribute> versions = preparedSql.versionColumns();
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
            return entity;
        });
    }

    @Override
    public <T> Updater<T> getUpdater(Class<T> type) {
        return new UpdaterImpl<>(this, type);
    }

    private static void setNewVersion(Object entity, List<BasicAttribute> versions) {
        for (BasicAttribute column : versions) {
            Object version = column.get(entity);
            if (version instanceof Integer) {
                version = (Integer) version + 1;
            } else if (version instanceof Long) {
                version = (Long) version + 1;
            } else {
                throw new IllegalStateException();
            }
            column.set(entity, version);
        }
    }

    private static <T> List<BasicAttribute> getNonNullColumn(T entity, EntityType entityType) {
        List<BasicAttribute> columns = new ArrayList<>();
        for (Attribute attribute : entityType.attributes()) {
            if (attribute instanceof BasicAttribute) {
                BasicAttribute column = (BasicAttribute) attribute;
                Object invoke = column.get(entity);
                if (invoke != null) {
                    columns.add(column);
                }
            }
        }
        return columns;
    }

    private <T> List<T> doInsert(List<T> entities,
                                 EntityType entityType,
                                 Connection connection,
                                 PreparedSql preparedSql)
            throws SQLException {
        String sql = preparedSql.sql();
        log.debug(sql);
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            List<BasicAttribute> columns = preparedSql.columns();
            setArgs(entities, columns, statement);
            statement.executeBatch();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                Iterator<T> iterator = entities.iterator();
                while (keys.next()) {
                    T entity = iterator.next();
                    Attribute idField = entityType.id();
                    Object key = JdbcUtil.getValue(keys, 1, idField.javaType());
                    idField.set(entity, key);
                }
            }
        }
        return entities;
    }

    private static <T> void setArgs(Iterable<T> entities,
                                    List<BasicAttribute> columns,
                                    PreparedStatement statement)
            throws SQLException {
        for (T entity : entities) {
            int i = 0;
            for (BasicAttribute column : columns) {
                Object v = column.get(entity);
                if (v instanceof Enum<?>) {
                    v = ((Enum<?>) v).ordinal();
                }
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
