package io.github.genie.sql;

import io.github.genie.sql.api.EntityRoot;
import io.github.genie.sql.api.ExpressionHolder;
import io.github.genie.sql.api.Lists;
import io.github.genie.sql.api.Path;
import io.github.genie.sql.api.Path.ComparablePath;
import io.github.genie.sql.api.Query;
import io.github.genie.sql.api.Query.OrderBy;
import io.github.genie.sql.api.Query.Select;
import io.github.genie.sql.api.Query.Where;
import io.github.genie.sql.api.TypedExpression.BooleanExpression;
import io.github.genie.sql.builder.ExpressionHolders;
import io.github.genie.sql.builder.Q;
import io.github.genie.sql.entity.User;
import io.github.genie.sql.projection.IUser;
import io.github.genie.sql.projection.UserInterface;
import io.github.genie.sql.projection.UserModel;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.github.genie.sql.builder.Q.get;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class QueryBuilderTest {

    static List<User> users() {
        return UserDaoProvider.users();
    }

    @ParameterizedTest
    @ArgumentsSource(UserDaoProvider.class)
    void select(Select<User> userQuery) {

        IUser first1 = userQuery.select(IUser.class).getFirst(20);
        System.out.println(first1);
        User first3 = userQuery.fetch(User::getParentUser).getFirst(20);
        System.out.println(first3);
        System.out.println(first3.getParentUser());
        IUser.U first2 = userQuery.select(IUser.U.class).getFirst(20);
        System.out.println(first2);

        User first = userQuery.select(User.class).getFirst();
        assertEquals(first, users().get(0));

        Integer firstUserid = userQuery.select(User::getId).getFirst();
        assertEquals(firstUserid, users().get(0).getId());

        Object[] array = userQuery.select(User::getId, User::getRandomNumber).getFirst();
        assertEquals(array[0], users().get(0).getId());
        assertEquals(array[1], users().get(0).getRandomNumber());

        UserModel model = userQuery.select(UserModel.class).getFirst();
        assertEquals(model, new UserModel(users().get(0)));

        UserInterface ui = userQuery.select(UserInterface.class).getFirst();
        assertEquals(model.asMap(), ui.asMap());

        ui = userQuery.selectDistinct(UserInterface.class).getFirst();
        assertEquals(model.asMap(), ui.asMap());

        Long count = userQuery.select(Q.get(User::getId).count()).getSingle();
        assertEquals(count, users().size());

        Object[] aggArray = userQuery.select(Lists.of(
                Q.get(User::getId).count(),
                Q.get(User::getRandomNumber).max(),
                Q.get(User::getRandomNumber).min(),
                Q.get(User::getRandomNumber).sum(),
                Q.get(User::getRandomNumber).avg()
        )).getSingle();

        int max = Integer.MIN_VALUE;
        int min = Integer.MAX_VALUE;
        int sum = 0;
        for (User user : users()) {
            int number = user.getRandomNumber();
            max = Math.max(max, number);
            min = Math.min(min, number);
            sum += number;
        }

        assertEquals(aggArray[0], (long) users().size());
        assertEquals(aggArray[1], max);
        assertEquals(aggArray[2], min);
        assertEquals(((Number) aggArray[3]).intValue(), sum);
        assertEquals(
                asBigDecimal(aggArray[4]),
                asBigDecimal(sum * 1.0 / users().size())
        );

    }

    BigDecimal asBigDecimal(Object num) {
        return new BigDecimal(num.toString()).setScale(4, RoundingMode.HALF_UP);
    }

    @ParameterizedTest
    @ArgumentsSource(UserDaoProvider.class)
    void selectDistinct(Select<User> userQuery) {
        List<Integer> list = userQuery.selectDistinct(User::getRandomNumber)
                .getList();
        List<Integer> collect = users()
                .stream().map(User::getRandomNumber)
                .distinct()
                .collect(Collectors.toList());
        assertEquals(list, collect);
    }

    @ParameterizedTest
    @ArgumentsSource(UserDaoProvider.class)
    void fetch(Select<User> userQuery) {
        List<User> users = userQuery.fetch(User::getParentUser).getList();

        assertEquals(users, users());
        for (int i = 0; i < users().size(); i++) {
            User a = users.get(i);
            User b = users().get(i);
            if (b.getParentUser() != null) {
                assertEquals(b.getParentUser(), a.getParentUser());
            } else {
                assertNull(a.getParentUser());
            }
        }

        users = userQuery.fetch(Q.get(User::getParentUser).get(User::getParentUser))
                .getList();

        assertEquals(users, users());
        for (int i = 0; i < users().size(); i++) {
            User a = users.get(i);
            User b = users().get(i);
            if (b.getParentUser() != null) {
                b = b.getParentUser();
                a = a.getParentUser();
                assertNotNull(a);
                if (b.getParentUser() != null) {
                    assertEquals(b.getParentUser(), a.getParentUser());
                }
            }
        }

    }

    @ParameterizedTest
    @ArgumentsSource(UserDaoProvider.class)
    void groupBy(Select<User> userQuery) {
        List<Object[]> list = userQuery
                .select(Lists.of(
                        Q.get(User::getRandomNumber),
                        Q.get(User::getId).count()
                ))
                .groupBy(User::getRandomNumber)
                .getList();

        Map<Object, Long> count = users().stream()
                .collect(Collectors.groupingBy(User::getRandomNumber, Collectors.counting()));

        assertEquals(list.size(), count.size());
        for (Object[] objects : list) {
            Long value = count.get(objects[0]);
            assertEquals(value, objects[1]);
        }

        Function<EntityRoot<User>, List<? extends ExpressionHolder<User, ?>>> expressionBuilder =
                (EntityRoot<User> root) -> Lists.of(root.get(User::getRandomNumber));
        list = userQuery
                .select(Lists.of(
                        Q.get(User::getRandomNumber),
                        Q.get(User::getId).count()
                ))
                .groupBy(expressionBuilder)
                .getList();

        assertEquals(list.size(), count.size());
        for (Object[] objects : list) {
            Long value = count.get(objects[0]);
            assertEquals(value, objects[1]);
        }

        list = userQuery
                .select(Lists.of(
                        Q.get(User::getRandomNumber),
                        Q.get(User::getId).count()
                ))
                .groupBy(Lists.<Path<User, ?>>of(User::getRandomNumber))
                .getList();

        assertEquals(list.size(), count.size());
        for (Object[] objects : list) {
            Long value = count.get(objects[0]);
            assertEquals(value, objects[1]);
        }

        list = userQuery
                .select(Lists.of(
                        Q.get(User::getRandomNumber),
                        Q.get(User::getId).count()
                ))
                .where(User::isValid)
                .and(User::getRandomNumber).eq(1)
                .groupBy(expressionBuilder)
                .getList();
        count = users().stream()
                .filter(it -> it.isValid() && it.getRandomNumber() == 1)
                .collect(Collectors.groupingBy(User::getRandomNumber, Collectors.counting()));
        assertEquals(list.size(), count.size());
        for (Object[] objects : list) {
            Long value = count.get(objects[0]);
            assertEquals(value, objects[1]);
        }

    }

    @ParameterizedTest
    @ArgumentsSource(UserDaoProvider.class)
    void orderBy(Select<User> userQuery) {
        for (Checker<User, OrderBy<User, User>> checker : getWhereTestCase(new Checker<>(users(), userQuery))) {
            ArrayList<User> sorted = new ArrayList<>(checker.expected);
            sorted.sort(Comparator.comparingInt(User::getRandomNumber));
            List<User> users = checker.collector
                    .orderBy(User::getRandomNumber, User::getId).asc()
                    .getList();
            assertEquals(users, sorted);
            users = checker.collector
                    .orderBy((EntityRoot<User> r) -> Lists.of(
                            r.get(User::getRandomNumber).asc(),
                            r.get(User::getId).asc()
                    ))
                    .getList();
            assertEquals(users, sorted);
            users = checker.collector.orderBy(User::getRandomNumber, User::getId)
                    .getList();
            assertEquals(users, sorted);
            users = checker.collector.orderBy(User::getRandomNumber, User::getId).desc()
                    .getList();
            sorted = new ArrayList<>(checker.expected);
            sorted.sort((a, b) -> {
                int compare = Integer.compare(b.getRandomNumber(), a.getRandomNumber());
                if (compare == 0) {
                    compare = Integer.compare(b.getId(), a.getId());
                }
                return compare;
            });
            assertEquals(users, sorted);

            users = checker.collector.orderBy(User::getRandomNumber).desc()
                    .orderBy(User::getId)
                    .getList();
            sorted = new ArrayList<>(checker.expected);
            sorted.sort((a, b) -> {
                int compare = Integer.compare(b.getRandomNumber(), a.getRandomNumber());
                if (compare == 0) {
                    compare = Integer.compare(a.getId(), b.getId());
                }
                return compare;
            });
            assertEquals(users, sorted);
        }

    }

    @ParameterizedTest
    @ArgumentsSource(UserDaoProvider.class)
    void getList(Select<User> userQuery) {

        List<User> users = userQuery.getList();
        assertEquals(users.size(), users().size());

        users = userQuery.getList(0, 10);
        assertEquals(users, users().subList(0, 10));

        users = userQuery.getList(100, 15);
        assertEquals(users, users().subList(100, 115));

    }

    @ParameterizedTest
    @ArgumentsSource(UserDaoProvider.class)
    void where(Select<User> userQuery) {
        Checker<User, Where<User, User>> check = new Checker<>(users(), userQuery);
        getWhereTestCase(check);
    }

    private List<Checker<User, OrderBy<User, User>>> getWhereTestCase(Checker<User, Where<User, User>> check) {
        List<Checker<User, OrderBy<User, User>>> result = new ArrayList<>();
        String username = users().get(10).getUsername();

        Where<User, User> userQuery = check.collector;
        OrderBy<User, User> collector = userQuery
                .where(get(User::getParentUser).get(User::getUsername).eq(username));
        Stream<User> stream = newStream(check)
                .filter(user -> user.getParentUser() != null && username.equals(user.getParentUser().getUsername()));
        result.add(new Checker<>(stream, collector));

        stream = newStream(check)
                .filter(user -> user.getParentUser() != null && !username.equals(user.getParentUser().getUsername()));
        collector = userQuery
                .where(get(User::getParentUser).get(User::getUsername).ne(username));
        result.add(new Checker<>(stream, collector));

        collector = userQuery
                .where(get(User::getUsername).ne(username));
        stream = newStream(check)
                .filter(user -> !username.equals(user.getUsername()));
        result.add(new Checker<>(stream, collector));

        collector = userQuery
                .where(get(User::getUsername).ne(username));
        stream = newStream(check)
                .filter(user -> !username.equals(user.getUsername()));
        result.add(new Checker<>(stream, collector));

        collector = userQuery
                .where(get(User::getUsername).ne(username));
        stream = newStream(check)
                .filter(user -> !username.equals(user.getUsername()));
        result.add(new Checker<>(stream, collector));


        BooleanExpression<User> isValid = get(User::isValid);
        collector = userQuery.where(isValid);
        stream = users().stream().filter(User::isValid);

        result.add(new Checker<>(stream, collector));

        collector = userQuery.where(isValid.and(User::getRandomNumber).eq(2));
        stream = newStream(check).filter(User::isValid).filter(user -> user.getRandomNumber() == 2);
        result.add(new Checker<>(stream, collector));

        collector = userQuery.where(isValid.and(User::getPid).ne(2));
        stream = newStream(check).filter(User::isValid).filter(user -> user.getPid() != null && user.getPid() != 2);
        result.add(new Checker<>(stream, collector));

        collector = userQuery.where(isValid.and(User::getRandomNumber).in(1, 2, 3));
        stream = newStream(check).filter(User::isValid).filter(user -> Arrays.asList(1, 2, 3).contains(user.getRandomNumber()));
        result.add(new Checker<>(stream, collector));

        collector = userQuery.where(isValid.and(User::getRandomNumber).notIn(1, 2, 3));
        stream = newStream(check).filter(User::isValid).filter(user -> !Arrays.asList(1, 2, 3).contains(user.getRandomNumber()));
        result.add(new Checker<>(stream, collector));

        collector = userQuery.where(isValid.and(User::getPid).isNull());
        stream = newStream(check).filter(User::isValid).filter(user -> user.getPid() == null);
        result.add(new Checker<>(stream, collector));

        collector = userQuery.where(isValid.and(User::getRandomNumber).ge(10));
        stream = newStream(check).filter(User::isValid).filter(user -> user.getRandomNumber() >= 10);
        result.add(new Checker<>(stream, collector));

        collector = userQuery.where(isValid.and(User::getRandomNumber).gt(10));
        stream = newStream(check).filter(User::isValid).filter(user -> user.getRandomNumber() > 10);
        result.add(new Checker<>(stream, collector));

        collector = userQuery.where(isValid.and(User::getRandomNumber).le(10));
        stream = newStream(check).filter(User::isValid).filter(user -> user.getRandomNumber() <= 10);
        result.add(new Checker<>(stream, collector));

        collector = userQuery.where(isValid.and(User::getRandomNumber).lt(10));
        stream = newStream(check).filter(User::isValid).filter(user -> user.getRandomNumber() < 10);
        result.add(new Checker<>(stream, collector));

        collector = userQuery.where(isValid.and(User::getRandomNumber).between(10, 15));
        stream = newStream(check).filter(User::isValid).filter(user -> user.getRandomNumber() >= 10 && user.getRandomNumber() <= 15);
        result.add(new Checker<>(stream, collector));

        collector = userQuery.where(isValid.and(User::getRandomNumber).notBetween(10, 15));
        stream = newStream(check).filter(User::isValid).filter(user -> user.getRandomNumber() < 10 || user.getRandomNumber() > 15);
        result.add(new Checker<>(stream, collector));

        collector = userQuery.where(isValid
                .and(User::getRandomNumber).notBetween(10, 15)
                .and(User::getId).mod(3).eq(0)
        );
        stream = newStream(check).filter(User::isValid).filter(user ->
                !(user.getRandomNumber() >= 10 && user.getRandomNumber() <= 15)
                        && user.getId() % 3 == 0);
        result.add(new Checker<>(stream, collector));

        collector = userQuery.where(isValid.and(User::getRandomNumber).ge(get(User::getPid)));
        stream = newStream(check).filter(User::isValid)
                .filter(user -> user.getPid() != null && user.getRandomNumber() >= user.getPid());
        result.add(new Checker<>(stream, collector));

        collector = userQuery.where(isValid.and(User::getRandomNumber).gt(get(User::getPid)));
        stream = newStream(check).filter(User::isValid)
                .filter(user -> user.getPid() != null && user.getRandomNumber() > user.getPid());
        result.add(new Checker<>(stream, collector));

        collector = userQuery.where(isValid.and(User::getRandomNumber).le(get(User::getPid)));
        stream = newStream(check).filter(User::isValid)
                .filter(user -> user.getPid() != null && user.getRandomNumber() <= user.getPid());
        result.add(new Checker<>(stream, collector));

        collector = userQuery.where(isValid.and(User::getRandomNumber).lt(get(User::getPid)));
        stream = newStream(check).filter(User::isValid)
                .filter(user -> user.getPid() != null && user.getRandomNumber() < user.getPid());
        result.add(new Checker<>(stream, collector));

        collector = userQuery.where(isValid.and(User::getRandomNumber)
                .between(get(User::getRandomNumber), get(User::getPid)));
        stream = newStream(check).filter(User::isValid)
                .filter(user -> user.getPid() != null && user.getRandomNumber() >= user.getRandomNumber() && user.getRandomNumber() <= user.getPid());
        result.add(new Checker<>(stream, collector));
        for (Checker<User, OrderBy<User, User>> checker : getExpressionOperatorCase(check)) {
            addTestCaseAndCheck(result, checker);
        }
        return result;
    }

    private List<Checker<User, OrderBy<User, User>>> getExpressionOperatorCase(Checker<User, Where<User, User>> check) {
        List<Checker<User, OrderBy<User, User>>> result = new ArrayList<>();
        // B eq(U value);
        List<User> users = check.expected.stream().filter(it -> it.getRandomNumber() == 1).collect(Collectors.toList());
        OrderBy<User, User> collector = check.collector.where(User::getRandomNumber).eq(1);
        result.add(new Checker<>(users, collector));
        collector = check.collector.where((ComparablePath<User, Integer>) User::getRandomNumber).eq(1);
        result.add(new Checker<>(users, collector));
        collector = check.collector.where(Q.get(User::getRandomNumber).eq(1));
        result.add(new Checker<>(users, collector));
        users = check.expected.stream()
                .filter(it -> it.getRandomNumber() == 1 || it.getRandomNumber() == 2)
                .collect(Collectors.toList());
        collector = check.collector.where(Q.get(User::getRandomNumber).eq(1).or(User::getRandomNumber).eq(2));
        result.add(new Checker<>(users, collector));
        collector = check.collector.where(Q.get(User::getRandomNumber).eq(1).or((ComparablePath<User, Integer>) User::getRandomNumber).eq(2));
        result.add(new Checker<>(users, collector));
        collector = check.collector.where(Q.get(User::getRandomNumber).eq(1).or(Lists.of(Q.get(User::getRandomNumber).eq(2))));
        result.add(new Checker<>(users, collector));
        users = check.expected.stream()
                .filter(it -> it.getRandomNumber() == 1 && it.isValid())
                .collect(Collectors.toList());
        collector = check.collector.where(User::getRandomNumber).eq(1).and(User::isValid);
        result.add(new Checker<>(users, collector));
        collector = check.collector.where(Q.get(User::getRandomNumber).eq(1).and(User::isValid));
        result.add(new Checker<>(users, collector));
        collector = check.collector.where(Q.get(User::getRandomNumber).eq(1).and(Lists.of(Q.get(User::isValid))));
        result.add(new Checker<>(users, collector));
        //
        //    B eq(ExpressionHolder<T, U> expression);
        users = check.expected.stream().filter(it -> it.getRandomNumber() == it.getId()).collect(Collectors.toList());
        collector = check.collector.where(User::getRandomNumber).eq(Q.get(User::getId));
        result.add(new Checker<>(users, collector));
        collector = check.collector.where(Q.get(User::getRandomNumber).eq(Q.get(User::getId)));
        result.add(new Checker<>(users, collector));
        collector = check.collector.where((EntityRoot<User> root) -> root.get(User::getRandomNumber).eq(root.get(User::getId)));
        result.add(new Checker<>(users, collector));


        //
        //    B ne(U value);
        users = check.expected.stream().filter(it -> it.getRandomNumber() != 1).collect(Collectors.toList());
        collector = check.collector.where(User::getRandomNumber).ne(1);
        result.add(new Checker<>(users, collector));
        collector = check.collector.where(Q.get(User::getRandomNumber).ne(1));
        result.add(new Checker<>(users, collector));
        //
        //    B ne(ExpressionHolder<T, U> expression);
        users = check.expected.stream().filter(it -> it.getRandomNumber() != it.getId()).collect(Collectors.toList());
        collector = check.collector.where(User::getRandomNumber).ne(Q.get(User::getId));
        result.add(new Checker<>(users, collector));
        collector = check.collector.where(Q.get(User::getRandomNumber).ne(Q.get(User::getId)));
        result.add(new Checker<>(users, collector));
        users = check.expected.stream().filter(User::isValid).collect(Collectors.toList());
        collector = check.collector.where(User::isValid);
        result.add(new Checker<>(users, collector));
        //
        //    @SuppressWarnings({"unchecked"})
        //    B in(U... values);
        for (int i = 0; i <= 5; i++) {
            Integer[] nums = new Integer[i];
            for (int j = 0; j < i; j++) {
                nums[j] = j;
            }
            List<Integer> values = Arrays.asList(nums);
            users = check.expected.stream().filter(it -> values.contains(it.getRandomNumber()))
                    .collect(Collectors.toList());
            collector = check.collector.where(User::getRandomNumber).in(nums);
            result.add(new Checker<>(users, collector));
            collector = check.collector.where(User::getRandomNumber).in(values);
            result.add(new Checker<>(users, collector));
            collector = check.collector.where(Q.get(User::getRandomNumber).in(nums));
            result.add(new Checker<>(users, collector));
            collector = check.collector.where(Q.get(User::getRandomNumber).in(values));
            result.add(new Checker<>(users, collector));

            List<ExpressionHolder<User, Integer>> collect = values.stream()
                    .<ExpressionHolder<User, Integer>>map(ExpressionHolders::of)
                    .collect(Collectors.toList());
            collector = check.collector.where(User::getRandomNumber).in(collect);
            result.add(new Checker<>(users, collector));
            collector = check.collector.where(Q.get(User::getRandomNumber).in(collect));
            result.add(new Checker<>(users, collector));

            users = check.expected.stream().filter(it -> !values.contains(it.getRandomNumber()))
                    .collect(Collectors.toList());
            collector = check.collector.where(User::getRandomNumber).notIn(nums);
            result.add(new Checker<>(users, collector));
            collector = check.collector.where(Q.get(User::getRandomNumber).notIn(nums));
            result.add(new Checker<>(users, collector));
            collector = check.collector.where(User::getRandomNumber).notIn(values);
            result.add(new Checker<>(users, collector));
            collector = check.collector.where(Q.get(User::getRandomNumber).notIn(values));
            result.add(new Checker<>(users, collector));
            collector = check.collector.where(User::getRandomNumber).notIn(collect);
            result.add(new Checker<>(users, collector));
            collector = check.collector.where(Q.get(User::getRandomNumber).notIn(collect));
            result.add(new Checker<>(users, collector));

        }

        //
        //    B isNull();
        users = check.expected.stream().filter(it -> it.getPid() == null).collect(Collectors.toList());
        collector = check.collector.where(User::getPid).isNull();
        result.add(new Checker<>(users, collector));
        collector = check.collector.where(Q.get(User::getPid).isNull());
        result.add(new Checker<>(users, collector));
        //
        //    B isNotNull();
        users = check.expected.stream().filter(it -> it.getPid() != null).collect(Collectors.toList());
        collector = check.collector.where(User::getPid).isNotNull();
        result.add(new Checker<>(users, collector));
        collector = check.collector.where(Q.get(User::getPid).isNotNull());
        result.add(new Checker<>(users, collector));


        //  B ge(U value);
        users = check.expected.stream().filter(it -> it.getRandomNumber() >= 50).collect(Collectors.toList());
        collector = check.collector.where(User::getRandomNumber).ge(50);
        result.add(new Checker<>(users, collector));
        collector = check.collector.where(Q.get(User::getRandomNumber).ge(50));
        result.add(new Checker<>(users, collector));
        collector = check.collector.where(User::getRandomNumber).ge(ExpressionHolders.of(50));
        result.add(new Checker<>(users, collector));
        collector = check.collector.where(Q.get(User::getRandomNumber).ge(ExpressionHolders.of(50)));
        result.add(new Checker<>(users, collector));

        //
        //        B gt(U value);
        users = check.expected.stream().filter(it -> it.getRandomNumber() > 50).collect(Collectors.toList());
        collector = check.collector.where(User::getRandomNumber).gt(50);
        result.add(new Checker<>(users, collector));
        collector = check.collector.where(Q.get(User::getRandomNumber).gt(50));
        result.add(new Checker<>(users, collector));
        collector = check.collector.where(User::getRandomNumber).gt(ExpressionHolders.of(50));
        result.add(new Checker<>(users, collector));
        collector = check.collector.where(Q.get(User::getRandomNumber).gt(ExpressionHolders.of(50)));
        result.add(new Checker<>(users, collector));
        //
        //        B le(U value);
        users = check.expected.stream().filter(it -> it.getRandomNumber() <= 50).collect(Collectors.toList());
        collector = check.collector.where(User::getRandomNumber).le(50);
        result.add(new Checker<>(users, collector));
        collector = check.collector.where(Q.get(User::getRandomNumber).le(50));
        result.add(new Checker<>(users, collector));
        collector = check.collector.where(User::getRandomNumber).le(ExpressionHolders.of(50));
        result.add(new Checker<>(users, collector));
        collector = check.collector.where(Q.get(User::getRandomNumber).le(ExpressionHolders.of(50)));
        result.add(new Checker<>(users, collector));
        //
        //        B lt(U value);
        users = check.expected.stream().filter(it -> it.getRandomNumber() < 50).collect(Collectors.toList());
        collector = check.collector.where(User::getRandomNumber).lt(50);
        result.add(new Checker<>(users, collector));
        collector = check.collector.where(Q.get(User::getRandomNumber).lt(50));
        result.add(new Checker<>(users, collector));
        collector = check.collector.where(User::getRandomNumber).lt(ExpressionHolders.of(50));
        result.add(new Checker<>(users, collector));
        collector = check.collector.where(Q.get(User::getRandomNumber).lt(ExpressionHolders.of(50)));
        result.add(new Checker<>(users, collector));
        //
        //        B between(U l, U r);

        users = check.expected.stream().filter(it -> {
                    int number = it.getRandomNumber();
                    return number >= 25 && number <= 73;
                })
                .collect(Collectors.toList());
        collector = check.collector.where(User::getRandomNumber).between(25, 73);
        result.add(new Checker<>(users, collector));
        collector = check.collector.where(Q.get(User::getRandomNumber).between(25, 73));
        result.add(new Checker<>(users, collector));
        collector = check.collector.where(User::getRandomNumber).between(25, ExpressionHolders.of(73));
        result.add(new Checker<>(users, collector));
        collector = check.collector.where(Q.get(User::getRandomNumber).between(25, ExpressionHolders.of(73)));
        result.add(new Checker<>(users, collector));
        collector = check.collector.where(User::getRandomNumber).between(ExpressionHolders.of(25), 73);
        result.add(new Checker<>(users, collector));
        collector = check.collector.where(Q.get(User::getRandomNumber).between(ExpressionHolders.of(25), 73));
        result.add(new Checker<>(users, collector));
        collector = check.collector.where(User::getRandomNumber)
                .between(ExpressionHolders.of(25), ExpressionHolders.of(73));
        result.add(new Checker<>(users, collector));
        collector = check.collector.where(Q.get(User::getRandomNumber)
                .between(ExpressionHolders.of(25), ExpressionHolders.of(73)));
        result.add(new Checker<>(users, collector));

        //
        //        B notBetween(U l, U r);
        users = check.expected.stream().filter(it -> {
                    int number = it.getRandomNumber();
                    return number < 25 || number > 73;
                })
                .collect(Collectors.toList());
        collector = check.collector.where(User::getRandomNumber).notBetween(25, 73);
        result.add(new Checker<>(users, collector));
        collector = check.collector.where(Q.get(User::getRandomNumber).notBetween(25, 73));
        result.add(new Checker<>(users, collector));
        collector = check.collector.where(User::getRandomNumber).notBetween(25, ExpressionHolders.of(73));
        result.add(new Checker<>(users, collector));
        collector = check.collector.where(Q.get(User::getRandomNumber).notBetween(25, ExpressionHolders.of(73)));
        result.add(new Checker<>(users, collector));
        collector = check.collector.where(User::getRandomNumber).notBetween(ExpressionHolders.of(25), 73);
        result.add(new Checker<>(users, collector));
        collector = check.collector.where(Q.get(User::getRandomNumber).notBetween(ExpressionHolders.of(25), 73));
        result.add(new Checker<>(users, collector));
        collector = check.collector.where(User::getRandomNumber)
                .notBetween(ExpressionHolders.of(25), ExpressionHolders.of(73));
        result.add(new Checker<>(users, collector));
        collector = check.collector.where(Q.get(User::getRandomNumber)
                .notBetween(ExpressionHolders.of(25), ExpressionHolders.of(73)));
        result.add(new Checker<>(users, collector));

        //   NumberOperator<T, U, B> add(U value);
        users = check.expected.stream().filter(it -> it.getRandomNumber() + 1 == 5).collect(Collectors.toList());
        collector = check.collector.where(User::getRandomNumber).add(1).eq(5);
        result.add(new Checker<>(users, collector));
        collector = check.collector.where(Q.get(User::getRandomNumber).add(1).eq(5));
        result.add(new Checker<>(users, collector));
        collector = check.collector.where(User::getRandomNumber).add(ExpressionHolders.of(1)).eq(5);
        result.add(new Checker<>(users, collector));
        collector = check.collector.where(Q.get(User::getRandomNumber).add(ExpressionHolders.of(1)).eq(5));
        result.add(new Checker<>(users, collector));

        //
        //        NumberOperator<T, U, B> subtract(U value);
        users = check.expected.stream().filter(it -> it.getRandomNumber() - 1 == 5).collect(Collectors.toList());
        collector = check.collector.where(User::getRandomNumber).subtract(1).eq(5);
        result.add(new Checker<>(users, collector));
        collector = check.collector.where(Q.get(User::getRandomNumber).subtract(1).eq(5));
        result.add(new Checker<>(users, collector));
        collector = check.collector.where(User::getRandomNumber).subtract(ExpressionHolders.of(1)).eq(5);
        result.add(new Checker<>(users, collector));
        collector = check.collector.where(Q.get(User::getRandomNumber).subtract(ExpressionHolders.of(1)).eq(5));
        result.add(new Checker<>(users, collector));
        //
        //        NumberOperator<T, U, B> multiply(U value);
        users = check.expected.stream().filter(it -> it.getRandomNumber() * 3 == 45).collect(Collectors.toList());
        collector = check.collector.where(User::getRandomNumber).multiply(3).eq(45);
        result.add(new Checker<>(users, collector));
        collector = check.collector.where(Q.get(User::getRandomNumber).multiply(3).eq(45));
        result.add(new Checker<>(users, collector));
        collector = check.collector.where(User::getRandomNumber).multiply(ExpressionHolders.of(3)).eq(45);
        result.add(new Checker<>(users, collector));
        collector = check.collector.where(Q.get(User::getRandomNumber).multiply(ExpressionHolders.of(3)).eq(45));
        result.add(new Checker<>(users, collector));
        //
        //        NumberOperator<T, U, B> divide(U value);
        users = check.expected.stream().filter(it -> it.getRandomNumber() / 3 == 12).collect(Collectors.toList());
        collector = check.collector.where(User::getRandomNumber).divide(3).eq(12);
        result.add(new Checker<>(users, collector));
        collector = check.collector.where(Q.get(User::getRandomNumber).divide(3).eq(12));
        result.add(new Checker<>(users, collector));
        collector = check.collector.where(User::getRandomNumber).divide(ExpressionHolders.of(3)).eq(12);
        result.add(new Checker<>(users, collector));
        collector = check.collector.where(Q.get(User::getRandomNumber).divide(ExpressionHolders.of(3)).eq(12));
        result.add(new Checker<>(users, collector));

        //
        //        NumberOperator<T, U, B> mod(U value);

        users = check.expected.stream().filter(it -> it.getRandomNumber() % 8 == 2).collect(Collectors.toList());
        collector = check.collector.where(User::getRandomNumber).mod(8).eq(2);
        result.add(new Checker<>(users, collector));
        collector = check.collector.where(Q.get(User::getRandomNumber).mod(8).eq(2));
        result.add(new Checker<>(users, collector));
        collector = check.collector.where(User::getRandomNumber).mod(ExpressionHolders.of(8)).eq(2);
        result.add(new Checker<>(users, collector));
        collector = check.collector.where(Q.get(User::getRandomNumber).mod(ExpressionHolders.of(8)).eq(2));
        result.add(new Checker<>(users, collector));


        //   B like(String value);

        users = check.expected.stream().filter(it -> it.getUsername().contains("one")).collect(Collectors.toList());
        collector = check.collector.where(User::getUsername).like("%one%");
        result.add(new Checker<>(users, collector));
        collector = check.collector.where(Q.get(User::getUsername).like("%one%"));
        result.add(new Checker<>(users, collector));
        collector = check.collector.where(User::getUsername).contains("one");
        result.add(new Checker<>(users, collector));
        collector = check.collector.where(Q.get(User::getUsername).contains("one"));
        result.add(new Checker<>(users, collector));
        //
        //        default B startWith(String value) {
        //            return like(value + '%');
        //        }
        users = check.expected.stream().filter(it -> it.getUsername().startsWith("Ja")).collect(Collectors.toList());
        collector = check.collector.where(User::getUsername).startWith("Ja");
        result.add(new Checker<>(users, collector));
        collector = check.collector.where(Q.get(User::getUsername).startWith("Ja"));
        result.add(new Checker<>(users, collector));
        //
        //        default B endsWith(String value) {
        //            return like('%' + value);
        //        }
        users = check.expected.stream().filter(it -> it.getUsername().endsWith("win")).collect(Collectors.toList());
        collector = check.collector.where(User::getUsername).endsWith("win");
        result.add(new Checker<>(users, collector));
        collector = check.collector.where(Q.get(User::getUsername).endsWith("win"));
        result.add(new Checker<>(users, collector));


        ////
        users = check.expected.stream().filter(it -> !it.getUsername().contains("one")).collect(Collectors.toList());
        collector = check.collector.where(User::getUsername).notLike("%one%");
        result.add(new Checker<>(users, collector));
        collector = check.collector.where(Q.get(User::getUsername).notLike("%one%"));
        result.add(new Checker<>(users, collector));
        collector = check.collector.where(User::getUsername).notContains("one");
        result.add(new Checker<>(users, collector));
        collector = check.collector.where(Q.get(User::getUsername).notContains("one"));
        result.add(new Checker<>(users, collector));
        //
        //        default B startWith(String value) {
        //            return like(value + '%');
        //        }
        users = check.expected.stream().filter(it -> !it.getUsername().startsWith("Ja")).collect(Collectors.toList());
        collector = check.collector.where(User::getUsername).notStartWith("Ja");
        result.add(new Checker<>(users, collector));
        collector = check.collector.where(Q.get(User::getUsername).notStartWith("Ja"));
        result.add(new Checker<>(users, collector));
        //
        //        default B endsWith(String value) {
        //            return like('%' + value);
        //        }
        users = check.expected.stream().filter(it -> !it.getUsername().endsWith("win")).collect(Collectors.toList());
        collector = check.collector.where(User::getUsername).notEndsWith("win");
        result.add(new Checker<>(users, collector));
        collector = check.collector.where(Q.get(User::getUsername).notEndsWith("win"));
        result.add(new Checker<>(users, collector));

        //
        //        StringOperator<T, B> lower();
        users = check.expected.stream().filter(it -> !it.getUsername().toLowerCase().startsWith("ja")).collect(Collectors.toList());
        collector = check.collector.where(User::getUsername).lower().notStartWith("ja");
        result.add(new Checker<>(users, collector));
        collector = check.collector.where(Q.get(User::getUsername).lower().notStartWith("ja"));
        result.add(new Checker<>(users, collector));
        //
        //        StringOperator<T, B> upper();
        users = check.expected.stream().filter(it -> !it.getUsername().toUpperCase().startsWith("JA")).collect(Collectors.toList());
        collector = check.collector.where(User::getUsername).upper().notStartWith("JA");
        result.add(new Checker<>(users, collector));
        collector = check.collector.where(Q.get(User::getUsername).upper().notStartWith("JA"));
        result.add(new Checker<>(users, collector));
        //
        //        StringOperator<T, B> substring(int a, int b);
        users = check.expected.stream().filter(it -> it.getUsername().startsWith("ar", 1)).collect(Collectors.toList());
        collector = check.collector.where(User::getUsername).substring(2, 2).eq("ar");
        result.add(new Checker<>(users, collector));
        collector = check.collector.where(Q.get(User::getUsername).substring(2, 2).eq("ar"));
        result.add(new Checker<>(users, collector));
        //
        //        StringOperator<T, B> substring(int a);
        users = check.expected.stream().filter(it -> {
                    String username = it.getUsername();
                    return username.length() == 17 && username.endsWith("ing");
                })
                .collect(Collectors.toList());
        collector = check.collector.where(User::getUsername).substring(15).eq("ing");
        result.add(new Checker<>(users, collector));
        collector = check.collector.where(Q.get(User::getUsername).substring(15).eq("ing"));
        result.add(new Checker<>(users, collector));

        users = check.expected.stream().filter(it -> {
                    String username = it.getUsername();
                    return username.length() == 17;
                })
                .collect(Collectors.toList());
        collector = check.collector.where(User::getUsername).length().eq(17);
        result.add(new Checker<>(users, collector));
        collector = check.collector.where(Q.get(User::getUsername).length().eq(17));
        result.add(new Checker<>(users, collector));
        return result;
    }

    private static <T, U extends Query.Collector<T>> void addTestCaseAndCheck(List<Checker<T, U>> result, Checker<T, U> checker) {
        result.add(checker);
    }

    private static <T> Stream<T> newStream(Checker<T, ?> check) {
        return check.expected.stream();
    }

    @ParameterizedTest
    @ArgumentsSource(UserDaoProvider.class)
    void count(Select<User> userQuery) {
    }

    @ParameterizedTest
    @ArgumentsSource(UserDaoProvider.class)
    void requiredCountSubQuery(Select<User> userQuery) {
    }

    @ParameterizedTest
    @ArgumentsSource(UserDaoProvider.class)
    void queryList(Select<User> userQuery) {
    }

    @ParameterizedTest
    @ArgumentsSource(UserDaoProvider.class)
    void buildMetadata(Select<User> userQuery) {
    }

    @ParameterizedTest
    @ArgumentsSource(UserDaoProvider.class)
    void having(Select<User> userQuery) {
    }

    @ParameterizedTest
    @ArgumentsSource(UserDaoProvider.class)
    void root(Select<User> userQuery) {
    }

    static class Checker<T, U extends Query.Collector<T>> {
        List<T> expected;

        U collector;

        Checker(Stream<T> expected, U collector) {
            this(expected.collect(Collectors.toList()), collector);
        }

        Checker(List<T> expected, U collector) {
            this.expected = expected;
            this.collector = collector;
            check();
        }

        void check() {
            List<T> actual = collector.getList();
            assertEquals(expected, actual);
        }

    }
}