package io.github.genie.sql.core.executor.jdbc;

import io.github.genie.sql.core.mapping.ColumnMapping;
import io.github.genie.sql.core.mapping.FieldMapping;
import io.github.genie.sql.core.mapping.TableMapping;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

public interface JdbcUpdateSqlBuilder {

    PreparedSql buildInsert(@NotNull TableMapping mapping);

    default PreparedSql buildUpdate(@NotNull TableMapping tableMapping) {
        FieldMapping id = tableMapping.id();
        List<ColumnMapping> columns = tableMapping.fields().stream()
                .filter(it -> it instanceof ColumnMapping && it != id)
                .map(it -> (ColumnMapping) it)
                .collect(Collectors.toList());
        return buildUpdate(tableMapping, columns);
    }

    PreparedSql buildUpdate(@NotNull TableMapping tableMapping, @NotNull List<ColumnMapping> columns);

    interface PreparedSql {
        String sql();

        List<ColumnMapping> columns();

        List<ColumnMapping> versionColumns();

    }

}
