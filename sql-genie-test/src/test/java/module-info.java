module sql.genie.test {
    requires static lombok;
    requires static org.slf4j;
    requires io.github.genie.sql.builder;
    requires jakarta.persistence;
    requires mysql.connector.j;
    requires org.jetbrains.annotations;
    requires com.fasterxml.jackson.databind;
    requires io.github.genie.sql.api;
    requires io.github.genie.sql.executor.jdbc;
    requires io.github.genie.sql.meta;
    requires org.junit.jupiter.params;
    requires org.hibernate.orm.core;
    requires io.github.genie.sql.executor.jpa;
    requires java.naming;

    opens io.github.genie.sql.test;
    opens io.github.genie.sql.test.entity;
    opens io.github.genie.sql.test.example;
    opens io.github.genie.sql.test.builder;
    opens io.github.genie.sql.test.projection;

}