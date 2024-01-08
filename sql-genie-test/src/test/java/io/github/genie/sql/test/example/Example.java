package io.github.genie.sql.test.example;

import io.github.genie.sql.api.Query;
import io.github.genie.sql.api.Query.Select;
import io.github.genie.sql.builder.Q;
import io.github.genie.sql.executor.jdbc.MySqlQuerySqlBuilder;
import io.github.genie.sql.executor.jpa.JpaQueryExecutor;
import io.github.genie.sql.meta.JpaMetamodel;
import io.github.genie.sql.test.DataSourceConfig;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import java.util.HashMap;
import java.util.Map;

public class Example {

    public static void main(String[] args) {
        DataSourceConfig config = new DataSourceConfig();

        Map<String, String> properties = new HashMap<>();
        properties.put("javax.persistence.jdbc.url", config.getUrl());
        properties.put("javax.persistence.jdbc.user", config.getUser());
        properties.put("javax.persistence.jdbc.password", config.getPassword());
        try (EntityManagerFactory factory = Persistence.createEntityManagerFactory("org.hibernate.jpa", properties)) {
            EntityManager em = factory.createEntityManager();
            Query builder = new JpaQueryExecutor(em, new JpaMetamodel(), new MySqlQuerySqlBuilder()).createQuery();
            Select<Employee> select0 = builder.from(Employee.class);
            runExample(select0);
        }
    }

    private static void runExample(Select<Employee> query) {

        // select * from employee where id = 1
        query.where(Employee::getId).eq(1).getSingle();

        // select employee.*, company.* from
        // employee left join company
        // on employee.company_id = company.id
        // where employee.id = 1
        query.fetch(Employee::getCompany).where(Employee::getId).eq(1).getSingle();

        // select * from employee where name = 'Luna' and age > 10
        query.where(Employee::getName).eq("Luna").and(Employee::getAge).gt(10).getList();

        // select * from employee where name = 'Luna' and age > 10 order by id desc limit 0,100
        query.where(Employee::getName).eq("Luna")
                .and(Employee::getAge).gt(10)
                .orderBy(Q.get(Employee::getId).desc())
                .getList(0, 100);

        // select employee.* from
        // employee left join company
        // on employee.company_id = company.id
        // where company.name = 'Microsoft'
        query.where(Employee::getCompany).get(Company::getName).eq("Microsoft").getList();

        // page
        Page<Employee> page = query
                .where(Employee::getCompany).get(Company::getName).eq("Microsoft")
                .slice(new Pageable<>(1, 10));

    }

}
