package io.github.genie.sql.executor.jdbc;

import io.github.genie.sql.api.Selection;
import io.github.genie.sql.builder.executor.ProjectionUtil;
import io.github.genie.sql.builder.meta.Attribute;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.function.BiFunction;

import static io.github.genie.sql.api.Selection.MultiColumn;
import static io.github.genie.sql.api.Selection.SingleColumn;

public class JdbcResultCollector implements JdbcQueryExecutor.ResultCollector {

    @Override
    public <R> R collect(@NotNull ResultSet resultSet,
                         @NotNull Selection selectClause,
                         @NotNull Class<?> fromType,
                         @NotNull List<? extends Attribute> attributes)
            throws SQLException {
        int columnsCount = resultSet.getMetaData().getColumnCount();
        int column = 0;
        if (selectClause instanceof MultiColumn) {
            MultiColumn multiColumn = (MultiColumn) selectClause;
            if (multiColumn.columns().size() != columnsCount) {
                throw new IllegalStateException();
            }
            Object[] row = new Object[columnsCount];
            while (column < columnsCount) {
                row[column++] = resultSet.getObject(column);
            }
            return cast(row);
        } else if (selectClause instanceof SingleColumn) {
            SingleColumn singleColumn = (SingleColumn) selectClause;
            if (1 != columnsCount) {
                throw new IllegalStateException();
            }
            Object r = JdbcUtil.getValue(resultSet, 1, singleColumn.resultType());
            return cast(r);
        } else {
            if (attributes.size() != columnsCount) {
                throw new IllegalStateException();
            }
            Class<?> resultType = selectClause.resultType();
            if (resultType.isInterface()) {
                return ProjectionUtil.getInterfaceResult(getJdbcResultFunction(resultSet), attributes, resultType);
            } else {
                return ProjectionUtil.getBeanResult(getJdbcResultFunction(resultSet), attributes, resultType);
            }
        }
    }

    @NotNull
    private static BiFunction<Integer, Class<?>, Object> getJdbcResultFunction(@NotNull ResultSet resultSet) {
        // noinspection Convert2Lambda
        return new BiFunction<Integer, Class<?>, Object>() {
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
