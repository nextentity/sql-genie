package io.github.genie.sql;

import io.github.genie.sql.api.Lists;
import io.github.genie.sql.api.Query.Select;
import io.github.genie.sql.builder.Q;
import io.github.genie.sql.entity.User;
import io.github.genie.sql.projection.UserInterface;
import io.github.genie.sql.projection.UserModel;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

class QueryBuilderTest {

    static List<User> users() {
        return UserDaoProvider.users();
    }

    @ParameterizedTest
    @ArgumentsSource(UserDaoProvider.class)
    void select(Select<User> userQuery) {
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

        Long count = userQuery.select(Q.get(User::getId).count()).getSingle();
        assertEquals(count, users().size());

        Object[] aggArray = userQuery.select(List.of(
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
        return new BigDecimal(num.toString()).setScale(3, RoundingMode.HALF_UP);
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
        User user = userQuery.fetch(User::getParentUser)
                .where(User::getPid).isNotNull()
                .getFirst();

        User parentUser = user.getParentUser();
        for (User u : users()) {
            if (u.getParentUser() != null) {
                assertEquals(parentUser, u.getParentUser());
                break;
            }
        }

        user = userQuery.fetch(Q.get(User::getParentUser).get(User::getParentUser))
                .where(User::getParentUser).get(User::getPid).isNotNull()
                .getFirst();

        parentUser = user.getParentUser().getParentUser();
        for (User u : users()) {
            User p = u.getParentUser();
            if (p != null) {
                p = p.getParentUser();
                if (p != null) {
                    assertEquals(parentUser, p);
                    break;
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

    }

    @ParameterizedTest
    @ArgumentsSource(UserDaoProvider.class)
    void orderBy(Select<User> userQuery) {
    }

    @ParameterizedTest
    @ArgumentsSource(UserDaoProvider.class)
    void getList(Select<User> userQuery) {
    }

    @ParameterizedTest
    @ArgumentsSource(UserDaoProvider.class)
    void first(Select<User> userQuery) {
    }

    @ParameterizedTest
    @ArgumentsSource(UserDaoProvider.class)
    void getFirst(Select<User> userQuery) {
    }

    @ParameterizedTest
    @ArgumentsSource(UserDaoProvider.class)
    void requireSingle(Select<User> userQuery) {
    }

    @ParameterizedTest
    @ArgumentsSource(UserDaoProvider.class)
    void single(Select<User> userQuery) {
    }

    @ParameterizedTest
    @ArgumentsSource(UserDaoProvider.class)
    void getSingle(Select<User> userQuery) {
    }

    @ParameterizedTest
    @ArgumentsSource(UserDaoProvider.class)
    void exist(Select<User> userQuery) {
    }

    @ParameterizedTest
    @ArgumentsSource(UserDaoProvider.class)
    void getResult(Select<User> userQuery) {
    }

    @ParameterizedTest
    @ArgumentsSource(UserDaoProvider.class)
    void slice(Select<User> userQuery) {
    }

    @ParameterizedTest
    @ArgumentsSource(UserDaoProvider.class)
    void getType(Select<User> userQuery) {
    }

    @ParameterizedTest
    @ArgumentsSource(UserDaoProvider.class)
    void where(Select<User> userQuery) {
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
}