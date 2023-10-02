package io.github.genie.sql.core.executor;

import io.github.genie.sql.core.SelectClause;
import io.github.genie.sql.core.exception.BeanReflectiveException;
import io.github.genie.sql.core.mapping.FieldMapping;
import io.github.genie.sql.core.mapping.Mapping;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static io.github.genie.sql.core.SelectClause.MultiColumn;
import static io.github.genie.sql.core.SelectClause.SingleColumn;

public class JdbcResultCollector implements JdbcQueryExecutor.ResultCollector {

    @Override
    public <R> R collect(@NotNull ResultSet resultSet,
                         @NotNull SelectClause selectClause,
                         @NotNull Class<?> fromType,
                         @NotNull List<? extends FieldMapping> fields)
            throws SQLException {
        int columnsCount = resultSet.getMetaData().getColumnCount();
        int column = 0;
        if (selectClause instanceof MultiColumn multiColumn) {
            if (multiColumn.columns().size() != columnsCount) {
                throw new IllegalStateException();
            }
            Object[] row = new Object[columnsCount];
            while (column < columnsCount) {
                row[column++] = resultSet.getObject(column);
            }
            return cast(row);
        } else if (selectClause instanceof SingleColumn singleColumn) {
            if (1 != columnsCount) {
                throw new IllegalStateException();
            }
            Object r = JdbcUtil.getValue(resultSet, 1, singleColumn.resultType());
            return cast(r);
        } else {
            if (fields.size() != columnsCount) {
                throw new IllegalStateException();
            }
            try {
                Object row = selectClause.resultType().getConstructor().newInstance();
                for (FieldMapping projection : fields) {
                    int deep = 0;
                    Mapping cur = projection;
                    while (cur != null) {
                        deep++;
                        cur = cur.parent();
                    }
                    FieldMapping[] mappings = new FieldMapping[deep - 1];
                    cur = projection;
                    for (int i = mappings.length - 1; i >= 0; i--) {
                        mappings[i] = (FieldMapping) cur;
                        cur = cur.parent();
                    }
                    Class<?> fieldType = projection.javaType();
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
                    projection.setter().invoke(obj, value);
                }
                return cast(row);
            } catch (ReflectiveOperationException e) {
                throw new BeanReflectiveException(e);
            }
        }
    }

    private <R> R cast(Object result) {
        // noinspection unchecked
        return (R) result;
    }


}
