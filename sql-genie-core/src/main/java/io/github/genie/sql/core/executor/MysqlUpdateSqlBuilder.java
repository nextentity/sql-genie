package io.github.genie.sql.core.executor;

import io.github.genie.sql.core.mapping.ColumnMapping;
import io.github.genie.sql.core.mapping.FieldMapping;
import io.github.genie.sql.core.mapping.TableMapping;
import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class MysqlUpdateSqlBuilder implements JdbcUpdateSqlBuilder {

    @Override
    public PreparedSql buildInsert(@NotNull TableMapping mapping) {
        String tableName = mapping.tableName();
        List<ColumnMapping> columns = new ArrayList<>();
        StringBuilder sql = new StringBuilder("insert into `")
                .append(tableName).append("` (");
        String delimiter = "";
        for (FieldMapping field : mapping.fields()) {
            if (!(field instanceof ColumnMapping column)) {
                continue;
            }
            sql.append(delimiter).append("`").append(column.columnName()).append("`");
            columns.add(column);
            delimiter = ",";
        }

        sql.append(") values (");
        delimiter = "";
        int size = columns.size();
        for (int i = 0; i < size; i++) {
            sql.append(delimiter).append("?");
            delimiter = ",";
        }
        sql.append(")");
        return new PreparedSqlImpl(sql.toString(), columns, null);
    }

    @Override
    public PreparedSql buildUpdate(@NotNull TableMapping tableMapping, @NotNull List<ColumnMapping> columns) {
        StringBuilder sql = new StringBuilder("update `").append(tableMapping.tableName()).append("` set ");
        ColumnMapping id = (ColumnMapping) tableMapping.id();
        String delimiter = "";
        List<ColumnMapping> cms = new ArrayList<>(columns.size() + 1);
        List<ColumnMapping> versions = null;
        for (ColumnMapping column : columns) {
            sql.append(delimiter).append("`").append(column.columnName());
            if (column.versionColumn()) {
                sql.append("`=`").append(column.columnName()).append("`+1");
                versions = versions == null ? new ArrayList<>(1) : versions;
                versions.add(column);
            } else {
                sql.append("`=?");
                cms.add(column);
            }
            delimiter = ",";
        }
        sql.append(" where `").append(id.columnName()).append("`=?");
        cms.add(id);
        if (versions != null) {
            for (ColumnMapping version : versions) {
                sql.append(" and `").append(version.columnName()).append("`=?");
                cms.add(version);
            }
        }
        return new PreparedSqlImpl(sql.toString(), cms, versions);
    }

    @AllArgsConstructor
    private static class PreparedSqlImpl implements PreparedSql {
        private String sql;
        private List<ColumnMapping> columns;
        private List<ColumnMapping> versionColumns;

        @Override
        public String sql() {
            return this.sql;
        }

        @Override
        public List<ColumnMapping> columns() {
            return this.columns;
        }

        @Override
        public List<ColumnMapping> versionColumns() {
            return versionColumns;
        }
    }
}
