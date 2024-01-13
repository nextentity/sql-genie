package io.github.genie.sql.builder;

import io.github.genie.sql.api.Expression;
import io.github.genie.sql.api.ExpressionHolder;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public interface ExpressionHolders {

    static <T, U> ExpressionHolder<T, U> of(Expression expression) {
        return TypeCastUtil.cast(expression);
    }

    static <T, U> ExpressionHolder<T, U> of(U value) {
        return of(Expressions.of(value));
    }

    static <T, U> List<ExpressionHolder<T, U>> of(U[] value) {
        return Arrays.stream(value)
                .map(ExpressionHolders::<T, U>of)
                .collect(Collectors.toList());
    }

    static <T, U> List<ExpressionHolder<T, U>> of(Iterable<? extends U> value) {
        return StreamSupport.stream(value.spliterator(), false)
                .map(ExpressionHolders::<T, U>of)
                .collect(Collectors.toList());
    }

    static <T, U> ExpressionHolder<T, U> ofTrue() {
        return of(Expressions.TRUE);
    }

}
