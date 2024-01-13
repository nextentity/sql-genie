package io.github.genie.sql.api;

@SuppressWarnings("unused")
public interface ExpressionHolder<T, U> {

    Expression expression();

    interface ColumnHolder<T, U> extends ExpressionHolder<T, U> {

    }

}
