package io.github.genie.sql.test;

import io.github.genie.sql.api.Updater;
import io.github.genie.sql.test.entity.User;
import io.github.genie.sql.executor.jdbc.JdbcUpdate;
import io.github.genie.sql.executor.jdbc.MySqlQuerySqlBuilder;
import io.github.genie.sql.executor.jdbc.MysqlUpdateSqlBuilder;
import io.github.genie.sql.executor.jpa.JpaQueryExecutor;
import io.github.genie.sql.executor.jpa.JpaUpdate;
import io.github.genie.sql.meta.JpaMetamodel;
import javax.persistence.EntityManager;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;

import java.util.stream.Stream;

public class UserUpdaterProvider implements ArgumentsProvider {
    public static final Updater<User> jdbc = jdbc();
    public static final Updater<User> jpa = jpa();

    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext extensionContext) {
        EntityManagers.getEntityManager().clear();
        return Stream.of(
                Arguments.of(jdbc),
                Arguments.of(jpa)
        );
    }

    private static Updater<User> jdbc() {
        JdbcUpdate jdbcUpdate = new JdbcUpdate(
                new MysqlUpdateSqlBuilder(),
                SingleConnectionProvider.CONNECTION_PROVIDER,
                JpaMetamodel.of()
        );
        return jdbcUpdate.getUpdater(User.class);
    }

    private static Updater<User> jpa() {
        EntityManager em = EntityManagers.getEntityManager();
        MySqlQuerySqlBuilder sqlBuilder = new MySqlQuerySqlBuilder();
        JpaQueryExecutor jpaQueryExecutor = new JpaQueryExecutor(em, JpaMetamodel.of(), sqlBuilder);
        return new JpaUpdate(em, jpaQueryExecutor).getUpdater(User.class);
    }
}
