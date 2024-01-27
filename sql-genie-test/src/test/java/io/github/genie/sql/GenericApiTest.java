package io.github.genie.sql;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mysql.cj.jdbc.MysqlDataSource;
import io.github.genie.sql.api.Expression;
import io.github.genie.sql.api.ExpressionHolder;
import io.github.genie.sql.api.LockModeType;
import io.github.genie.sql.api.Path;
import io.github.genie.sql.api.Query;
import io.github.genie.sql.api.Query.Select;
import io.github.genie.sql.api.QueryStructure;
import io.github.genie.sql.api.Slice;
import io.github.genie.sql.api.TypedExpression.BooleanExpression;
import io.github.genie.sql.entity.User;
import io.github.genie.sql.executor.jdbc.ConnectionProvider;
import io.github.genie.sql.executor.jdbc.JdbcQueryExecutor;
import io.github.genie.sql.executor.jdbc.JdbcResultCollector;
import io.github.genie.sql.executor.jdbc.JdbcUpdate;
import io.github.genie.sql.executor.jdbc.MySqlQuerySqlBuilder;
import io.github.genie.sql.executor.jdbc.MysqlUpdateSqlBuilder;
import io.github.genie.sql.meta.JpaMetamodel;
import io.github.genie.sql.projection.UserInterface;
import io.github.genie.sql.projection.UserModel;
import lombok.Lombok;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static io.github.genie.sql.builder.Q.and;
import static io.github.genie.sql.builder.Q.asc;
import static io.github.genie.sql.builder.Q.avg;
import static io.github.genie.sql.builder.Q.count;
import static io.github.genie.sql.builder.Q.desc;
import static io.github.genie.sql.builder.Q.get;
import static io.github.genie.sql.builder.Q.max;
import static io.github.genie.sql.builder.Q.min;
import static io.github.genie.sql.builder.Q.not;
import static io.github.genie.sql.builder.Q.or;
import static io.github.genie.sql.builder.Q.sum;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
public class GenericApiTest {

    protected static final String username = "Jeremy Keynes";

    protected static List<User> allUsers;
    protected static final MysqlDataSource dataSource = new DataSourceConfig().getMysqlDataSource();

    static {

        doInTransaction(connection -> {
            try {
                // noinspection SqlDialectInspection,SqlNoDataSourceInspection
                connection.createStatement().executeUpdate("update user set pid = null");
                ConnectionProvider connectionProvider = new ConnectionProvider() {
                    @Override
                    public <T> T execute(ConnectionCallback<T> action) throws SQLException {
                        return action.doInConnection(connection);
                    }
                };
                JpaMetamodel metamodel = new JpaMetamodel();
                Query query = new JdbcQueryExecutor(
                        metamodel,
                        new MySqlQuerySqlBuilder(),
                        connectionProvider,
                        new JdbcResultCollector()
                )
                        .createQuery();
                JdbcUpdate jdbcUpdate = new JdbcUpdate(
                        new MysqlUpdateSqlBuilder(),
                        connectionProvider,
                        metamodel);
                jdbcUpdate.delete(queryAllUsers(query), User.class);
                jdbcUpdate.insert(Users.getUsers(), User.class);
                allUsers = queryAllUsers(query);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private static List<User> queryAllUsers(Query query) {
        List<User> list = query.from(User.class).getList();
        Map<Integer, User> map = list.stream().collect(Collectors.toMap(User::getId, Function.identity()));
        for (User user : list) {
            Integer pid = user.getPid();
            if (pid != null) {
                User p = map.get(pid);
                user.setParentUser(p);
            }
        }
        return list;
    }

    private static void doInTransaction(Consumer<Connection> action) {
        Object o = doInTransaction(connection -> {
            action.accept(connection);
            return null;
        });
        log.trace("{}", o);
    }

    @SneakyThrows
    private static <T> T doInTransaction(Function<Connection, T> action) {
        Connection connection = dataSource.getConnection();
        T result;
        boolean autoCommit = connection.getAutoCommit();
        try {
            if (autoCommit) {
                connection.setAutoCommit(false);
            }
            result = action.apply(connection);
            connection.commit();
        } catch (Exception e) {
            connection.rollback();
            throw Lombok.sneakyThrow(e);
        } finally {
            if (autoCommit) {
                connection.setAutoCommit(true);
            }
        }

        return result;
    }

//    public GenericApiTest(Select<User> userQuery) {
//        this.userQuery = userQuery;
//    }

    @ParameterizedTest
    @ArgumentsSource(UserDaoProvider.class)
    public void testAndOr(Select<User> userQuery) {
        User single = userQuery
                .where(User::getId).eq(0)
                .getSingle();
        System.out.println(single);
        List<User> dbList = userQuery
                .where(User::getRandomNumber)
                .ne(1)
                .and(User::getRandomNumber)
                .gt(100)
                .and(User::getRandomNumber).ne(125)
                .and(User::getRandomNumber).le(666)
                .and(get(User::getRandomNumber).lt(106)
                        .or(User::getRandomNumber).gt(120)
                        .or(User::getRandomNumber).eq(109)
                )
                .and(User::getRandomNumber).ne(128)
                .getList();

        List<User> ftList = allUsers.stream()
                .filter(user -> user.getRandomNumber() != 1
                        && user.getRandomNumber() > 100
                        && user.getRandomNumber() != 125
                        && user.getRandomNumber() <= 666
                        && (user.getRandomNumber() < 106
                        || user.getRandomNumber() > 120
                        || user.getRandomNumber() == 109)
                        && user.getRandomNumber() != 128
                )
                .collect(Collectors.toList());

        assertEquals(dbList, ftList);
    }

    @ParameterizedTest
    @ArgumentsSource(UserDaoProvider.class)
    public void testAndOrChain(Select<User> userQuery) {
        User single = userQuery
                .where(User::getId).eq(0)
                .getSingle();
        System.out.println(single);
        List<User> dbList = userQuery
                .where(User::getRandomNumber).ne(1)
                .and(User::getRandomNumber).gt(100)
                .and(User::getRandomNumber).ne(125)
                .and(User::getRandomNumber).le(666)
                .and(get(User::getRandomNumber).lt(106)
                        .or(User::getRandomNumber).gt(120)
                        .or(User::getRandomNumber).eq(109)
                )
                .and(User::getRandomNumber).ne(128)
                .getList();

        List<User> ftList = allUsers.stream()
                .filter(user -> user.getRandomNumber() != 1
                        && user.getRandomNumber() > 100
                        && user.getRandomNumber() != 125
                        && user.getRandomNumber() <= 666
                        && (user.getRandomNumber() < 106
                        || user.getRandomNumber() > 120
                        || user.getRandomNumber() == 109)
                        && user.getRandomNumber() != 128
                )
                .collect(Collectors.toList());

        assertEquals(dbList, ftList);
    }

    @ParameterizedTest
    @ArgumentsSource(UserDaoProvider.class)
    public void testAndOrChan(Select<User> userQuery) {
        User single = userQuery
                .where(User::getId).eq(0)
                .getSingle();
        System.out.println(single);
        List<User> dbList = userQuery
                .where(User::getRandomNumber).ne(1)
                .and(User::getRandomNumber).gt(100)
                .and(User::getRandomNumber).ne(125)
                .and(User::getRandomNumber).le(666)
                .and(get(User::getRandomNumber).lt(106)
                        .or(User::getRandomNumber).gt(120)
                        .or(User::getRandomNumber).eq(109))
                .and(User::getRandomNumber).ne(128)
                .getList();

        List<User> ftList = allUsers.stream()
                .filter(user -> user.getRandomNumber() != 1
                        && user.getRandomNumber() > 100
                        && user.getRandomNumber() != 125
                        && user.getRandomNumber() <= 666
                        && (user.getRandomNumber() < 106
                        || user.getRandomNumber() > 120
                        || user.getRandomNumber() == 109)
                        && user.getRandomNumber() != 128
                )
                .collect(Collectors.toList());

        assertEquals(dbList, ftList);
    }

    @ParameterizedTest
    @ArgumentsSource(UserDaoProvider.class)
    public void testAndOr2(Select<User> userQuery) {
        User single = userQuery
                .where(get(User::getId).eq(0))
                .getSingle();
        System.out.println(single);
        List<User> dbList = userQuery
                .where(and(
                        get(User::getRandomNumber).ne(1),
                        get(User::getRandomNumber).gt(100),
                        get(User::getRandomNumber).ne(125),
                        get(User::getRandomNumber).le(666),
                        or(
                                get(User::getRandomNumber).lt(106),
                                get(User::getRandomNumber).gt(120),
                                get(User::getRandomNumber).eq(109)
                        ),
                        get(User::getRandomNumber).ne(128)
                )).getList();

        List<User> ftList = allUsers.stream()
                .filter(user -> user.getRandomNumber() != 1
                        && user.getRandomNumber() > 100
                        && user.getRandomNumber() != 125
                        && user.getRandomNumber() <= 666
                        && (user.getRandomNumber() < 106
                        || user.getRandomNumber() > 120
                        || user.getRandomNumber() == 109)
                        && user.getRandomNumber() != 128
                )
                .collect(Collectors.toList());

        assertEquals(dbList, ftList);
    }

    @ParameterizedTest
    @ArgumentsSource(UserDaoProvider.class)
    public void testComparablePredicateTesterGt(Select<User> userQuery) {

        List<User> qgt80 = userQuery
                .where(get(User::getRandomNumber).gt(80))
                .orderBy(get(User::getId).asc())
                .getList();
        List<User> fgt80 = allUsers.stream()
                .filter(it -> it.getRandomNumber() > 80)
                .collect(Collectors.toList());
        assertEquals(qgt80, fgt80);

    }

    @ParameterizedTest
    @ArgumentsSource(UserDaoProvider.class)
    public void testPredicateTesterEq(Select<User> userQuery) {
        int userId = 20;
        User user = userQuery
                .fetch(Arrays.asList(
                        get(User::getParentUser),
                        get(User::getParentUser).get(User::getParentUser)
                ))
                .where(get(User::getId).eq(userId))
                .getSingle();
        assertNotNull(user);
        assertEquals(user.getId(), userId);

        if (user.getPid() != null) {
            User parentUser = user.getParentUser();
            assertNotNull(parentUser);
            assertEquals(user.getPid(), parentUser.getId());
            User single = userQuery
                    .where(get(User::getId).eq(parentUser.getId()))
                    .getSingle();
            assertEquals(single, parentUser);
        }

    }

    @ParameterizedTest
    @ArgumentsSource(UserDaoProvider.class)
    void testGroupBy(Select<User> userQuery) {
        QueryStructure structure = userQuery
                .select(Arrays.asList(get(User::getId).min(), get(User::getRandomNumber)))
                .where(get(User::isValid).eq(true))
                .groupBy(User::getRandomNumber)
                .having(root -> root.get(User::getRandomNumber).eq(10))
                .buildMetadata()
                .getList(1, 5, LockModeType.PESSIMISTIC_WRITE);
        System.out.println(structure);
        MySqlQuerySqlBuilder builder = new MySqlQuerySqlBuilder();
        JdbcQueryExecutor.PreparedSql sql = builder.build(structure, new JpaMetamodel());
        System.out.println(sql.sql());

        String actual = "select" +
                " min(user_.id),user_.random_number" +
                " from `user` user_" +
                " where user_.valid=1" +
                " group by user_.random_number" +
                " having user_.random_number=?" +
                " limit ?,? for update";

    }

    @ParameterizedTest
    @ArgumentsSource(UserDaoProvider.class)
    public void testAggregateFunction(Select<User> userQuery) {

        List<ExpressionHolder<User, ?>> selected = Arrays.asList(
                min(User::getRandomNumber),
                max(User::getRandomNumber),
                count(User::getRandomNumber),
                avg(User::getRandomNumber),
                sum(User::getRandomNumber)
        );
        Object[] aggregated = userQuery
                .select(selected)
                .requireSingle();

        assertNotNull(aggregated);
        assertEquals(getUserIdStream().min().orElse(0), aggregated[0]);
        assertEquals(getUserIdStream().max().orElse(0), aggregated[1]);
        assertEquals(getUserIdStream().count(), aggregated[2]);
        OptionalDouble average = getUserIdStream().average();
        assertEquals(average.orElse(0), ((Number) aggregated[3]).doubleValue(), 0.0001);
        assertEquals((long) getUserIdStream().sum(), ((Number) aggregated[4]).intValue());

        List<Object[]> resultList = userQuery
                .select(Arrays.asList(min(User::getId), get(User::getRandomNumber)))
                .where(get(User::isValid).eq(true))
                .groupBy(User::getRandomNumber)
                .getList();

        Map<Integer, Optional<User>> map = allUsers.stream()
                .filter(User::isValid)
                .collect(Collectors.groupingBy(User::getRandomNumber, Collectors.minBy(Comparator.comparingInt(User::getId))));

        List<Object[]> fObjects = map.values().stream()
                .map(user -> {
                    Integer userId = user.map(User::getId).orElse(null);
                    Integer randomNumber = user.map(User::getRandomNumber).orElse(null);
                    return new Object[]{userId, randomNumber};
                })
                .sorted(Comparator.comparing(a -> ((Integer) a[0])))
                .collect(Collectors.toList());
        assertEqualsArrayList(resultList, fObjects);

        Object[] one = userQuery
                .select(Collections.singletonList(sum(User::getId)))
                .where(get(User::isValid).eq(true))
                .requireSingle();

        int userId = allUsers.stream()
                .filter(User::isValid)
                .mapToInt(User::getId)
                .sum();
        assertEquals(((Number) one[0]).intValue(), userId);

        Integer first = userQuery
                .select(User::getId)
                .orderBy(get(User::getId).desc())
                .getFirst();
        assertEquals(first, allUsers.get(allUsers.size() - 1).getId());
    }

    @ParameterizedTest
    @ArgumentsSource(UserDaoProvider.class)
    public void testSelect(Select<User> userQuery) {
        List<List<Object>> qList = map(userQuery
                .select(User::getRandomNumber, User::getUsername)
                .getList());

        List<List<Object>> fList = map(allUsers.stream()
                .map(it -> new Object[]{it.getRandomNumber(), it.getUsername()})
                .collect(Collectors.toList()));

        assertEquals(qList, fList);

        qList = map(userQuery
                .selectDistinct(User::getRandomNumber, User::getUsername)
                .getList());
        fList = fList.stream().distinct().collect(Collectors.toList());
        assertEquals(qList, fList);
    }

    List<List<Object>> map(List<Object[]> list) {
        return list.stream().map(Arrays::asList).collect(Collectors.toList());
    }

    @ParameterizedTest
    @ArgumentsSource(UserDaoProvider.class)
    public void testTime(Select<User> userQuery) {
        long start = System.currentTimeMillis();
        userQuery
                .orderBy(Arrays.asList(
                        get(User::getRandomNumber).desc(),
                        get(User::getId).asc()
                ))
                .getList();
        System.out.println(System.currentTimeMillis() - start);
    }

    @ParameterizedTest
    @ArgumentsSource(UserDaoProvider.class)
    public void testOrderBy(Select<User> userQuery) {
        List<User> list = userQuery
                .orderBy(Arrays.asList(
                        get(User::getRandomNumber).desc(),
                        get(User::getId).asc()
                ))

                .getList();
        ArrayList<User> sorted = new ArrayList<>(allUsers);
        sorted.sort((a, b) -> Integer.compare(b.getRandomNumber(), a.getRandomNumber()));
        assertEquals(list, sorted);

        list = userQuery
                .orderBy(Arrays.asList(get(User::getUsername).asc(),
                        get(User::getRandomNumber).desc(),
                        get(User::getId).asc()))
                .getList();

        sorted.sort((a, b) -> Integer.compare(b.getRandomNumber(), a.getRandomNumber()));
        sorted.sort(Comparator.comparing(User::getUsername));
        assertEquals(list, sorted);

        list = userQuery
                .orderBy(get(User::getTime).asc())
                .getList();
        sorted = new ArrayList<>(allUsers);
        sorted.sort(Comparator.comparing(User::getTime));
        assertEquals(list, sorted);
    }

    @ParameterizedTest
    @ArgumentsSource(UserDaoProvider.class)
    public void testOrderBy2(Select<User> userQuery) {
        List<User> list = userQuery
                .orderBy(
                        desc(User::getRandomNumber),
                        asc(User::getId)
                )
                .getList();
        ArrayList<User> sorted = new ArrayList<>(allUsers);
        sorted.sort(Comparator.comparingInt(User::getId));
        sorted.sort(Comparator.comparingInt(User::getRandomNumber).reversed());
        assertEquals(list, sorted);

        list = userQuery
                .orderBy(
                        asc(User::getUsername),
                        desc(User::getRandomNumber),
                        asc(User::getId)
                )
                .getList();

        sorted.sort(Comparator.comparingInt(User::getId));
        sorted.sort(Comparator.comparingInt(User::getRandomNumber).reversed());
        sorted.sort(Comparator.comparing(User::getUsername));
        assertEquals(list, sorted);

        list = userQuery
                .orderBy(User::getUsername)
                .orderBy(User::getRandomNumber).desc()
                .orderBy(User::getId).asc()
                .getList();
        assertEquals(list, sorted);

        list = userQuery
                .orderBy(asc(User::getUsername, User::getRandomNumber, User::getId))
                .getList();

        sorted.sort(Comparator.comparingInt(User::getId));
        sorted.sort(Comparator.comparingInt(User::getRandomNumber));
        sorted.sort(Comparator.comparing(User::getUsername));
        assertEquals(list, sorted);

        list = userQuery
                .orderBy(User::getUsername, User::getRandomNumber, User::getId).desc()
                .getList();

        sorted.sort(Comparator.comparingInt(User::getId).reversed());
        sorted.sort(Comparator.comparingInt(User::getRandomNumber).reversed());
        sorted.sort(Comparator.comparing(User::getUsername).reversed());
        assertEquals(list, sorted);

        list = userQuery
                .orderBy(User::getTime)
                .getList();
        sorted = new ArrayList<>(allUsers);
        sorted.sort(Comparator.comparing(User::getTime));
        assertEquals(list, sorted);
    }

    @ParameterizedTest
    @ArgumentsSource(UserDaoProvider.class)
    public void testPredicate(Select<User> userQuery) {
        List<User> qList = userQuery
                .where(not(get(User::getRandomNumber).ge(10)
                        .or(User::getRandomNumber).lt(5)))
                .getList();
        List<User> fList = allUsers.stream()
                .filter(it -> !(it.getRandomNumber() >= 10 || it.getRandomNumber() < 5))
                .collect(Collectors.toList());

        assertEquals(qList, fList);

        qList = userQuery
                .where(get(User::getUsername).ne("Jeremy Keynes").not())
                .getList();
        fList = allUsers.stream()
                .filter(it -> (it.getUsername().equalsIgnoreCase("Jeremy Keynes")))
                .collect(Collectors.toList());
        assertEquals(qList, fList);

        qList = userQuery.where(get(User::getUsername).eq("Jeremy Keynes"))
                .getList();
        assertEquals(qList, fList);

        qList = userQuery.where(
                        get(User::getUsername).eq("Jeremy Keynes")
                                .or(get(User::getId).eq(3))
                                .then().not()
                )
                .getList();
        fList = allUsers.stream()
                .filter(it -> !(it.getUsername().equalsIgnoreCase("Jeremy Keynes")
                        || it.getId() == 3))
                .collect(Collectors.toList());
        assertEquals(qList, fList);

        qList = userQuery
                .where(get(User::getUsername).eq("Jeremy Keynes")
                        .and(get(User::getId).eq(3))
                        .then().not()
                )
                .getList();
        fList = allUsers.stream()
                .filter(it -> !(it.getUsername().equalsIgnoreCase("Jeremy Keynes")
                        && it.getId() == 3))
                .collect(Collectors.toList());
        assertEquals(qList, fList);

    }

    @ParameterizedTest
    @ArgumentsSource(UserDaoProvider.class)
    public void testPredicate2(Select<User> userQuery) {
        List<User> qList = userQuery
                .where(or(
                        get(User::getRandomNumber).ge(10),
                        get(User::getRandomNumber).lt(5)
                ).not())
                .getList();
        List<User> fList = allUsers.stream()
                .filter(it -> !(it.getRandomNumber() >= 10 || it.getRandomNumber() < 5))
                .collect(Collectors.toList());

        assertEquals(qList, fList);

        qList = userQuery
                .where(get(User::getUsername).eq("Jeremy Keynes").not())
                .getList();
        fList = allUsers.stream()
                .filter(it -> !(it.getUsername().equalsIgnoreCase("Jeremy Keynes")))
                .collect(Collectors.toList());
        assertEquals(qList, fList);

        qList = userQuery.where(get(User::getUsername).eq("Jeremy Keynes")
                        .not()
                )
                .getList();
        assertEquals(qList, fList);

        qList = userQuery.where(get(User::getUsername).eq("Jeremy Keynes")
                        .or(get(User::getId).eq(3))
                        .then().not()
                )
                .getList();
        fList = allUsers.stream()
                .filter(it -> !(it.getUsername().equalsIgnoreCase("Jeremy Keynes")
                        || it.getId() == 3))
                .collect(Collectors.toList());
        assertEquals(qList, fList);

        qList = userQuery
                .where(and(
                        get(User::getUsername).eq("Jeremy Keynes"),
                        get(User::getId).eq(3)
                ).not())
                .getList();
        fList = allUsers.stream()
                .filter(it -> !(it.getUsername().equalsIgnoreCase("Jeremy Keynes")
                        && it.getId() == 3))
                .collect(Collectors.toList());
        assertEquals(qList, fList);

    }

    @ParameterizedTest
    @ArgumentsSource(UserDaoProvider.class)
    public void testGroupBy1(Select<User> userQuery) {
        List<Object[]> resultList = userQuery
                .select(User::isValid, User::getRandomNumber, User::getPid)
                .groupBy(User::getRandomNumber, User::getPid, User::isValid)
                .getList();

        List<Object[]> resultList2 = userQuery
                .select(User::isValid, User::getRandomNumber, User::getPid)
                .groupBy(User::getRandomNumber, User::getPid, User::isValid)
                .getList();
        assertEqualsArrayList(resultList, resultList2);
    }

    @ParameterizedTest
    @ArgumentsSource(UserDaoProvider.class)
    public void testIsNull(Select<User> userQuery) {

        List<User> qList = userQuery.where(get(User::getPid).isNotNull())
                .getList();

        List<User> fList = allUsers.stream()
                .filter(it -> it.getPid() != null)
                .collect(Collectors.toList());
        assertEquals(qList, fList);

        qList = userQuery.where(get(User::getPid).add(2).multiply(3).isNull())
                .getList();

        fList = allUsers.stream()
                .filter(it -> it.getPid() == null)
                .collect(Collectors.toList());
        assertEquals(qList, fList);

    }

    @ParameterizedTest
    @ArgumentsSource(UserDaoProvider.class)
    public void testOperator(Select<User> userQuery) {

        BooleanExpression<User> isValid = get(User::isValid);
        List<User> qList = userQuery.where(isValid).getList();
        List<User> validUsers = allUsers.stream().filter(User::isValid)
                .collect(Collectors.toList());
        List<User> fList = validUsers;
        assertEquals(qList, fList);

        qList = userQuery.where(isValid.and(User::getRandomNumber).eq(2))
                .getList();
        fList = validUsers.stream().filter(user -> user.getRandomNumber() == 2)
                .collect(Collectors.toList());
        assertEquals(qList, fList);

        qList = userQuery.where(isValid.and(User::getPid).ne(2))
                .getList();
        fList = validUsers.stream().filter(user -> user.getPid() != null && user.getPid() != 2)
                .collect(Collectors.toList());
        assertEquals(qList, fList);

        qList = userQuery.where(isValid.and(User::getRandomNumber).in(1, 2, 3))
                .getList();
        fList = validUsers.stream().filter(user -> Arrays.asList(1, 2, 3).contains(user.getRandomNumber()))
                .collect(Collectors.toList());
        assertEquals(qList, fList);

        qList = userQuery.where(isValid.and(User::getRandomNumber).notIn(1, 2, 3))
                .getList();
        fList = validUsers.stream().filter(user -> !Arrays.asList(1, 2, 3).contains(user.getRandomNumber()))
                .collect(Collectors.toList());
        assertEquals(qList, fList);

        qList = userQuery.where(isValid.and(User::getPid).isNull())
                .getList();
        fList = validUsers.stream().filter(user -> user.getPid() == null)
                .collect(Collectors.toList());
        assertEquals(qList, fList);

        qList = userQuery.where(isValid.and(User::getRandomNumber).ge(10))
                .getList();
        fList = validUsers.stream().filter(user -> user.getRandomNumber() >= 10)
                .collect(Collectors.toList());
        assertEquals(qList, fList);

        qList = userQuery.where(isValid.and(User::getRandomNumber).gt(10))
                .getList();
        fList = validUsers.stream().filter(user -> user.getRandomNumber() > 10)
                .collect(Collectors.toList());
        assertEquals(qList, fList);

        qList = userQuery.where(isValid.and(User::getRandomNumber).le(10))
                .getList();
        fList = validUsers.stream().filter(user -> user.getRandomNumber() <= 10)
                .collect(Collectors.toList());
        assertEquals(qList, fList);

        qList = userQuery.where(isValid.and(User::getRandomNumber).lt(10))
                .getList();
        fList = validUsers.stream().filter(user -> user.getRandomNumber() < 10)
                .collect(Collectors.toList());
        assertEquals(qList, fList);

        qList = userQuery.where(isValid.and(User::getRandomNumber).between(10, 15))
                .getList();
        fList = validUsers.stream().filter(user -> user.getRandomNumber() >= 10 && user.getRandomNumber() <= 15)
                .collect(Collectors.toList());
        assertEquals(qList, fList);

        qList = userQuery.where(isValid.and(User::getRandomNumber).notBetween(10, 15))
                .getList();
        fList = validUsers.stream().filter(user -> user.getRandomNumber() < 10 || user.getRandomNumber() > 15)
                .collect(Collectors.toList());
        assertEquals(qList, fList);

        qList = userQuery.where(isValid
                        .and(User::getRandomNumber).notBetween(10, 15)
                        .and(User::getId).mod(3).eq(0)
                )
                .getList();
        fList = validUsers.stream().filter(user ->
                        !(user.getRandomNumber() >= 10 && user.getRandomNumber() <= 15)
                                && user.getId() % 3 == 0)
                .collect(Collectors.toList());
        assertEquals(qList, fList);

        qList = userQuery.where(isValid.and(User::getRandomNumber).ge(get(User::getPid)))
                .getList();
        fList = validUsers.stream().filter(user -> user.getPid() != null && user.getRandomNumber() >= user.getPid())
                .collect(Collectors.toList());
        assertEquals(qList, fList);

        qList = userQuery.where(isValid.and(User::getRandomNumber).gt(get(User::getPid)))
                .getList();
        fList = validUsers.stream().filter(user -> user.getPid() != null && user.getRandomNumber() > user.getPid())
                .collect(Collectors.toList());
        assertEquals(qList, fList);

        qList = userQuery.where(isValid.and(User::getRandomNumber).le(get(User::getPid)))
                .getList();
        fList = validUsers.stream().filter(user -> user.getPid() != null && user.getRandomNumber() <= user.getPid())
                .collect(Collectors.toList());
        assertEquals(qList, fList);

        qList = userQuery.where(isValid.and(User::getRandomNumber).lt(get(User::getPid)))
                .getList();
        fList = validUsers.stream().filter(user -> user.getPid() != null && user.getRandomNumber() < user.getPid())
                .collect(Collectors.toList());
        assertEquals(qList, fList);

        qList = userQuery.where(isValid.and(User::getRandomNumber)
                        .between(get(User::getRandomNumber), get(User::getPid)))
                .getList();
        fList = validUsers.stream()
                .filter(user -> user.getPid() != null && user.getRandomNumber() >= user.getRandomNumber() && user.getRandomNumber() <= user.getPid())
                .collect(Collectors.toList());
        assertEquals(qList, fList);

    }

    @ParameterizedTest
    @ArgumentsSource(UserDaoProvider.class)
    public void testPredicateAssembler(Select<User> userQuery) {

        List<User> qList = userQuery.where(get(User::isValid).eq(true)
                        .and(User::getParentUser).get(User::getUsername).eq(username))
                .getList();
        List<User> fList = allUsers.stream()
                .filter(user -> user.isValid()
                        && user.getParentUser() != null
                        && Objects.equals(user.getParentUser().getUsername(), username))
                .collect(Collectors.toList());

        assertEquals(qList, fList);

        Path<User, Number> getUsername = User::getRandomNumber;
        qList = userQuery.where(get(User::isValid).eq(true)
                        .and(getUsername).eq(10))
                .getList();
        fList = allUsers.stream()
                .filter(user -> user.isValid()
                        && Objects.equals(user.getRandomNumber(), 10))
                .collect(Collectors.toList());

        assertEquals(qList, fList);

        qList = userQuery.where(get(User::isValid).eq(true)
                        .or(getUsername).eq(10))
                .getList();
        fList = allUsers.stream()
                .filter(user -> user.isValid()
                        || Objects.equals(user.getRandomNumber(), 10))
                .collect(Collectors.toList());

        assertEquals(qList, fList);

        qList = userQuery.where(get(User::isValid).eq(true)
                        .and(getUsername).ne(10))
                .getList();
        fList = allUsers.stream()
                .filter(user -> user.isValid()
                        && !Objects.equals(user.getRandomNumber(), 10))
                .collect(Collectors.toList());

        assertEquals(qList, fList);

        qList = userQuery.where(get(User::isValid).eq(true)
                        .or(getUsername).ne(10))
                .getList();
        fList = allUsers.stream()
                .filter(user -> user.isValid()
                        || !Objects.equals(user.getRandomNumber(), 10))
                .collect(Collectors.toList());

        assertEquals(qList, fList);

        Date time = allUsers.get(20).getTime();

        ExpressionHolder<User, Boolean> or = get(User::isValid).eq(true)
                .or(
                        get(User::getParentUser)
                                .get(User::getUsername)
                                .eq(username)
                                .and(User::getTime)
                                .ge(time));
        qList = userQuery.where(or).getList();

        List<User> jeremy_keynes = userQuery
                .fetch(User::getParentUser)
                .where(get(User::isValid).eq(true)
                        .or(get(User::getParentUser)
                                .get(User::getUsername).eq(username)
                                .and(User::getTime).ge(time)
                        ))
                .getList();

        fList = allUsers.stream()
                .filter(user -> user.isValid()
                        || (user.getParentUser() != null
                        && Objects.equals(user.getParentUser().getUsername(), username)
                        && user.getTime().getTime() >= time.getTime()))
                .collect(Collectors.toList());

        assertEquals(qList, fList);
        assertEquals(qList, jeremy_keynes);

        qList = userQuery.where(get(User::isValid).eq(true)
                        .and(User::getRandomNumber).ne(5))
                .getList();
        fList = allUsers.stream()
                .filter(user -> user.isValid()
                        && user.getRandomNumber() != 5)
                .collect(Collectors.toList());

        assertEquals(qList, fList);

        qList = userQuery.where(get(User::isValid).eq(true)
                        .or(User::getRandomNumber).eq(5))
                .getList();
        fList = allUsers.stream()
                .filter(user -> user.isValid()
                        || user.getRandomNumber() == 5)
                .collect(Collectors.toList());

        assertEquals(qList, fList);

        qList = userQuery.where(get(User::getRandomNumber).ne(6)
                        .or(User::isValid).eq(false))
                .getList();
        fList = allUsers.stream()
                .filter(user -> user.getRandomNumber() != 6
                        || !user.isValid())
                .collect(Collectors.toList());

        assertEquals((qList), (fList));

        qList = userQuery.where(get(User::getRandomNumber).ne(6)
                        .and(User::getParentUser).get(User::isValid).eq(true))
                .getList();
        fList = allUsers.stream()
                .filter(user -> user.getRandomNumber() != 6
                        && (user.getParentUser() != null && user.getParentUser().isValid()))
                .collect(Collectors.toList());

        assertEquals((qList), (fList));

        qList = userQuery.where(get(User::getRandomNumber).ne(6)
                        .and(User::getParentUser).get(User::isValid).ne(true))
                .getList();
        fList = allUsers.stream()
                .filter(user -> user.getRandomNumber() != 6
                        && (user.getParentUser() != null && !user.getParentUser().isValid()))
                .collect(Collectors.toList());

        assertEquals((qList), (fList));

        qList = userQuery.where(get(User::getRandomNumber).ne(6)
                        .or(User::getParentUser).get(User::isValid).ne(true))
                .getList();
        fList = allUsers.stream()
                .filter(user -> user.getRandomNumber() != 6
                        || (user.getParentUser() != null && !user.getParentUser().isValid()))
                .collect(Collectors.toList());

        assertEquals((qList), (fList));

        qList = userQuery.where(not(get(User::getRandomNumber).ge(10)
                        .or(User::getRandomNumber).lt(5)
                ))
                .getList();
        fList = allUsers.stream()
                .filter(it -> !(it.getRandomNumber() >= 10 || it.getRandomNumber() < 5))
                .collect(Collectors.toList());

        assertEquals(qList, fList);

        qList = userQuery.where(not(get(User::getRandomNumber).ge(10)
                                .and(User::getRandomNumber).le(15)
                        )
                )
                .getList();
        fList = allUsers.stream()
                .filter(it -> !(it.getRandomNumber() >= 10 && it.getRandomNumber() <= 15))
                .collect(Collectors.toList());

        assertEquals(qList, fList);

        qList = userQuery.where(not(
                        get(User::getRandomNumber).ge(10)
                                .and(User::getUsername).eq(username)
                ))
                .getList();
        fList = allUsers.stream()
                .filter(it -> !(it.getRandomNumber() >= 10 && it.getUsername().equals(username)))
                .collect(Collectors.toList());
        assertEquals(qList, fList);

        qList = userQuery.where(not(get(User::getRandomNumber).ge(10)
                                .or(User::getUsername).eq(username)
                        )
                )
                .getList();
        fList = allUsers.stream()
                .filter(it -> !(it.getRandomNumber() >= 10 || it.getUsername().equals(username)))
                .collect(Collectors.toList());
        assertEquals(qList, fList);

        qList = userQuery.where(not(get(User::getRandomNumber).ge(10)
                        .and(User::getUsername).eq(username))
                        .not()
                )
                .getList();
        fList = allUsers.stream()
                .filter(it -> (it.getRandomNumber() >= 10 && it.getUsername().equals(username)))
                .collect(Collectors.toList());
        assertEquals(qList, fList);

        qList = userQuery.where(not(get(User::getRandomNumber).ge(10)
                        .or(User::getUsername).eq(username))
                        .not()
                )
                .getList();
        fList = allUsers.stream()
                .filter(it -> it.getRandomNumber() >= 10 || it.getUsername().equals(username))
                .collect(Collectors.toList());
        assertEquals(qList, fList);

    }

    @ParameterizedTest
    @ArgumentsSource(UserDaoProvider.class)
    void testSubQuery(Select<User> userQuery) {
        Date time = allUsers.get(20).getTime();

        userQuery
                .fetch(User::getParentUser)
                .where(get(User::isValid).eq(true)
                        .or(get(User::getParentUser)
                                .get(User::getUsername).eq(username)
                                .and(User::getTime).ge(time)
                        ))
                .count();
    }

    @ParameterizedTest
    @ArgumentsSource(UserDaoProvider.class)
    public void testNumberPredicateTester(Select<User> userQuery) {
        List<User> list = userQuery
                .where(get(User::getRandomNumber).add(2).ge(4))
                .getList();
        List<User> fList = allUsers.stream()
                .filter(user -> user.getRandomNumber() + 2 >= 4)
                .collect(Collectors.toList());

        assertEquals(list, fList);

        list = userQuery
                .where(get(User::getRandomNumber).subtract(2).ge(4))
                .getList();
        fList = allUsers.stream()
                .filter(user -> user.getRandomNumber() - 2 >= 4)
                .collect(Collectors.toList());

        assertEquals(list, fList);

        list = userQuery
                .where(get(User::getRandomNumber).multiply(2).ge(4))
                .getList();
        fList = allUsers.stream()
                .filter(user -> user.getRandomNumber() * 2 >= 4)
                .collect(Collectors.toList());

        assertEquals(list, fList);

        list = userQuery
                .where(get(User::getRandomNumber).divide(2).ge(4))
                .getList();
        fList = allUsers.stream()
                .filter(user -> user.getRandomNumber() / 2 >= 4)
                .collect(Collectors.toList());

        assertEquals(list, fList);

        list = userQuery
                .where(get(User::getRandomNumber).mod(2).ge(1))
                .getList();
        fList = allUsers.stream()
                .filter(user -> user.getRandomNumber() % 2 == 1)
                .collect(Collectors.toList());

        assertEquals(list, fList);

        ///
        list = userQuery
                .where(get(User::getRandomNumber).add(get(User::getId)).ge(40))
                .getList();
        fList = allUsers.stream()
                .filter(user -> user.getRandomNumber() + user.getId() >= 40)
                .collect(Collectors.toList());

        assertEquals(list, fList);

        list = userQuery
                .where(get(User::getRandomNumber).subtract(get(User::getId)).ge(40))
                .getList();
        fList = allUsers.stream()
                .filter(user -> user.getRandomNumber() - user.getId() >= 40)
                .collect(Collectors.toList());

        assertEquals(list, fList);

        list = userQuery
                .where(get(User::getRandomNumber).multiply(get(User::getId)).ge(40))
                .getList();
        fList = allUsers.stream()
                .filter(user -> user.getRandomNumber() * user.getId() >= 40)
                .collect(Collectors.toList());

        assertEquals(list, fList);

        list = userQuery
                .where(get(User::getRandomNumber).divide(get(User::getId)).ge(40))
                .getList();
        fList = allUsers.stream()
                .filter(user -> user.getId() != 0 && user.getRandomNumber() / user.getId() >= 40)
                .collect(Collectors.toList());

        assertEquals(list, fList);

        list = userQuery
                .where(get(User::getRandomNumber).mod(get(User::getId)).ge(10))
                .getList();
        fList = allUsers.stream()
                .filter(user -> user.getId() != 0 && user.getRandomNumber() % user.getId() >= 10)
                .collect(Collectors.toList());

        assertEquals(list, fList);

    }

    @ParameterizedTest
    @ArgumentsSource(UserDaoProvider.class)
    public void testStringPredicateTester(Select<User> userQuery) {
        String username = "Roy Sawyer";

        List<User> qList = userQuery.where(get(User::getUsername).substring(2).eq("eremy Keynes"))
                .getList();
        List<User> fList = allUsers.stream()
                .filter(user -> user.getUsername().substring(1).equals("eremy Keynes"))
                .collect(Collectors.toList());

        assertEquals(qList, fList);

        qList = userQuery.where(get(User::getUsername).substring(1, 1).eq("M"))
                .getList();
        fList = allUsers.stream()
                .filter(user -> user.getUsername().charAt(0) == 'M')
                .collect(Collectors.toList());

        assertEquals(qList, fList);

        qList = userQuery.where(get(User::getUsername).trim().like(username))
                .getList();
        fList = allUsers.stream()
                .filter(user -> user.getUsername().trim().startsWith(username))
                .collect(Collectors.toList());
        assertEquals(qList, fList);

        qList = userQuery.where(get(User::getUsername).trim().notContains("i"))
                .getList();
        fList = allUsers.stream()
                .filter(user -> !user.getUsername().toLowerCase().contains("i"))
                .collect(Collectors.toList());
        assertEquals(qList, fList);

        qList = userQuery.where(get(User::getUsername).length().eq(username.length()))
                .getList();
        fList = allUsers.stream()
                .filter(user -> user.getUsername().length() == username.length())
                .collect(Collectors.toList());
        assertEquals(qList, fList);

        qList = userQuery.where(get(User::getUsername).startWith("M"))
                .getList();
        fList = allUsers.stream()
                .filter(user -> user.getUsername().startsWith("M"))
                .collect(Collectors.toList());
        assertEquals(qList, fList);

        qList = userQuery.where(get(User::getUsername).endsWith("s"))
                .getList();
        fList = allUsers.stream()
                .filter(user -> user.getUsername().endsWith("s"))
                .collect(Collectors.toList());
        assertEquals(qList, fList);

        qList = userQuery.where(get(User::getUsername).lower().contains("s"))
                .getList();
        fList = allUsers.stream()
                .filter(user -> user.getUsername().toLowerCase().contains("s"))
                .collect(Collectors.toList());
        assertEquals(qList, fList);

        qList = userQuery.where(get(User::getUsername).upper().contains("S"))
                .getList();
        fList = allUsers.stream()
                .filter(user -> user.getUsername().toUpperCase().contains("S"))
                .collect(Collectors.toList());
        assertEquals(qList, fList);
    }

    @ParameterizedTest
    @ArgumentsSource(UserDaoProvider.class)
    public void testResultBuilder(Select<User> userQuery) {
        List<User> resultList = userQuery.getList(5, 10);
        List<User> subList = allUsers.subList(5, 5 + 10);
        assertEquals(resultList, subList);

//        resultList = userQuery.getList(20);
//        subList = allUsers.subList(20, allUsers.size());
//        assertEquals(resultList, subList);

        List<Integer> userIds = userQuery.select(User::getId)
                .getList(5, 10);
        List<Integer> subUserIds = allUsers.subList(5, 5 + 10)
                .stream().map(User::getId)
                .collect(Collectors.toList());

        assertEquals(userIds, subUserIds);

        resultList = userQuery.where(get(User::getId).in()).getList();
        assertEquals(resultList.size(), 0);

        resultList = userQuery.where(get(User::getId).notIn()).getList();
        assertEquals(resultList, allUsers);

        long count = userQuery.count();
        assertEquals(count, allUsers.size());

        User first = userQuery.getFirst();
        assertEquals(first, allUsers.get(0));

        first = userQuery.where(get(User::getId).eq(0)).requireSingle();
        assertEquals(first, allUsers.get(0));

        first = userQuery.getFirst(10);
        assertEquals(first, allUsers.get(10));

        assertThrowsExactly(IllegalStateException.class, userQuery::requireSingle);
        assertThrowsExactly(NullPointerException.class, () -> userQuery.where(get(User::getId).eq(-1)).requireSingle());

        assertTrue(userQuery.exist());
        assertTrue(userQuery.exist(allUsers.size() - 1));
        assertFalse(userQuery.exist(allUsers.size()));

        List<UserModel> userModels = userQuery.select(UserModel.class).getList();

        List<Map<String, Object>> l0 = allUsers.stream()
                .map(UserModel::new)
                .map(UserInterface::asMap)
                .collect(Collectors.toList());

        List<Map<String, Object>> l1 = userQuery.select(UserInterface.class).getList()
                .stream()
                .map(UserInterface::asMap)
                .collect(Collectors.toList());

        List<Map<String, Object>> l2 = userModels.stream()
                .map(UserInterface::asMap)
                .collect(Collectors.toList());

        assertEquals(l0, l1);
        assertEquals(l0, l2);

    }

    @ParameterizedTest
    @ArgumentsSource(UserDaoProvider.class)
    public void testSlice(Select<User> userQuery) {
        Slice<String> slice = userQuery.select(User::getUsername)
                .where(User::getParentUser).get(User::getRandomNumber).eq(10)
                .groupBy(User::getUsername)
                .slice(2, 10);
        System.out.println(slice);
    }

    @ParameterizedTest
    @ArgumentsSource(UserDaoProvider.class)
    void projection(Select<User> userQuery) throws JsonProcessingException {
        List<UserInterface> list0 = userQuery.select(UserInterface.class)
                .getList();
        List<UserInterface> list1 = userQuery.select(UserInterface.class)
                .getList();

        System.out.println(JsonSerializablePredicateValueTest.mapper.writeValueAsString(list0.get(0)));

        assertEquals(list0, list1);
    }

    @ParameterizedTest
    @ArgumentsSource(UserDaoProvider.class)
    void testInterfaceSelect(Select<User> userQuery) {
        UserInterface list = userQuery.select(UserInterface.class)
                .getFirst();
        String string = list.toString();
        System.out.println(string);
    }

    @ParameterizedTest
    @ArgumentsSource(UserDaoProvider.class)
    public void testAttr(Select<User> userQuery) {
        User first = userQuery.orderBy(get(User::getId).desc()).getFirst();
        ArrayList<User> users = new ArrayList<>(allUsers);
        users.sort((a, b) -> Integer.compare(b.getId(), a.getId()));
        User f = users.stream().findFirst().orElse(null);
        assertEquals(first, f);

        first = userQuery.orderBy(get(User::getUsername).desc()).getFirst();

        users = new ArrayList<>(allUsers);
        users.sort((a, b) -> b.getUsername().compareTo(a.getUsername()));
        f = users.stream().findFirst().orElse(null);
        assertEquals(first, f);

        first = userQuery.orderBy(get(User::isValid).desc()).getFirst();
        users = new ArrayList<>(allUsers);
        users.sort((a, b) -> Boolean.compare(b.isValid(), a.isValid()));
        f = users.stream().findFirst().orElse(null);
        assertEquals(first, f);

        first = userQuery
                .where(get(User::isValid).eq(true))
                .getFirst();

        f = allUsers.stream()
                .filter(User::isValid)
                .findFirst()
                .orElse(null);
        assertEquals(first, f);

        List<User> resultList = userQuery
                .where(get(User::getParentUser).get(User::isValid)
                        .eq(true))
                .getList();
        List<User> fList = allUsers.stream()
                .filter(user -> user.getParentUser() != null && user.getParentUser().isValid())
                .collect(Collectors.toList());

        assertEquals(resultList, fList);
    }

    @ParameterizedTest
    @ArgumentsSource(UserDaoProvider.class)
    public void testWhere(Select<User> userQuery) {
        List<User> resultList = userQuery
                .where(get(User::getParentUser).get(User::getUsername).eq(username))
                .getList();
        List<User> fList = allUsers.stream()
                .filter(user -> user.getParentUser() != null && username.equals(user.getParentUser().getUsername()))
                .collect(Collectors.toList());
        assertEquals(resultList, fList);

        resultList = userQuery
                .where(get(User::getParentUser).get(User::getUsername).ne(username))
                .getList();
        fList = allUsers.stream()
                .filter(user -> user.getParentUser() != null && !username.equals(user.getParentUser().getUsername()))
                .collect(Collectors.toList());
        assertEquals(resultList, fList);

        resultList = userQuery
                .where(get(User::getUsername).ne(username))
                .getList();
        fList = allUsers.stream()
                .filter(user -> !username.equals(user.getUsername()))
                .collect(Collectors.toList());
        assertEquals(resultList, fList);

        resultList = userQuery
                .where(get(User::getUsername).ne(username))
                .getList();
        fList = allUsers.stream()
                .filter(user -> !username.equals(user.getUsername()))
                .collect(Collectors.toList());
        assertEquals(resultList, fList);

        resultList = userQuery
                .where(get(User::getUsername).ne(username))
                .getList();
        fList = allUsers.stream()
                .filter(user -> !username.equals(user.getUsername()))
                .collect(Collectors.toList());
        assertEquals(resultList, fList);
    }

    @ParameterizedTest
    @ArgumentsSource(UserDaoProvider.class)
    public void testPathBuilder(Select<User> userQuery) {
        List<User> resultList = userQuery.where(get(User::getParentUser)
                        .get(User::getParentUser).get(User::getUsername).eq(username))
                .getList();
        List<User> fList = allUsers.stream()
                .filter(user -> {
                    User p = user.getParentUser();
                    return p != null && p.getParentUser() != null && username.equals(p.getParentUser().getUsername());
                })
                .collect(Collectors.toList());
        assertEquals(resultList, fList);

        resultList = userQuery.where(get(User::getParentUser)
                        .get(User::getRandomNumber).eq(5))
                .getList();
        fList = allUsers.stream()
                .filter(user -> {
                    User p = user.getParentUser();
                    return p != null && p.getRandomNumber() == 5;
                })
                .collect(Collectors.toList());
        assertEquals(resultList, fList);

        resultList = userQuery.where(get(User::getParentUser)
                        .get(User::getRandomNumber).eq(5))
                .getList();
        fList = allUsers.stream()
                .filter(user -> {
                    User p = user.getParentUser();
                    return p != null && p.getRandomNumber() == 5;
                })
                .collect(Collectors.toList());
        assertEquals(resultList, fList);
    }

    @ParameterizedTest
    @ArgumentsSource(UserDaoProvider.class)
    public void testBigNum(Select<User> userQuery) {
        List<User> users = userQuery.where(get(User::getTimestamp).eq(Double.MAX_VALUE))
                .getList();
        System.out.println(users);
    }

    private IntStream getUserIdStream() {
        return allUsers.stream().mapToInt(User::getRandomNumber);
    }

    static void assertEqualsArrayList(List<Object[]> resultList, List<Object[]> resultList2) {
        assertEquals(resultList.size(), resultList2.size());
        for (int i = 0; i < resultList.size(); i++) {
            assertArrayEquals(resultList.get(i), resultList2.get(i));
        }
    }

}
