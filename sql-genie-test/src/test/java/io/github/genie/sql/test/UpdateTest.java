package io.github.genie.sql.test;

import io.github.genie.sql.api.Query.Select;
import io.github.genie.sql.api.Updater;
import io.github.genie.sql.test.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class UpdateTest {

    Select<User> query(Updater<User> updater) {
        return updater == UserUpdaterProvider.jdbc
                ? UserQueryProvider.jdbc
                : UserQueryProvider.jpa;
    }

    @ParameterizedTest
    @ArgumentsSource(UserUpdaterProvider.class)
    void insert(Updater<User> userUpdater) {
        Transaction.doInTransaction(() -> doInsert(userUpdater));
    }

    private void doInsert(Updater<User> userUpdater) {
        List<User> existUsers = query(userUpdater).where(User::getId).in(10000000, 10000001, 10000002)
                .getList();
        if (!existUsers.isEmpty()) {
            userUpdater.delete(existUsers);
        }
        List<User> exist = query(userUpdater).where(User::getId).in(10000000, 10000001, 10000002).getList();
        assertTrue(exist.isEmpty());

        User newUser = newUser(10000000);
        userUpdater.insert(newUser);
        User single = query(userUpdater).where(User::getId).eq(10000000).getSingle();
        assertEquals(newUser, single);
        List<User> users = Arrays.asList(newUser(10000001), newUser(10000002));
        userUpdater.insert(users);
        List<User> userList = query(userUpdater).where(User::getId).in(10000001, 10000002).getList();
        assertEquals(userList, new ArrayList<>(users));
        userUpdater.delete(newUser);
        userUpdater.delete(users);
        exist = query(userUpdater).where(User::getId).in(10000000, 10000001, 10000002).getList();
        assertTrue(exist.isEmpty());
    }

    private static User newUser(int id) {
        return Users.newUser(id, "username-" + id, new Random());
    }


    @ParameterizedTest
    @ArgumentsSource(UserUpdaterProvider.class)
    void update(Updater<User> userUpdater) {
        Transaction.doInTransaction(() -> testUpdate(userUpdater));
    }

    private void testUpdate(Updater<User> userUpdater) {
        List<User> users = query(userUpdater).where(User::getId).in(1, 2, 3).getList();
        for (User user : users) {
            user.setRandomNumber(user.getRandomNumber() + 1);
        }
        userUpdater.update(users);
        assertEquals(users, query(userUpdater).where(User::getId).in(1, 2, 3).getList());

        for (User user : users) {
            user.setRandomNumber(user.getRandomNumber() + 1);
            userUpdater.update(user);
        }
        assertEquals(users, query(userUpdater).where(User::getId).in(1, 2, 3).getList());
    }

    @ParameterizedTest
    @ArgumentsSource(UserUpdaterProvider.class)
    void updateNonNullColumn(Updater<User> userUpdater) {
        Transaction.doInTransaction(() -> testUpdateNonNullColumn(userUpdater));
    }

    private void testUpdateNonNullColumn(Updater<User> userUpdater) {
        List<User> users = query(userUpdater).where(User::getId).in(1, 2, 3).getList();
        List<User> users2 = new ArrayList<>(users.size());
        for (User user : users) {
            user = user.clone();
            User user2 = user.clone();
            users2.add(user2);
            int randomNumber = user.getRandomNumber() + 1;
            user.setRandomNumber(randomNumber);
            user2.setRandomNumber(randomNumber);
            user.setUsername(null);
            user.setTime(null);
            user.setPid(null);
            user = userUpdater.updateNonNullColumn(user);
        }
        assertEquals(users2, query(userUpdater).where(User::getId).in(1, 2, 3).getList());

    }

    @Test
    public void test() {
        Transaction.doInTransaction(() -> {
            Updater<User> updater = UserUpdaterProvider.jdbc;
            Select<User> select = UserQueryProvider.jdbc;
            User user = select.where(User::getId).eq(10000006).getSingle();
            if (user != null) {
                updater.delete(user);
            }
            User entity = newUser(10000006);
            updater.insert(entity);
            user = select.where(User::getId).eq(10000006).getSingle();
            assertEquals(entity, user);
            System.out.println(entity.getInstant());
            System.out.println(user.getInstant());
        });

        Transaction.doInTransaction(() -> {
            Updater<User> updater = UserUpdaterProvider.jpa;
            Select<User> select = UserQueryProvider.jpa;
            User user = select.where(User::getId).eq(10000007).getSingle();
            if (user != null) {
                updater.delete(user);
            }
            User entity = newUser(10000007);
            updater.insert(entity);
            user = select.where(User::getId).eq(10000007).getSingle();
            assertEquals(entity, user);
            System.out.println(entity.getInstant());
            System.out.println(user.getInstant());
        });
    }

    @Test
    public void test2() {
        User a = UserQueryProvider.jdbc.where(User::getId).eq(1).getSingle();
        User b = UserQueryProvider.jpa.where(User::getId).eq(1).getSingle();
        System.out.println(a);
        System.out.println(b);
    }
}
