package io.github.genie.sql.test;

import io.github.genie.sql.api.Query.Select;
import io.github.genie.sql.executor.jdbc.MySqlQuerySqlBuilder;
import io.github.genie.sql.executor.jpa.JpaQueryExecutor;
import io.github.genie.sql.meta.JpaMetamodel;
import io.github.genie.sql.test.entity.User;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
public class JpaTest {
    private final GenericApiTest apiTest = getGenericApiTest();

    static GenericApiTest getGenericApiTest() {
        EntityManager manager = EntityManagers.getEntityManager();
        Select<User> userQuery = new JpaQueryExecutor(manager, new JpaMetamodel(), new MySqlQuerySqlBuilder())
                .createQuery(new TestPostProcessor())
                .from(User.class);
        return new GenericApiTest(userQuery) {
        };
    }

    @Test
    void testAndOr() {
        apiTest.testAndOr();
    }

    @Test
    void testAndOrChain() {
        apiTest.testAndOrChain();
    }

    @Test
    void testAndOrChan() {
        apiTest.testAndOrChan();
    }

    @Test
    void testTime() {
        apiTest.testTime();
    }

    @Test
    void testAndOr2() {
        apiTest.testAndOr2();
    }

    @Test
    void testComparablePredicateTesterGt() {
        apiTest.testComparablePredicateTesterGt();
    }

    @Test
    void testPredicateTesterEq() {
        apiTest.testPredicateTesterEq();
    }

    @Test
    void testAggregateFunction() {
        apiTest.testAggregateFunction();
    }

    @Test
    void testSelect() {
        apiTest.testSelect();
    }

    @Test
    void testOrderBy() {
        apiTest.testOrderBy();
    }

    @Test
    void testOrderBy2() {
        apiTest.testOrderBy2();
    }

    @Test
    void testPredicate() {
        apiTest.testPredicate();
    }

    @Test
    void testPredicate2() {
        apiTest.testPredicate2();
    }

    @Test
    void testGroupBy1() {
        apiTest.testGroupBy1();
    }

    @Test
    void testIsNull() {
        apiTest.testIsNull();
    }

    @Test
    void testOperator() {
        apiTest.testOperator();
    }

    @Test
    void testPredicateAssembler() {
        apiTest.testPredicateAssembler();
    }

    @Test
    void testNumberPredicateTester() {
        apiTest.testNumberPredicateTester();
    }

    @Test
    void testStringPredicateTester() {
        apiTest.testStringPredicateTester();
    }

    @Test
    void testOffset() {
        apiTest.testOffset();
    }

    @Test
    void testResultBuilder() {
        apiTest.testResultBuilder();
    }

    @Test
    void testSlice() {
        apiTest.testSlice();
    }

    @Test
    void testAttr() {
        apiTest.testAttr();
    }

    @Test
    void testWhere() {
        apiTest.testWhere();
    }

    @Test
    void testPathBuilder() {
        apiTest.testPathBuilder();
    }

    @Test
    void testBigNum() {
        apiTest.testBigNum();
    }
}
