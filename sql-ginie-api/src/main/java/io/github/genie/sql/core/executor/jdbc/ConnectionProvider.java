package io.github.genie.sql.core.executor.jdbc;

import java.sql.Connection;
import java.sql.SQLException;

public interface ConnectionProvider {
    <T> T execute(ConnectionCallback<T> action) throws SQLException;

    interface ConnectionCallback<T> {
        T doInConnection(Connection connection) throws SQLException;
    }
}
