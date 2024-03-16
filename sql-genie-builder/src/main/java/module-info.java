module io.github.genie.sql.builder {
    requires static lombok;
    requires static org.jetbrains.annotations;

    requires io.github.genie.sql.api;
    requires java.desktop;
    requires org.slf4j;

    exports io.github.genie.sql.builder;
    exports io.github.genie.sql.builder.exception;
    exports io.github.genie.sql.builder.meta;
    exports io.github.genie.sql.builder.reflect;
    exports io.github.genie.sql.builder.util;
}