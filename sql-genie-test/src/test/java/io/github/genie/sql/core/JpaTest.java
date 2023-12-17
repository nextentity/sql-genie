package io.github.genie.sql.core;

import io.github.genie.sql.core.Query.Select0;
import io.github.genie.sql.core.entity.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaQuery;
import lombok.Lombok;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.junit.jupiter.api.BeforeAll;

import java.util.List;
import java.util.concurrent.Callable;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SuppressWarnings("DuplicatedCode")
@Slf4j
public class JpaTest {
    protected static final String username = "Jeremy Keynes";
    static Select0<User, User> userQuery;
    protected static Query query;
    protected static List<User> allUsers;

    @SuppressWarnings("JpaQlInspection")
    @BeforeAll
    public static void init() {
        EntityManager manager = EntityManagers.getEntityManager();
        // QueryBuilder queryBuilder = new JpaQueryBuilder(manager);
        allUsers = Users.getUsers();

        doInTransaction(() -> {
            manager.createQuery("update User set pid = null").executeUpdate();
            manager.createQuery("delete from User").executeUpdate();
            for (User user : allUsers) {
                manager.persist(user);
            }
        });
        CriteriaQuery<User> query = manager.getCriteriaBuilder().createQuery(User.class);
        query.from(User.class);
        allUsers = manager.createQuery(query)
                .getResultList();

        manager.clear();
    }

    public static void doInTransaction(Runnable action) {
        Object o = doInTransaction(() -> {
            action.run();
            return null;
        });
        log.trace("{}", o);
    }

    public static <T> T doInTransaction(Callable<T> action) {
        EntityManager manager = EntityManagers.getEntityManager();

        Session session = manager.unwrap(Session.class);
        Transaction transaction = session.getTransaction();
        T result;
        try {
            transaction.begin();
            result = action.call();
            transaction.commit();
        } catch (Exception e) {
            transaction.rollback();
            throw Lombok.sneakyThrow(e);
        }

        return result;
    }


    static void assertEqualsArrayList(List<Object[]> resultList, List<Object[]> resultList2) {
        assertEquals(resultList.size(), resultList2.size());
        for (int i = 0; i < resultList.size(); i++) {
            assertArrayEquals(resultList.get(i), resultList2.get(i));
        }
    }

    //
    // private SerializableExpression exchange(SerializableExpression expression) {
    //     try {
    //         String s = JsonSerializablePredicateValueTest.mapper.writeValueAsString(expression);
    //         return JsonSerializablePredicateValueTest.mapper.readValue(s, SerializableExpression.class);
    //     } catch (JsonProcessingException e) {
    //         throw new RuntimeException(e);
    //     }
    // }
    //
    // @Test
    // public void testJsonSerializableWhereExpression() {
    //     Predicate<User> predicate = Predicates
    //             .where(User::getRandomNumber).ge(10)
    //             .or(User::getRandomNumber).lt(5)
    //             .not();
    //
    //     check(predicate);
    //
    //     predicate = Predicates
    //             .where(User::getUsername).eq("Jeremy Keynes")
    //             .not();
    //
    //     check(predicate);
    //
    //
    //     predicate = Predicates
    //             .where(User::getUsername).eq("Jeremy Keynes")
    //             .not();
    //     check(predicate);
    //
    //
    //     predicate = Predicates
    //             .where(User::getUsername).eq("Jeremy Keynes");
    //     check(predicate);
    //
    //
    //     predicate = predicate
    //             .and(Predicates.where(User::getId).eq(3))
    //             .not();
    //     check(predicate);
    //
    //
    // }
    //
    // private void check(Predicate<User> predicate) {
    //     List<User> qList = userQuery.where(predicate).getList();
    //     SerializableExpression expression = new SerializableExpression(predicate.expression());
    //     Predicate<User> predicate1 = expression.toPredicate();
    //     List<User> fList = userQuery.where(predicate1).getList();
    //     assertEquals(qList, fList);
    //     fList = userQuery.where(exchange(expression).toPredicate()).getList();
    //     assertEquals(qList, fList);
    // }
    //

    //
    // @Test
    // public void testPredicateAssembler() {
    //
    //     List<User> qList = userQuery.where(User::isValid).eq(true)
    //             .and(User::getParentUser).to(User::getUsername).eq(username)
    //             .getList();
    //     List<User> fList = allUsers.stream()
    //             .filter(user -> user.isValid()
    //                             && user.getParentUser() != null
    //                             && Objects.equals(user.getParentUser().getUsername(), username))
    //             .collect(Collectors.toList());
    //
    //     assertEquals(qList, fList);
    //
    //     ColumnGetter<User, Number> getUsername = User::getRandomNumber;
    //     qList = userQuery.where(User::isValid).eq(true)
    //             .and(getUsername).eq(10)
    //             .getList();
    //     fList = allUsers.stream()
    //             .filter(user -> user.isValid()
    //                             && Objects.equals(user.getRandomNumber(), 10))
    //             .collect(Collectors.toList());
    //
    //     assertEquals(qList, fList);
    //
    //     qList = userQuery.where(User::isValid).eq(true)
    //             .or(getUsername).eq(10)
    //             .getList();
    //     fList = allUsers.stream()
    //             .filter(user -> user.isValid()
    //                             || Objects.equals(user.getRandomNumber(), 10))
    //             .collect(Collectors.toList());
    //
    //     assertEquals(qList, fList);
    //
    //
    //     qList = userQuery.where(User::isValid).eq(true)
    //             .and(getUsername).not().eq(10)
    //             .getList();
    //     fList = allUsers.stream()
    //             .filter(user -> user.isValid()
    //                             && !Objects.equals(user.getRandomNumber(), 10))
    //             .collect(Collectors.toList());
    //
    //     assertEquals(qList, fList);
    //
    //     qList = userQuery.where(User::isValid).eq(true)
    //             .or(getUsername).not().eq(10)
    //             .getList();
    //     fList = allUsers.stream()
    //             .filter(user -> user.isValid()
    //                             || !Objects.equals(user.getRandomNumber(), 10))
    //             .collect(Collectors.toList());
    //
    //     assertEquals(qList, fList);
    //
    //
    //     Date time = allUsers.get(20).getTime();
    //
    //     qList = userQuery.where(User::isValid).eq(true)
    //             .or(User::getParentUser).to(User::getUsername).eq(username)
    //             .and(User::getTime).ge(time)
    //             .getList();
    //
    //     List<User> jeremy_keynes = userQuery.where(User::isValid).eq(true)
    //             .or(User::getParentUser).to(User::getUsername).eq(username)
    //             .fetch(User::getParentUser)
    //             .and(User::getTime).ge(time)
    //             .getList();
    //
    //     fList = allUsers.stream()
    //             .filter(user -> user.isValid()
    //                             || (user.getParentUser() != null
    //                                 && Objects.equals(user.getParentUser().getUsername(), username)
    //                                 && user.getTime().getTime() >= time.getTime()))
    //             .collect(Collectors.toList());
    //
    //     assertEquals(qList, fList);
    //     assertEquals(qList, jeremy_keynes);
    //
    //
    //     qList = userQuery.where(User::isValid).eq(true)
    //             .and(User::getRandomNumber).not().eq(5)
    //             .getList();
    //     fList = allUsers.stream()
    //             .filter(user -> user.isValid()
    //                             && user.getRandomNumber() != 5)
    //             .collect(Collectors.toList());
    //
    //     assertEquals(qList, fList);
    //
    //     qList = userQuery.where(User::isValid).eq(true)
    //             .or(User::getRandomNumber).not().ne(5)
    //             .getList();
    //     fList = allUsers.stream()
    //             .filter(user -> user.isValid()
    //                             || user.getRandomNumber() == 5)
    //             .collect(Collectors.toList());
    //
    //     assertEquals(qList, fList);
    //
    //     qList = userQuery.where(User::getRandomNumber)
    //             .not().eq(6)
    //             .or(User::isValid).not().ne(false)
    //             .getList();
    //     fList = allUsers.stream()
    //             .filter(user -> user.getRandomNumber() != 6
    //                             || !user.isValid())
    //             .collect(Collectors.toList());
    //
    //     assertEquals((qList), (fList));
    //
    //     qList = userQuery.where(User::getRandomNumber).not().eq(6)
    //             .and(User::getParentUser).to(User::isValid).eq(true)
    //             .getList();
    //     fList = allUsers.stream()
    //             .filter(user -> user.getRandomNumber() != 6
    //                             && (user.getParentUser() != null && user.getParentUser().isValid()))
    //             .collect(Collectors.toList());
    //
    //     assertEquals((qList), (fList));
    //
    //     qList = userQuery.where(User::getRandomNumber).not().eq(6)
    //             .and(User::getParentUser).to(User::isValid).not().eq(true)
    //             .getList();
    //     fList = allUsers.stream()
    //             .filter(user -> user.getRandomNumber() != 6
    //                             && (user.getParentUser() != null && !user.getParentUser().isValid()))
    //             .collect(Collectors.toList());
    //
    //     assertEquals((qList), (fList));
    //
    //     qList = userQuery.where(User::getRandomNumber).not().eq(6)
    //             .or(User::getParentUser).to(User::isValid).not().eq(true)
    //             .getList();
    //     fList = allUsers.stream()
    //             .filter(user -> user.getRandomNumber() != 6
    //                             || (user.getParentUser() != null && !user.getParentUser().isValid()))
    //             .collect(Collectors.toList());
    //
    //     assertEquals((qList), (fList));
    //
    //
    //     qList = userQuery.where(Predicates
    //                     .where(User::getRandomNumber).ge(10)
    //                     .or(User::getRandomNumber).lt(5)
    //                     .not()
    //             )
    //             .getList();
    //     fList = allUsers.stream()
    //             .filter(it -> !(it.getRandomNumber() >= 10 || it.getRandomNumber() < 5))
    //             .collect(Collectors.toList());
    //
    //
    //     assertEquals(qList, fList);
    //
    //     qList = userQuery.where(Predicates
    //                     .where(User::getRandomNumber).ge(10)
    //                     .and(User::getRandomNumber).not().gt(15)
    //                     .not()
    //             )
    //             .getList();
    //     fList = allUsers.stream()
    //             .filter(it -> !(it.getRandomNumber() >= 10 && it.getRandomNumber() <= 15))
    //             .collect(Collectors.toList());
    //
    //
    //     assertEquals(qList, fList);
    //
    //     qList = userQuery.where(Predicates
    //                     .where(User::getRandomNumber).ge(10)
    //                     .and(User::getUsername).eq(username)
    //                     .not()
    //             )
    //             .getList();
    //     fList = allUsers.stream()
    //             .filter(it -> !(it.getRandomNumber() >= 10 && it.getUsername().equals(username)))
    //             .collect(Collectors.toList());
    //     assertEquals(qList, fList);
    //
    //
    //     qList = userQuery.where(Predicates
    //                     .where(User::getRandomNumber).ge(10)
    //                     .or(User::getUsername).eq(username)
    //                     .not()
    //             )
    //             .getList();
    //     fList = allUsers.stream()
    //             .filter(it -> !(it.getRandomNumber() >= 10 || it.getUsername().equals(username)))
    //             .collect(Collectors.toList());
    //     assertEquals(qList, fList);
    //
    //
    //     qList = userQuery.where(Predicates
    //                     .where(User::getRandomNumber).ge(10)
    //                     .and(User::getUsername).not().eq(username)
    //                     .not()
    //             )
    //             .getList();
    //     fList = allUsers.stream()
    //             .filter(it -> !(it.getRandomNumber() >= 10 && !it.getUsername().equals(username)))
    //             .collect(Collectors.toList());
    //     assertEquals(qList, fList);
    //
    //     qList = userQuery.where(Predicates
    //                     .where(User::getRandomNumber).ge(10)
    //                     .or(User::getUsername).not().eq(username)
    //                     .not()
    //             )
    //             .getList();
    //     fList = allUsers.stream()
    //             .filter(it -> !(it.getRandomNumber() >= 10 || !it.getUsername().equals(username)))
    //             .collect(Collectors.toList());
    //     assertEquals(qList, fList);
    //
    //
    // }
    //
    // @Test
    // public void testNumberPredicateTester() {
    //     List<User> list = userQuery
    //             .where(User::getRandomNumber).add(2).ge(4)
    //             .getList();
    //     List<User> fList = allUsers.stream()
    //             .filter(user -> user.getRandomNumber() + 2 >= 4)
    //             .collect(Collectors.toList());
    //
    //     assertEquals(list, fList);
    //
    //     list = userQuery
    //             .where(User::getRandomNumber).subtract(2).ge(4)
    //             .getList();
    //     fList = allUsers.stream()
    //             .filter(user -> user.getRandomNumber() - 2 >= 4)
    //             .collect(Collectors.toList());
    //
    //     assertEquals(list, fList);
    //
    //
    //     list = userQuery
    //             .where(User::getRandomNumber).multiply(2).ge(4)
    //             .getList();
    //     fList = allUsers.stream()
    //             .filter(user -> user.getRandomNumber() * 2 >= 4)
    //             .collect(Collectors.toList());
    //
    //     assertEquals(list, fList);
    //
    //
    //     list = userQuery
    //             .where(User::getRandomNumber).divide(2).ge(4)
    //             .getList();
    //     fList = allUsers.stream()
    //             .filter(user -> user.getRandomNumber() / 2 >= 4)
    //             .collect(Collectors.toList());
    //
    //     assertEquals(list, fList);
    //
    //
    //     list = userQuery
    //             .where(User::getRandomNumber).mod(2).ge(1)
    //             .getList();
    //     fList = allUsers.stream()
    //             .filter(user -> user.getRandomNumber() % 2 == 1)
    //             .collect(Collectors.toList());
    //
    //     assertEquals(list, fList);
    //
    //
    //     ///
    //     list = userQuery
    //             .where(User::getRandomNumber).add(User::getId).ge(40)
    //             .getList();
    //     fList = allUsers.stream()
    //             .filter(user -> user.getRandomNumber() + user.getId() >= 40)
    //             .collect(Collectors.toList());
    //
    //     assertEquals(list, fList);
    //
    //     list = userQuery
    //             .where(User::getRandomNumber).subtract(User::getId).ge(40)
    //             .getList();
    //     fList = allUsers.stream()
    //             .filter(user -> user.getRandomNumber() - user.getId() >= 40)
    //             .collect(Collectors.toList());
    //
    //     assertEquals(list, fList);
    //
    //
    //     list = userQuery
    //             .where(User::getRandomNumber).multiply(User::getId).ge(40)
    //             .getList();
    //     fList = allUsers.stream()
    //             .filter(user -> user.getRandomNumber() * user.getId() >= 40)
    //             .collect(Collectors.toList());
    //
    //     assertEquals(list, fList);
    //
    //
    //     list = userQuery
    //             .where(User::getRandomNumber).divide(User::getId).ge(40)
    //             .getList();
    //     fList = allUsers.stream()
    //             .filter(user -> user.getId() != 0 && user.getRandomNumber() / user.getId() >= 40)
    //             .collect(Collectors.toList());
    //
    //     assertEquals(list, fList);
    //
    //
    //     list = userQuery
    //             .where(User::getRandomNumber).mod(User::getId).ge(10)
    //             .getList();
    //     fList = allUsers.stream()
    //             .filter(user -> user.getId() != 0 && user.getRandomNumber() % user.getId() >= 10)
    //             .collect(Collectors.toList());
    //
    //     assertEquals(list, fList);
    //
    // }
    //
    // @Test
    // public void testStringPredicateTester() {
    //     String username = "Roy Sawyer";
    //
    //     List<User> qList = userQuery.where(User::getUsername).substring(2).eq("eremy Keynes")
    //             .getList();
    //     List<User> fList = allUsers.stream()
    //             .filter(user -> user.getUsername().substring(1).equals("eremy Keynes"))
    //             .collect(Collectors.toList());
    //
    //     assertEquals(qList, fList);
    //
    //     qList = userQuery.where(User::getUsername).substring(1, 1).eq("M")
    //             .getList();
    //     fList = allUsers.stream()
    //             .filter(user -> user.getUsername().charAt(0) == 'M')
    //             .collect(Collectors.toList());
    //
    //     assertEquals(qList, fList);
    //
    //     qList = userQuery.where(User::getUsername).trim().like(username)
    //             .getList();
    //     fList = allUsers.stream()
    //             .filter(user -> user.getUsername().trim().startsWith(username))
    //             .collect(Collectors.toList());
    //     assertEquals(qList, fList);
    //
    //     qList = userQuery.where(User::getUsername).length().eq(username.length())
    //             .getList();
    //     fList = allUsers.stream()
    //             .filter(user -> user.getUsername().length() == username.length())
    //             .collect(Collectors.toList());
    //     assertEquals(qList, fList);
    //
    //
    //     qList = userQuery.where(User::getUsername).startWith("M")
    //             .getList();
    //     fList = allUsers.stream()
    //             .filter(user -> user.getUsername().startsWith("M"))
    //             .collect(Collectors.toList());
    //     assertEquals(qList, fList);
    //
    //     qList = userQuery.where(User::getUsername).endsWith("s")
    //             .getList();
    //     fList = allUsers.stream()
    //             .filter(user -> user.getUsername().endsWith("s"))
    //             .collect(Collectors.toList());
    //     assertEquals(qList, fList);
    //
    //     qList = userQuery.where(User::getUsername).lower().contains("s")
    //             .getList();
    //     fList = allUsers.stream()
    //             .filter(user -> user.getUsername().toLowerCase().contains("s"))
    //             .collect(Collectors.toList());
    //     assertEquals(qList, fList);
    //
    //     qList = userQuery.where(User::getUsername).upper().contains("S")
    //             .getList();
    //     fList = allUsers.stream()
    //             .filter(user -> user.getUsername().toUpperCase().contains("S"))
    //             .collect(Collectors.toList());
    //     assertEquals(qList, fList);
    // }
    //
    // @Test
    // public void testOffset() {
    //     userQuery.getList(100);
    // }
    //
    // @Test
    // public void testResultBuilder() {
    //     List<User> resultList = userQuery.getList(5, 10);
    //     List<User> subList = allUsers.subList(5, 5 + 10);
    //     assertEquals(resultList, subList);
    //
    //     resultList = userQuery.getList(20);
    //     subList = allUsers.subList(20, allUsers.size());
    //     assertEquals(resultList, subList);
    //
    //     List<Object[]> userIds = userQuery.select(User::getId)
    //             .getList(5, 10)
    //             .stream().map(Tuple::toArray)
    //             .collect(Collectors.toList());
    //     List<Object[]> subUserIds = allUsers.subList(5, 5 + 10)
    //             .stream().map(it -> new Object[]{it.getId()})
    //             .collect(Collectors.toList());
    //
    //     assertEqualsArrayList(userIds, subUserIds);
    //
    //     resultList = userQuery.where(User::getId).in().getList();
    //     assertEquals(resultList.size(), 0);
    //
    //     int count = userQuery.count();
    //     assertEquals(count, allUsers.size());
    //
    //     User first = userQuery.getFirst();
    //     assertEquals(first, allUsers.get(0));
    //
    //     first = userQuery.where(User::getId).eq(0).requireSingle();
    //     assertEquals(first, allUsers.get(0));
    //
    //     first = userQuery.getFirst(10);
    //     assertEquals(first, allUsers.get(10));
    //
    //     assertThrowsExactly(IllegalStateException.class, () -> userQuery.requireSingle());
    //     assertThrowsExactly(NullPointerException.class, () -> userQuery.where(User::getId).eq(-1).requireSingle());
    //
    //     assertTrue(userQuery.exist());
    //     assertTrue(userQuery.exist(allUsers.size() - 1));
    //     assertFalse(userQuery.exist(allUsers.size()));
    //
    //     List<UserInterface> userInterfaces = userQuery
    //             .projected(UserInterface.class)
    //             .getList();
    //     assertEquals(userInterfaces.get(0), userInterfaces.get(0));
    //
    //     List<UserModel> userModels = userQuery.projected(UserModel.class)
    //             .getList();
    //
    //
    //     List<Map<String, Object>> l0 = allUsers.stream()
    //             .map(UserModel::new)
    //             .map(UserInterface::asMap)
    //             .collect(Collectors.toList());
    //
    //     List<Map<String, Object>> l1 = userInterfaces.stream()
    //             .map(UserInterface::asMap)
    //             .collect(Collectors.toList());
    //     List<Map<String, Object>> l2 = userModels.stream()
    //             .map(UserInterface::asMap)
    //             .collect(Collectors.toList());
    //
    //     assertEquals(l0, l1);
    //     assertEquals(l0, l2);
    //
    // }
    //
    // @Test
    // public void testAttr() {
    //     User first = userQuery.orderBy(User::getId).desc().getFirst();
    //     ArrayList<User> users = new ArrayList<>(allUsers);
    //     users.sort((a, b) -> Integer.compare(b.getId(), a.getId()));
    //     User f = users.stream().findFirst().orElse(null);
    //     assertEquals(first, f);
    //
    //     first = userQuery.orderBy(User::getUsername).desc().getFirst();
    //
    //     users = new ArrayList<>(allUsers);
    //     users.sort((a, b) -> b.getUsername().compareTo(a.getUsername()));
    //     f = users.stream().findFirst().orElse(null);
    //     assertEquals(first, f);
    //
    //     first = userQuery.orderBy(User::isValid).desc().getFirst();
    //     users = new ArrayList<>(allUsers);
    //     users.sort((a, b) -> Boolean.compare(b.isValid(), a.isValid()));
    //     f = users.stream().findFirst().orElse(null);
    //     assertEquals(first, f);
    //
    //     first = userQuery
    //             .where(User::isValid).eq(true)
    //             .getFirst();
    //
    //     f = allUsers.stream()
    //             .filter(User::isValid)
    //             .findFirst()
    //             .orElse(null);
    //     assertEquals(first, f);
    //
    //     List<User> resultList = userQuery
    //             .where(User::getParentUser).to(User::isValid)
    //             .eq(true)
    //             .getList();
    //     List<User> fList = allUsers.stream()
    //             .filter(user -> user.getParentUser() != null && user.getParentUser().isValid())
    //             .collect(Collectors.toList());
    //
    //     assertEquals(resultList, fList);
    // }
    //
    // @Test
    // public void testWhere() {
    //     List<User> resultList = userQuery
    //             .where(User::getParentUser).to(User::getUsername).eq(username)
    //             .getList();
    //     List<User> fList = allUsers.stream()
    //             .filter(user -> user.getParentUser() != null && username.equals(user.getParentUser().getUsername()))
    //             .collect(Collectors.toList());
    //     assertEquals(resultList, fList);
    //
    //     resultList = userQuery
    //             .where(User::getParentUser).to(User::getUsername).not().eq(username)
    //             .getList();
    //     fList = allUsers.stream()
    //             .filter(user -> user.getParentUser() != null && !username.equals(user.getParentUser().getUsername()))
    //             .collect(Collectors.toList());
    //     assertEquals(resultList, fList);
    //
    //
    //     resultList = userQuery
    //             .where(User::getUsername).not().eq(username)
    //             .getList();
    //     fList = allUsers.stream()
    //             .filter(user -> !username.equals(user.getUsername()))
    //             .collect(Collectors.toList());
    //     assertEquals(resultList, fList);
    //
    //
    //     resultList = userQuery
    //             .where(User::getUsername).not().eq(username)
    //             .getList();
    //     fList = allUsers.stream()
    //             .filter(user -> !username.equals(user.getUsername()))
    //             .collect(Collectors.toList());
    //     assertEquals(resultList, fList);
    //
    //
    //     resultList = userQuery
    //             .where(User::getUsername).not().eq(username)
    //             .getList();
    //     fList = allUsers.stream()
    //             .filter(user -> !username.equals(user.getUsername()))
    //             .collect(Collectors.toList());
    //     assertEquals(resultList, fList);
    // }
    //
    // @Test
    // public void testPathBuilder() {
    //     List<User> resultList = userQuery.where(User::getParentUser)
    //             .to(User::getParentUser).to(User::getUsername).eq(username)
    //             .getList();
    //     List<User> fList = allUsers.stream()
    //             .filter(user -> {
    //                 User p = user.getParentUser();
    //                 return p != null && p.getParentUser() != null && username.equals(p.getParentUser().getUsername());
    //             })
    //             .collect(Collectors.toList());
    //     assertEquals(resultList, fList);
    //
    //     resultList = userQuery.where(User::getParentUser)
    //             .to(User::getRandomNumber).eq(5)
    //             .getList();
    //     fList = allUsers.stream()
    //             .filter(user -> {
    //                 User p = user.getParentUser();
    //                 return p != null && p.getRandomNumber() == 5;
    //             })
    //             .collect(Collectors.toList());
    //     assertEquals(resultList, fList);
    //
    //     resultList = userQuery.where(User::getParentUser)
    //             .to(User::getRandomNumber).eq(5)
    //             .getList();
    //     fList = allUsers.stream()
    //             .filter(user -> {
    //                 User p = user.getParentUser();
    //                 return p != null && p.getRandomNumber() == 5;
    //             })
    //             .collect(Collectors.toList());
    //     assertEquals(resultList, fList);
    // }
    //
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


}
