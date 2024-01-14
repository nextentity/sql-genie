package io.github.genie.sql;

import io.github.genie.sql.executor.jdbc.ConnectionProvider;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class SimpleConnectionProvider implements ConnectionProvider {
    private final DataSource dataSource;

    public SimpleConnectionProvider(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public <T> T execute(ConnectionCallback<T> action) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            return action.doInConnection(connection);
        }
    }
}
