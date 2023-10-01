package io.github.genie.sql.core;

import io.github.genie.model.Company;
import io.github.genie.model.User;
import io.github.genie.sql.core.Query.MetadataBuilder;
import io.github.genie.sql.core.Query.Select0;
import io.github.genie.sql.core.Query.SliceMeta;
import io.github.genie.sql.core.executor.JdbcQueryExecutor;
import io.github.genie.sql.core.executor.MySqlSqlBuilder;
import io.github.genie.sql.core.mapping.JpaTableMappingFactory;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.github.genie.sql.core.Q.get;
import static io.github.genie.sql.core.Q.or;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class QueryTest {


    static JpaTableMappingFactory mappings = new JpaTableMappingFactory();
    static MySqlSqlBuilder builder = new MySqlSqlBuilder();
    static Query fromBuilder = Query.createQuery(new QueryExecutor() {
        @Override
        public <R> List<R> getList(@NotNull QueryMetadata queryMetadata) {
            throw new UnsupportedOperationException();
        }
    });

    @Test
    public void test() {


        Select0<User, User> root = fromBuilder.from(User.class);
        SliceMeta metadata = root
                // .select(User::getId)
                .where(get(User::getId).add(2)
                        .eq(10)
                        .and(User::getId).in(1, 2, 3)
                        .or(
                                get(User::getCompany).get(Company::getName).eq("cpn")
                                        .and(User::getId).eq(111)
                                        .and(User::getId).eq(100)
                        )
                )
                .orderBy(get(User::getUsername).asc(), get(User::getId).asc())
                .buildMetadata()
                .slice(9, 10);

        System.out.println(metadata);

        String name = User.class.getName();
        String s0 = "select count(1) from " + User.class.getName() +
                    " where id + 2 = 10 and id in(1, 2, 3) or company.name = cpn and id = 111 and id = 100" +
                    " orderBy username ASC,id ASC";
        assertEquals(s0, metadata.count().toString());
        String s1 = "select " + name + " from " + name +
                    " where id + 2 = 10 and id in(1, 2, 3) or company.name = cpn and id = 111 and id = 100" +
                    " orderBy username ASC,id ASC offset 9 limit 10";
        assertEquals(s1, metadata.list().toString());


        JdbcQueryExecutor.PreparedSql sql = builder.build(metadata.count(), mappings);
        String sql0 = "select count(1) from `user` u left join `company` c0 on u.company_id=c0.id where u.id+2=10 " +
                      "and u.id in(1,2,3) or c0.name=? and u.id=111 and u.id=100 order by u.username asc,u.id asc";
        System.out.println(sql.sql());
        System.out.println(sql.projectionPaths());
        assertEquals(sql0, sql.sql());




        MetadataBuilder metadataBuilder = root
                .select(User::getId, User::getCompanyId)
                .orderBy(Q.get(User::getUsername).asc(), Q.get(User::getId).asc())
                .buildMetadata();
        QueryMetadata qm = metadataBuilder
                .getList(-1, -1, LockModeType.NONE);
        JdbcQueryExecutor.PreparedSql preparedSql = builder.build(qm, mappings);
        System.out.println(preparedSql.sql());
        System.out.println(preparedSql.projectionPaths());

        QueryMetadata count = metadataBuilder.exist(-1);
        System.out.println(count);
        System.out.println(builder.build(count, mappings));

        SliceMeta slice = root.where(get(User::getId).eq(10)
                        .and(get(User::getId).eq(11).or(User::getId).eq(12)))
                .buildMetadata()
                .slice(10, 5);

        String expected = "select count(1) from io.github.genie.model.User where id = 10 and (id = 11 or id = 12)";
        assertEquals(expected, slice.count().toString());


    }

    @Test
    void test2() {
        Select0<User, User> users = fromBuilder.from(User.class);
        MetadataBuilder metadataBuilder = users
                .select(User::getId, User::getCompanyId)
                .orderBy(Q.get(User::getUsername).asc(), Q.get(User::getId).asc())
                .buildMetadata();
        QueryMetadata count = metadataBuilder.exist(-1);
        System.out.println(count);
        System.out.println(builder.build(count, mappings));
    }

    @Test
    void testAndOr() {
        Predicate<User> predicate = Q.and(
                // get(User::getRandomNumber).ne(1),
                // get(User::getRandomNumber).gt(100),
                // get(User::getRandomNumber).ne(125),
                get(User::getRandomNumber).le(666),
                or(
                        // get(User::getRandomNumber).lt(106),
                        get(User::getRandomNumber).gt(120),
                        get(User::getRandomNumber).eq(109)
                ),
                get(User::getRandomNumber).ne(128)
        );

        System.out.println(predicate.meta());
        Select0<User, User> root = fromBuilder.from(User.class);

        QueryMetadata metadata = root.where(predicate)
                .buildMetadata()
                .getList(2, 3, LockModeType.NONE);
        JdbcQueryExecutor.PreparedSql sql = builder.build(metadata, mappings);

        System.out.println(sql.sql());

    }

}