package io.github.genie.sql.executor.jdbc;

import io.github.genie.sql.api.Column;
import io.github.genie.sql.api.QueryStructure;
import io.github.genie.sql.api.Selection;
import io.github.genie.sql.api.Selection.EntitySelected;
import io.github.genie.sql.api.Selection.MultiSelected;
import io.github.genie.sql.api.Selection.ProjectionSelected;
import io.github.genie.sql.api.Selection.SingleSelected;
import io.github.genie.sql.builder.Tuples;
import io.github.genie.sql.builder.TypeCastUtil;
import io.github.genie.sql.builder.meta.Attribute;
import io.github.genie.sql.builder.meta.EntityType;
import io.github.genie.sql.builder.meta.Type;
import io.github.genie.sql.builder.reflect.InstanceConstructor;
import io.github.genie.sql.builder.reflect.ReflectUtil;
import io.github.genie.sql.executor.jdbc.JdbcQueryExecutor.ResultCollector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class JdbcResultCollector implements ResultCollector {
    @Override
    public <T> List<T> resolve(ResultSet resultSet,
                               EntityType entityType,
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

        if (select instanceof MultiSelected) {
            MultiSelected multiSelected = (MultiSelected) select;
            if (multiSelected.expressions().size() != columnsCount) {
                throw new IllegalStateException();
            }
            List<Class<?>> types = multiSelected.expressions().stream()
                    .map(expression -> {
                        if (expression instanceof Column) {
                            Type t = entityType;
                            Column column = (Column) expression;
                            for (String s : column) {
                                t = ((EntityType) t).getAttribute(s);
                            }
                            return t.javaType();
                        }
                        return Object.class;
                    })
                    .collect(Collectors.toList());
            while (resultSet.next()) {
                Object[] row = getObjects(resultSet, columnsCount, types);
                result.add(TypeCastUtil.unsafeCast(Tuples.of(row)));
            }
        } else if (select instanceof SingleSelected) {
            if (1 != columnsCount) {
                throw new IllegalStateException();
            }
            SingleSelected sc = (SingleSelected) select;
            while (resultSet.next()) {
                T row = getSingleObj(resultSet, sc);
                result.add(row);
            }
        } else {
            if (selected.size() != columnsCount) {
                throw new IllegalStateException();
            }
            Class<?> resultType;
            if (select instanceof EntitySelected) {
                resultType = structure.from().type();
            } else if (select instanceof ProjectionSelected) {
                resultType = select.resultType();
            } else {
                throw new IllegalStateException();
            }
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
    private <R> R getSingleObj(@NotNull ResultSet resultSet, SingleSelected selectClause) throws SQLException {
        Object r = JdbcUtil.getValue(resultSet, 1, selectClause.resultType());
        return TypeCastUtil.unsafeCast(r);
    }

    private Object[] getObjects(@NotNull ResultSet resultSet, int columnsCount, List<Class<?>> types) throws SQLException {
        int column = 0;
        Object[] row = new Object[columnsCount];
        for (Class<?> expression : types) {
            row[column++] = JdbcUtil.getValue(resultSet, column, expression);
        }
        return row;
    }

}
