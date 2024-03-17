module io.github.genie.sql.executor.jpa {
    requires static org.jetbrains.annotations;

    requires io.github.genie.sql.api;
    requires io.github.genie.sql.builder;
    requires jakarta.persistence;
    requires io.github.genie.sql.executor.jdbc;
    requires static lombok;
    requires static org.slf4j;

    exports io.github.genie.sql.executor.jpa;
}