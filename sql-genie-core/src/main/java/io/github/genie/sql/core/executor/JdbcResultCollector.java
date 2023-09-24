package io.github.genie.sql.core.executor;

import io.github.genie.sql.core.SelectClause;
import io.github.genie.sql.core.exception.BeanReflectiveException;
import io.github.genie.sql.core.mapping.FieldMapping;
import io.github.genie.sql.core.mapping.Mapping;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class JdbcResultCollector implements JdbcQueryExecutor.ResultCollector {

    @Override
    public <R> R collect(@NotNull ResultSet resultSet,
                         @NotNull SelectClause selectClause,
                         @NotNull List<? extends JdbcQueryExecutor.ColumnProjection> projectionPaths) throws SQLException {
        int columnsCount = resultSet.getMetaData().getColumnCount();
        if (projectionPaths.size() != columnsCount) {
            throw new IllegalStateException();
        }
        int column = 0;
        if (selectClause instanceof SelectClause.SingleColumn
            || selectClause instanceof SelectClause.MultiColumn) {
            Object[] row = new Object[columnsCount];
            while (column < columnsCount) {
                row[column++] = resultSet.getObject(column);
            }
            // noinspection unchecked
            return (R) row;
        } else {
            try {

                Object row = selectClause.resultType().getConstructor().newInstance();
                for (JdbcQueryExecutor.ColumnProjection projection : projectionPaths) {
                    int deep = 0;
                    Mapping cur = projection.field();
                    while (cur != null) {
                        deep++;
                        cur = cur.parent();
                    }
                    FieldMapping[] mappings = new FieldMapping[deep - 1];
                    cur = projection.field();
                    for (int i = mappings.length - 1; i >= 0; i--) {
                        mappings[i] = (FieldMapping) cur;
                        cur = cur.parent();
                    }
                    FieldMapping field = projection.field();
                    Class<?> fieldType = field.javaType();
                    Object value = JdbcUtil.getValue(resultSet, ++column, fieldType);
                    if (value == null && mappings.length > 1) {
                        continue;
                    }
                    Object obj = row;
                    for (int i = 0; i < mappings.length - 1; i++) {
                        FieldMapping mapping = mappings[i];
                        Object tmp = mapping.getter().invoke(obj);
                        if (tmp == null) {
                            tmp = mapping.javaType().getConstructor().newInstance();
                            mapping.setter().invoke(obj, tmp);
                        }
                        obj = tmp;
                    }
                    field.setter().invoke(obj, value);
                }
                // noinspection unchecked
                return (R) row;
            } catch (ReflectiveOperationException e) {
                throw new BeanReflectiveException(e);
            }
        }
    }
}
