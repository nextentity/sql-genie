package io.github.genie.sql.core;

import com.mysql.cj.jdbc.MysqlDataSource;
import io.github.genie.sql.core.Expression.Meta;
import io.github.genie.sql.core.entity.User;
import io.github.genie.sql.core.executor.jdbc.ConnectionProvider;
import io.github.genie.sql.core.executor.jdbc.JdbcQueryExecutor;
import io.github.genie.sql.core.executor.jdbc.JdbcResultCollector;
import io.github.genie.sql.core.executor.jdbc.MySqlSqlBuilder;
import io.github.genie.sql.core.mapping.JpaTableMappingFactory;
import io.github.genie.sql.core.projection.UserInterface;
import io.github.genie.sql.core.projection.UserModel;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static io.github.genie.sql.core.Q.*;
import static org.junit.jupiter.api.Assertions.*;


@Slf4j
public class JdbcTest extends JpaTest {

    @BeforeAll
    public static void init() {
        JpaTest.init();
        MysqlDataSource source = new MysqlDataSource();
        source.setUrl("jdbc:mysql:///sql-dsl");
        source.setUser("root");
        source.setPassword("root");
        ConnectionProvider sqlExecutor = new ConnectionProvider() {
            @Override
            public <T> T execute(ConnectionCallback<T> action) throws SQLException {
                try (Connection connection = source.getConnection()) {
                    return action.doInConnection(connection);
                }
            }
        };
        query = Query.createQuery(new JdbcQueryExecutor(
                new JpaTableMappingFactory(),
                new MySqlSqlBuilder(),
                sqlExecutor,
                new JdbcResultCollector()
        ));
        userQuery = query.from(User.class);
    }


    @Test
    public void testAndOr() {
        User single = userQuery
                .where(get(User::getId).eq(0))
                .getSingle();
        System.out.println(single);
        List<User> dbList = userQuery
                .where(get(User::getRandomNumber)
                        .ne(1)
                        .and(User::getRandomNumber)
                        .gt(100)
                        .and(User::getRandomNumber).ne(125)
                        .and(User::getRandomNumber).le(666)
                        .and(or(
                                get(User::getRandomNumber).lt(106),
                                get(User::getRandomNumber).gt(120),
                                get(User::getRandomNumber).eq(109)
                        ))
                        .and(User::getRandomNumber).ne(128)
                ).getList();

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

    @Test
    public void testAndOrChain() {
        User single = userQuery
                .where(get(User::getId).eq(0))
                .getSingle();
        System.out.println(single);
        List<User> dbList = userQuery
                .where(User::getRandomNumber)
                .ne(1)
                .and(User::getRandomNumber)
                .gt(100)
                .and(User::getRandomNumber).ne(125)
                .and(User::getRandomNumber).le(666)
                .and(or(
                                get(User::getRandomNumber).lt(106),
                                get(User::getRandomNumber).gt(120),
                                get(User::getRandomNumber).eq(109)
                        )
                                .and(User::getRandomNumber).ne(128)
                ).getList();

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

    @Test
    public void testAndOrChan() {
        User single = userQuery
                .where(get(User::getId).eq(0))
                .getSingle();
        System.out.println(single);
        List<User> dbList = userQuery
                .where(User::getRandomNumber)
                .ne(1)
                .and(User::getRandomNumber)
                .gt(100)
                .and(User::getRandomNumber).ne(125)
                .and(User::getRandomNumber).le(666)
                .and(or(
                                get(User::getRandomNumber).lt(106),
                                get(User::getRandomNumber).gt(120),
                                get(User::getRandomNumber).eq(109)
                        )
                                .and(User::getRandomNumber).ne(128)
                ).getList();

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

    @Test
    public void testAndOr2() {
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


    @Test
    public void testComparablePredicateTesterGt() {

        List<User> qgt80 = userQuery
                .where(get(User::getRandomNumber).gt(80))
                .orderBy(get(User::getId).asc())
                .getList();
        List<User> fgt80 = allUsers.stream()
                .filter(it -> it.getRandomNumber() > 80)
                .collect(Collectors.toList());
        assertEquals(qgt80, fgt80);

    }

    @Test
    public void testPredicateTesterEq() {
        int userId = 20;
        User user = userQuery
                .fetch(List.of(
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

        User user1 = userQuery
                .fetch(
                        User::getParentUser, User::getParentUser
                )
                .where(get(User::getId).eq(userId))
                .getSingle();

    }

    @Test
    void testGroupBy() {
        QueryMetadata metadata = userQuery
                .select(List.of(get(User::getId).min(), get(User::getRandomNumber)))
                .where(get(User::isValid).eq(true))
                .groupBy(User::getRandomNumber)
                .having(get(User::getRandomNumber).eq(10))
                .buildMetadata()
                .getList(1, 5, LockModeType.PESSIMISTIC_WRITE);
        System.out.println(metadata);
        MySqlSqlBuilder builder = new MySqlSqlBuilder();
        JdbcQueryExecutor.PreparedSql sql = builder.build(metadata, new JpaTableMappingFactory());
        System.out.println(sql.sql());

        String actual = "select" +
                        " min(u.id),u.random_number" +
                        " from `user` u" +
                        " where u.valid=1" +
                        " group by u.random_number" +
                        " having u.random_number=?" +
                        " limit 1,5 for update";

        assertEquals(sql.sql(), actual);
    }

    @Test
    public void testAggregateFunction() {

        List<Expression<User, ?>> selected = Arrays.asList(
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
                .select(List.of(min(User::getId), get(User::getRandomNumber)))
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
                .select(List.of(sum(User::getId)))
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

    @Test
    public void testSelect() {
        List<Object[]> qList = new ArrayList<>(userQuery
                .select(User::getRandomNumber, User::getUsername)
                .getList());

        List<Object[]> fList = allUsers.stream()
                .map(it -> new Object[]{it.getRandomNumber(), it.getUsername()})
                .collect(Collectors.toList());

        JpaTest.assertEqualsArrayList(qList, fList);

    }

    @Test
    public void testOrderBy() {
        List<User> list = userQuery
                .orderBy(List.of(
                        get(User::getRandomNumber).desc(),
                        get(User::getId).asc()
                ))

                .getList();
        ArrayList<User> sorted = new ArrayList<>(allUsers);
        sorted.sort((a, b) -> Integer.compare(b.getRandomNumber(), a.getRandomNumber()));
        assertEquals(list, sorted);

        list = userQuery
                .orderBy(List.of(get(User::getUsername).asc(),
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

    @Test
    public void testOrderBy2() {
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
                .orderBy(asc(User::getUsername, User::getRandomNumber, User::getId))
                .getList();

        sorted.sort(Comparator.comparingInt(User::getId));
        sorted.sort(Comparator.comparingInt(User::getRandomNumber));
        sorted.sort(Comparator.comparing(User::getUsername));
        assertEquals(list, sorted);

        list = userQuery
                .orderBy(desc(User::getUsername, User::getRandomNumber, User::getId))
                .getList();

        sorted.sort(Comparator.comparingInt(User::getId).reversed());
        sorted.sort(Comparator.comparingInt(User::getRandomNumber).reversed());
        sorted.sort(Comparator.comparing(User::getUsername).reversed());
        assertEquals(list, sorted);


        list = userQuery
                .orderBy(asc(User::getTime))
                .getList();
        sorted = new ArrayList<>(allUsers);
        sorted.sort(Comparator.comparing(User::getTime));
        assertEquals(list, sorted);
    }

    @Test
    public void testPredicate() {
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

    @Test
    public void testPredicate2() {
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


    @Test
    public void testGroupBy1() {
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


    @Test
    public void testIsNull() {

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

    @Test
    public void testOperator() {

        ExpressionOps.Root.Predicate<User> isValid = get(User::isValid);
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

        // qList = isValid.and(User::getPid).nullIf(4).eq(2)
        //         .getList();
        // fList = validUsers.stream().filter(user -> {
        //             Integer pid = user.getPid();
        //             if (pid != null && pid == 4) {
        //                 pid = null;
        //             }
        //             return pid != null && pid == 2;
        //         })
        //         .collect(Collectors.toList());
        // assertEquals(qList, fList);

        // qList = isValid.and(User::getPid).ifNull(2).eq(2)
        //         .getList();
        // fList = validUsers.stream().filter(user -> {
        //             Integer pid = user.getPid();
        //             if (pid == null) {
        //                 pid = 2;
        //             }
        //             return pid == 2;
        //         })
        //         .collect(Collectors.toList());
        // assertEquals(qList, fList);


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

    @Test
    public void testPredicateAssembler() {

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

        qList = userQuery.where(get(User::isValid).eq(true)
                        .or(
                                get(User::getParentUser)
                                        .get(User::getUsername)
                                        .eq(username)
                                        .and(User::getTime)
                                        .ge(time))
                )
                .getList();

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

    @Test
    void e() {
        Expression<User, Boolean> ne = not(get(User::getRandomNumber).ge(10)
                .and(User::getUsername).eq(username))
                .not();
        Meta basic = ne.meta();
        System.out.println(basic);
    }

    @Test
    public void testNumberPredicateTester() {
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

    @Test
    public void testStringPredicateTester() {
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

    @Test
    public void testOffset() {
        userQuery.getList(100);
    }

    @Test
    public void testResultBuilder() {
        List<User> resultList = userQuery.getList(5, 10);
        List<User> subList = allUsers.subList(5, 5 + 10);
        assertEquals(resultList, subList);

        resultList = userQuery.getList(20);
        subList = allUsers.subList(20, allUsers.size());
        assertEquals(resultList, subList);

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

        int count = userQuery.count();
        assertEquals(count, allUsers.size());

        User first = userQuery.getFirst();
        assertEquals(first, allUsers.get(0));

        first = userQuery.where(get(User::getId).eq(0)).requireSingle();
        assertEquals(first, allUsers.get(0));

        first = userQuery.getFirst(10);
        assertEquals(first, allUsers.get(10));

        assertThrowsExactly(IllegalStateException.class, () -> userQuery.requireSingle());
        assertThrowsExactly(NullPointerException.class, () -> userQuery.where(get(User::getId).eq(-1)).requireSingle());

        assertTrue(userQuery.exist());
        assertTrue(userQuery.exist(allUsers.size() - 1));
        assertFalse(userQuery.exist(allUsers.size()));

        List<UserModel> userModels = userQuery.select(UserModel.class)
                .getList();


        List<Map<String, Object>> l0 = allUsers.stream()
                .map(UserModel::new)
                .map(UserInterface::asMap)
                .collect(Collectors.toList());

        // List<Map<String, Object>> l1 = userInterfaces.stream()
        //         .map(UserInterface::asMap)
        //         .collect(Collectors.toList());

        List<UserInterface> list = userQuery.select(UserInterface.class)
                .getList();

        List<Map<String, Object>> l2 = userModels.stream()
                .map(UserInterface::asMap)
                .collect(Collectors.toList());

        // assertEquals(l0, l1);
        assertEquals(l0, l2);

    }

    @Test
    void testInterfaceSelect() {
        UserInterface list = userQuery.select(UserInterface.class)
                .getFirst();
        String string = list.toString();
        System.out.println(string);
    }

    @Test
    public void testAttr() {
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

    @Test
    public void testWhere() {
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

    @Test
    public void testPathBuilder() {
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

    @Test
    public void testBigNum() {
        List<User> users = userQuery.where(get(User::getTimestamp).eq(Double.MAX_VALUE))
                .getList();
        System.out.println(users);
    }

    // @Test
    // public void testLockModeType() {
    //     EntityTransaction transaction = EntityManagers.getEntityManager().getTransaction();
    //     transaction.begin();
    //     for (LockModeType value : LockModeType.values()) {
    //         System.out.println("-----------------");
    //         System.out.println(value);
    //         List<User> ignored = userQuery.where(User::getId).eq(1).getList(value);
    //         System.out.println("-----------------");
    //     }
    // }
    //

    private IntStream getUserIdStream() {
        return allUsers.stream().mapToInt(User::getRandomNumber);
    }
}
