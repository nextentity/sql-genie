package io.github.genie.sql.api;

import io.github.genie.sql.api.Path.BooleanPath;
import io.github.genie.sql.api.Path.ComparablePath;
import io.github.genie.sql.api.Path.NumberPath;
import io.github.genie.sql.api.Path.StringPath;
import io.github.genie.sql.api.TypedExpression.Predicate;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

public interface ExpressionOperator<T, U, B> {

    B eq(U value);

    B eq(ExpressionHolder<T, U> expression);

    B ne(U value);

    B ne(ExpressionHolder<T, U> expression);

    @SuppressWarnings({"unchecked"})
    B in(U... values);

    B in(@NotNull List<? extends ExpressionHolder<T, U>> expressions);

    B in(@NotNull Collection<? extends U> values);

    @SuppressWarnings({"unchecked"})
    B notIn(U... values);

    B notIn(@NotNull List<? extends ExpressionHolder<T, U>> expressions);

    B notIn(@NotNull Collection<? extends U> values);

    B isNull();

    NumberOperator<T, Integer, B> count();

    B isNotNull();

    interface BooleanOperator<T, B> extends ComparableOperator<T, Boolean, B> {
        Predicate<T> then();
    }

    interface ComparableOperator<T, U extends Comparable<U>, B> extends ExpressionOperator<T, U, B> {

        B ge(U value);

        B gt(U value);

        B le(U value);

        B lt(U value);

        B between(U l, U r);

        B notBetween(U l, U r);

        B ge(ExpressionHolder<T, U> expression);

        B gt(ExpressionHolder<T, U> expression);

        B le(ExpressionHolder<T, U> expression);

        B lt(ExpressionHolder<T, U> expression);

        B between(ExpressionHolder<T, U> l, ExpressionHolder<T, U> r);

        B between(ExpressionHolder<T, U> l, U r);

        B between(U l, ExpressionHolder<T, U> r);

        B notBetween(ExpressionHolder<T, U> l, ExpressionHolder<T, U> r);

        B notBetween(ExpressionHolder<T, U> l, U r);

        B notBetween(U l, ExpressionHolder<T, U> r);

    }

    interface NumberOperator<T, U extends Number & Comparable<U>, B> extends ComparableOperator<T, U, B> {
        NumberOperator<T, U, B> add(U value);

        NumberOperator<T, U, B> subtract(U value);

        NumberOperator<T, U, B> multiply(U value);

        NumberOperator<T, U, B> divide(U value);

        NumberOperator<T, U, B> mod(U value);

        NumberOperator<T, U, B> add(ExpressionHolder<T, U> expression);

        NumberOperator<T, U, B> subtract(ExpressionHolder<T, U> expression);

        NumberOperator<T, U, B> multiply(ExpressionHolder<T, U> expression);

        NumberOperator<T, U, B> divide(ExpressionHolder<T, U> expression);

        NumberOperator<T, U, B> mod(ExpressionHolder<T, U> expression);

        NumberOperator<T, U, B> sum();

        <V extends Number & Comparable<V>> NumberOperator<T, V, B> avg();

        NumberOperator<T, U, B> max();

        NumberOperator<T, U, B> min();

    }

    interface PathOperator<T, U, B> extends ExpressionOperator<T, U, B> {

        <V> PathOperator<T, V, B> get(Path<U, V> path);

        StringOperator<T, B> get(StringPath<U> path);

        <V extends Number & Comparable<V>> NumberOperator<T, V, B> get(NumberPath<U, V> path);

        <V extends Comparable<V>> ComparableOperator<T, V, B> get(ComparablePath<U, V> path);

        default ComparableOperator<T, Boolean, B> get(BooleanPath<U> path) {
            return get((ComparablePath<U, Boolean>) path);
        }

    }

    interface StringOperator<T, B> extends ComparableOperator<T, String, B> {

        B like(String value);

        default B startWith(String value) {
            return like(value + '%');
        }

        default B endsWith(String value) {
            return like('%' + value);
        }

        default B contains(String value) {
            return like('%' + value + '%');
        }

        B notLike(String value);

        default B notStartWith(String value) {
            return notLike(value + '%');
        }

        default B notEndsWith(String value) {
            return notLike('%' + value);
        }

        default B notContains(String value) {
            return notLike('%' + value + '%');
        }

        StringOperator<T, B> lower();

        StringOperator<T, B> upper();

        StringOperator<T, B> substring(int a, int b);

        StringOperator<T, B> substring(int a);

        StringOperator<T, B> trim();

        NumberOperator<T, Integer, B> length();

    }
}
