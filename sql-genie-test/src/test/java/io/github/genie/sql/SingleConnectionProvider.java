package io.github.genie.sql;

import io.github.genie.sql.executor.jdbc.ConnectionProvider;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.SQLException;

public class SingleConnectionProvider implements ConnectionProvider {
    public static final ConnectionProvider CONNECTION_PROVIDER = getSimpleConnectionProvider();
    private final Connection connection;

    @SneakyThrows
    @NotNull
    private static SingleConnectionProvider getSimpleConnectionProvider() {
        Connection connection = new DataSourceConfig().getMysqlDataSource().getConnection();
        return new SingleConnectionProvider(connection);
    }

    private SingleConnectionProvider(Connection connection) {
        this.connection = connection;
    }

    @Override
    public <T> T execute(ConnectionCallback<T> action) throws SQLException {
        return action.doInConnection(connection);
    }
}
