package io.github.genie.sql.executor.jdbc;

import io.github.genie.sql.builder.meta.Attribute;
import io.github.genie.sql.builder.meta.BasicAttribute;
import io.github.genie.sql.builder.meta.EntityType;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

public interface JdbcUpdateSqlBuilder {

    PreparedSql buildInsert(@NotNull EntityType mapping);

    default PreparedSql buildUpdate(@NotNull EntityType entityType) {
        Attribute id = entityType.id();
        List<BasicAttribute> columns = entityType.fields().stream()
                .filter(it -> it instanceof BasicAttribute && it != id)
                .map(it -> (BasicAttribute) it)
                .collect(Collectors.toList());
        return buildUpdate(entityType, columns);
    }

    PreparedSql buildUpdate(@NotNull EntityType entityType, @NotNull List<BasicAttribute> columns);

    PreparedSql buildDelete(EntityType entity);

    interface PreparedSql {
        String sql();

        List<BasicAttribute> columns();

        List<BasicAttribute> versionColumns();

    }

}
