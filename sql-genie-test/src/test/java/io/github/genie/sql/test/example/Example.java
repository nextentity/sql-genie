package io.github.genie.sql.test.example;

import io.github.genie.sql.api.Query;
import io.github.genie.sql.api.Query.Select0;
import io.github.genie.sql.builder.Q;
import io.github.genie.sql.core.mapping.JpaMetamodel;
import io.github.genie.sql.executor.jdbc.MySqlQuerySqlBuilder;
import io.github.genie.sql.executor.jpa.JpaQueryExecutor;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class Example {

    public static void main(String[] args) {

        try (EntityManagerFactory factory = Persistence.createEntityManagerFactory("org.hibernate.jpa")) {
            EntityManager em = factory.createEntityManager();
            Query builder = new JpaQueryExecutor(em, new JpaMetamodel(), new MySqlQuerySqlBuilder()).createQuery();
            Select0<Employee, Employee> select0 = builder.from(Employee.class);
            runExample(select0);
        }
    }

    private static void runExample(Select0<Employee, Employee> query) {

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
