package io.github.genie.sql;

import com.mysql.cj.jdbc.MysqlDataSource;
import io.github.genie.sql.api.Query;
import io.github.genie.sql.api.Query.Select;
import io.github.genie.sql.entity.User;
import io.github.genie.sql.executor.jdbc.ConnectionProvider;
import io.github.genie.sql.executor.jdbc.JdbcQueryExecutor;
import io.github.genie.sql.executor.jdbc.JdbcResultCollector;
import io.github.genie.sql.executor.jdbc.MySqlQuerySqlBuilder;
import io.github.genie.sql.executor.jpa.JpaQueryExecutor;
import io.github.genie.sql.meta.JpaMetamodel;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class UserDaoProvider implements ArgumentsProvider {
    public static final Select<User> jdbc = jdbc();
    public static final Select<User> jpa = jpa();

    @Getter(lazy = true)
    @Accessors(fluent = true)
    private static final List<User> users = loadAllUsers();

    private static List<User> loadAllUsers() {
        EntityManager manager = EntityManagers.getEntityManager();
        CriteriaBuilder builder = manager.getCriteriaBuilder();
        CriteriaQuery<User> query = builder.createQuery(User.class);
        Root<User> root = query.from(User.class);
        query.orderBy(builder.asc(root.get("id")));
        List<User> list = manager.createQuery(query).getResultList();
        Map<Integer, User> map = list.stream().collect(Collectors.toMap(User::getId, Function.identity()));
        return list.stream()
                .map(user -> {
                    user = new User(user);
                    Integer pid = user.getPid();
                    if (pid != null) {
                        User p = map.get(pid);
                        user.setParentUser(p);
                    }
                    return user;
                })
                .collect(Collectors.toList());
    }

    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext extensionContext) {
        return Stream.of(
                Arguments.of(jdbc),
                Arguments.of(jpa)
        );
    }

    private static Select<User> jpa() {
        EntityManager manager = EntityManagers.getEntityManager();
        return new JpaQueryExecutor(manager, new JpaMetamodel(), new MySqlQuerySqlBuilder())
                .createQuery(/*new TestPostProcessor()*/)
                .from(User.class);
    }

    private static Select<User> jdbc() {
        DataSourceConfig config = new DataSourceConfig();
        MysqlDataSource source = config.getMysqlDataSource();
        ConnectionProvider sqlExecutor = new SimpleConnectionProvider(source);
        Query query = new JdbcQueryExecutor(new JpaMetamodel(),
                new MySqlQuerySqlBuilder(),
                sqlExecutor,
                new JdbcResultCollector()
        ).createQuery(/*new TestPostProcessor()*/);

        return query.from(User.class);
    }
}
