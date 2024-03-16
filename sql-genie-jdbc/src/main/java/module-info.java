module io.github.genie.sql.executor.jdbc {
    requires static lombok;
    requires static org.jetbrains.annotations;

    requires java.sql;
    requires io.github.genie.sql.api;
    requires io.github.genie.sql.builder;
    requires org.slf4j;

    exports io.github.genie.sql.executor.jdbc;
}