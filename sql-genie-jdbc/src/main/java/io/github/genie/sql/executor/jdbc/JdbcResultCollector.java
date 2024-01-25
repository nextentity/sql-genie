package io.github.genie.sql.executor.jdbc;

import io.github.genie.sql.api.QueryStructure;
import io.github.genie.sql.api.Selection;
import io.github.genie.sql.api.Selection.MultiColumn;
import io.github.genie.sql.api.Selection.SingleColumn;
import io.github.genie.sql.builder.TypeCastUtil;
import io.github.genie.sql.builder.meta.Attribute;
import io.github.genie.sql.builder.reflect.InstanceConstructor;
import io.github.genie.sql.builder.reflect.ReflectUtil;
import io.github.genie.sql.executor.jdbc.JdbcQueryExecutor.ResultCollector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class JdbcResultCollector implements ResultCollector {
    @Override
    public <T> List<T> resolve(ResultSet resultSet,
                               List<? extends Attribute> selected,
                               QueryStructure structure) throws SQLException {
        int type = resultSet.getType();
        List<T> result;
        if (type != ResultSet.TYPE_FORWARD_ONLY) {
            resultSet.last();
            int size = resultSet.getRow();
            result = new ArrayList<>(size);
            resultSet.beforeFirst();
        } else {
            result = new ArrayList<>();
        }
        Selection select = structure.select();
        int columnsCount = resultSet.getMetaData().getColumnCount();

        if (select instanceof MultiColumn multiColumn) {
            if (multiColumn.columns().size() != columnsCount) {
                throw new IllegalStateException();
            }
            while (resultSet.next()) {
                Object[] row = getObjects(resultSet, columnsCount);
                result.add(TypeCastUtil.unsafeCast(row));
            }
        } else if (select instanceof SingleColumn) {
            if (1 != columnsCount) {
                throw new IllegalStateException();
            }
            while (resultSet.next()) {
                T row = getSingleObj(resultSet, select);
                result.add(row);
            }
        } else {
            if (selected.size() != columnsCount) {
                throw new IllegalStateException();
            }
            Class<?> resultType = select.resultType();
            InstanceConstructor extractor = ReflectUtil.getRowInstanceConstructor(selected, resultType);
            Object[] data = new Object[columnsCount];
            while (resultSet.next()) {
                int i = 0;
                for (Attribute attribute : selected) {
                    data[i++] = JdbcUtil.getValue(resultSet, i, attribute.javaType());
                }
                T row = TypeCastUtil.unsafeCast(extractor.newInstance(data));
                result.add(row);
            }
        }
        return result;
    }

    @Nullable
    private <R> R getSingleObj(@NotNull ResultSet resultSet, Selection selectClause) throws SQLException {
        Object r = JdbcUtil.getValue(resultSet, 1, selectClause.resultType());
        return TypeCastUtil.unsafeCast(r);
    }

    private Object[] getObjects(@NotNull ResultSet resultSet, int columnsCount) throws SQLException {
        int column = 0;
        Object[] row = new Object[columnsCount];
        while (column < columnsCount) {
            row[column++] = resultSet.getObject(column);
        }
        return row;
    }

}
