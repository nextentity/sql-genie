package io.github.genie.sql.builder;

import io.github.genie.sql.api.Expression;
import io.github.genie.sql.api.ExpressionHolder;

import java.util.List;

public class TypeCastUtil {


    public static <T> List<T> cast(List<?> expression) {
        return unsafeCast(expression);
    }

    public static <T, U> ExpressionHolder<T, U> cast(Expression expression) {
        return unsafeCast(expression);
    }

    public static <T> T unsafeCast(Object object) {
        // noinspection unchecked
        return (T) object;
    }


}
