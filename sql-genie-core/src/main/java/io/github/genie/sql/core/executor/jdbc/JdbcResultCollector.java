package io.github.genie.sql.core.executor.jdbc;

import io.github.genie.sql.core.SelectClause;
import io.github.genie.sql.core.executor.ProjectionUtil;
import io.github.genie.sql.core.mapping.FieldMapping;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.function.BiFunction;

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
            Class<?> resultType = selectClause.resultType();
            if (resultType.isInterface()) {
                return ProjectionUtil.getInterfaceResult(getJdbcResultFunction(resultSet), fields, resultType);
            } else if (resultType.isRecord()) {
                return ProjectionUtil.getRecordResult(getJdbcResultFunction(resultSet), fields, resultType);
            } else {
                return ProjectionUtil.getBeanResult(getJdbcResultFunction(resultSet), fields, resultType);
            }
        }
    }

    @NotNull
    private static BiFunction<Integer, Class<?>, Object> getJdbcResultFunction(@NotNull ResultSet resultSet) {
        // noinspection Convert2Lambda
        return new BiFunction<>() {
            @SneakyThrows
            @Override
            public Object apply(Integer index, Class<?> resultType) {
                return JdbcUtil.getValue(resultSet, 1 + index, resultType);
            }
        };
    }

    private <R> R cast(Object result) {
        // noinspection unchecked
        return (R) result;
    }


}
