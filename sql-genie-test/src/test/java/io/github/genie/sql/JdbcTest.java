package io.github.genie.sql;

import com.mysql.cj.jdbc.MysqlDataSource;
import io.github.genie.sql.api.Query;
import io.github.genie.sql.api.Query.Select;
import io.github.genie.sql.entity.User;
import io.github.genie.sql.executor.jdbc.ConnectionProvider;
import io.github.genie.sql.executor.jdbc.JdbcQueryExecutor;
import io.github.genie.sql.executor.jdbc.MySqlQuerySqlBuilder;
import io.github.genie.sql.executor.jdbc.JdbcResultCollector;
import io.github.genie.sql.meta.JpaMetamodel;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;

@Slf4j
public class JdbcTest {

    private static GenericApiTest apiTest;

    @BeforeAll
    static void init() throws SQLException {
        DataSourceConfig config = new DataSourceConfig();
        MysqlDataSource source = config.getMysqlDataSource();
        Connection connection = source.getConnection();
        ConnectionProvider sqlExecutor = new ConnectionProvider() {
            @Override
            public <T> T execute(ConnectionCallback<T> action) throws SQLException {
                return action.doInConnection(connection);
            }
        };
        Query query = new JdbcQueryExecutor(new JpaMetamodel(),
                new MySqlQuerySqlBuilder(),
                sqlExecutor,
                new JdbcResultCollector()
        ).createQuery(new TestPostProcessor());

        Select<User> userQuery = query.from(User.class);

        apiTest = new GenericApiTest(userQuery) {
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
