package io.github.genie.sql.core.executor.jdbc;

import io.github.genie.sql.core.SelectClause;
import io.github.genie.sql.core.exception.BeanReflectiveException;
import io.github.genie.sql.core.executor.ProjectionUtil;
import io.github.genie.sql.core.mapping.FieldMapping;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

import static io.github.genie.sql.core.SelectClause.MultiColumn;
import static io.github.genie.sql.core.SelectClause.SingleColumn;
import static io.github.genie.sql.core.executor.ProjectionUtil.newProxyInstance;

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
                Class<?> resultType = selectClause.resultType();
                if (resultType.isInterface()) {
                    return getInterfaceResult(resultSet, fields, resultType);
                } else if (resultType.isRecord()) {
                    return getRecordResult(resultSet, fields, resultType);
                } else {
                    return getBeanResult(resultSet, fields, resultType);
                }
            } catch (ReflectiveOperationException e) {
                throw new BeanReflectiveException(e);
            }
        }
    }

    @NotNull
    private <R> R getRecordResult(@NotNull ResultSet resultSet,
                                  @NotNull List<? extends FieldMapping> fields,
                                  Class<?> resultType)
            throws SQLException, ReflectiveOperationException {
        Map<String, Object> map = new HashMap<>();
        int i = 0;
        for (FieldMapping attribute : fields) {
            Object value = JdbcUtil.getValue(resultSet, ++i, attribute.javaType());
            map.put(attribute.fieldName(), value);
        }
        return ProjectionUtil.getRecordResult(resultType, map);
    }

    private <R> R getInterfaceResult(ResultSet resultSet, List<? extends FieldMapping> fields, Class<?> resultType) throws SQLException {
        Map<Method, Object> map = new HashMap<>();
        int i = 0;
        for (FieldMapping attribute : fields) {
            Object value = JdbcUtil.getValue(resultSet, ++i, attribute.javaType());
            map.put(attribute.getter(), value);
        }

        Object result = newProxyInstance(fields, resultType, map);
        return cast(result);
    }

    @NotNull
    private <R> R getBeanResult(@NotNull ResultSet resultSet,
                                @NotNull List<? extends FieldMapping> fields,
                                Class<?> resultType)
            throws ReflectiveOperationException {
        // noinspection Convert2Lambda
        return ProjectionUtil.getBeanResult(new BiFunction<>() {
            @SneakyThrows
            @Override
            public Object apply(Integer index, Class<?> resultType) {
                return JdbcUtil.getValue(resultSet, 1 + index, resultType);
            }
        }, fields, resultType);
    }

    private <R> R cast(Object result) {
        // noinspection unchecked
        return (R) result;
    }


}
