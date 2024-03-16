package io.github.genie.sql.test;

import io.github.genie.sql.api.Query;
import io.github.genie.sql.api.Query.Select;
import io.github.genie.sql.test.entity.User;
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
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class UserQueryProvider implements ArgumentsProvider {
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
                    user = user.clone();
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
        Query query = new JpaQueryExecutor(manager, JpaMetamodel.of(), new MySqlQuerySqlBuilder())
                .createQuery(new TestPostProcessor());
        log.debug("create jpa query: " + query);
        return query.from(User.class);
    }

    @SneakyThrows
    private static Select<User> jdbc() {
        ConnectionProvider sqlExecutor = SingleConnectionProvider.CONNECTION_PROVIDER;
        Query query = new JdbcQueryExecutor(JpaMetamodel.of(),
                new MySqlQuerySqlBuilder(),
                sqlExecutor,
                new JdbcResultCollector()
        ).createQuery(new TestPostProcessor());
        log.debug("create jdbc query: " + query);
        return query.from(User.class);
    }
}
