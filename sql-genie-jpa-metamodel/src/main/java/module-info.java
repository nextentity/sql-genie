module io.github.genie.sql.meta {
    requires static lombok;
    requires static org.slf4j;

    requires io.github.genie.sql.builder;
    requires jakarta.persistence;

    exports io.github.genie.sql.meta;
}