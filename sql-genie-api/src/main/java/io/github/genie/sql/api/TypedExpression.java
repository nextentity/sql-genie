package io.github.genie.sql.api;

import io.github.genie.sql.api.ExpressionOperator.ComparableOperator;
import io.github.genie.sql.api.ExpressionOperator.NumberOperator;
import io.github.genie.sql.api.ExpressionOperator.PathOperator;
import io.github.genie.sql.api.ExpressionOperator.StringOperator;
import io.github.genie.sql.api.Path.BooleanPath;
import io.github.genie.sql.api.Path.ComparablePath;
import io.github.genie.sql.api.Path.NumberPath;
import io.github.genie.sql.api.Path.StringPath;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

public interface TypedExpression<T, U> extends ExpressionHolder<T, U> {

    EntityRoot<T> root();

    NumberExpression<T, Long> count();

    BooleanExpression<T> eq(U value);

    BooleanExpression<T> eq(ExpressionHolder<T, U> value);

    BooleanExpression<T> ne(U value);

    BooleanExpression<T> ne(ExpressionHolder<T, U> value);

    @SuppressWarnings("unchecked")
    BooleanExpression<T> in(U... values);

    BooleanExpression<T> in(@NotNull List<? extends ExpressionHolder<T, U>> values);

    BooleanExpression<T> in(@NotNull Collection<? extends U> values);

    @SuppressWarnings("unchecked")
    BooleanExpression<T> notIn(U... values);

    BooleanExpression<T> notIn(@NotNull List<? extends ExpressionHolder<T, U>> values);

    BooleanExpression<T> notIn(@NotNull Collection<? extends U> values);

    BooleanExpression<T> isNull();

    BooleanExpression<T> isNotNull();

    interface AndOperator<T> extends ComparableExpression<T, Boolean> {

        <R> PathOperator<T, R, AndOperator<T>> and(Path<T, R> path);

        <R extends Comparable<R>> ComparableOperator<T, R, AndOperator<T>> and(ComparablePath<T, R> path);

        <R extends Number & Comparable<R>> NumberOperator<T, R, AndOperator<T>> and(NumberPath<T, R> path);

        AndOperator<T> and(BooleanPath<T> path);

        StringOperator<T, AndOperator<T>> and(StringPath<T> path);

        AndOperator<T> and(ExpressionHolder<T, Boolean> expression);

        AndOperator<T> and(List<? extends ExpressionHolder<T, Boolean>> expressions);

        Predicate<T> then();

    }

    interface OrOperator<T> extends ComparableExpression<T, Boolean> {

        <N> PathOperator<T, N, OrOperator<T>> or(Path<T, N> path);

        <N extends Number & Comparable<N>> NumberOperator<T, N, OrOperator<T>> or(NumberPath<T, N> path);

        <N extends Comparable<N>> ComparableOperator<T, N, OrOperator<T>> or(ComparablePath<T, N> path);

        StringOperator<T, ? extends OrOperator<T>> or(StringPath<T> path);

        OrOperator<T> or(BooleanPath<T> path);

        OrOperator<T> or(ExpressionHolder<T, Boolean> predicate);

        OrOperator<T> or(List<? extends ExpressionHolder<T, Boolean>> expressions);

        Predicate<T> then();

    }

    interface BooleanExpression<T> extends AndOperator<T>, OrOperator<T>, Predicate<T> {
    }

    interface ComparableExpression<T, U extends Comparable<U>> extends TypedExpression<T, U> {
        BooleanExpression<T> ge(ExpressionHolder<T, U> expression);

        BooleanExpression<T> gt(ExpressionHolder<T, U> expression);

        BooleanExpression<T> le(ExpressionHolder<T, U> expression);

        BooleanExpression<T> lt(ExpressionHolder<T, U> expression);

        BooleanExpression<T> between(ExpressionHolder<T, U> l, ExpressionHolder<T, U> r);

        BooleanExpression<T> notBetween(ExpressionHolder<T, U> l, ExpressionHolder<T, U> r);

        Order<T> asc();

        Order<T> desc();

        default BooleanExpression<T> ge(U value) {
            return ge(root().of(value));
        }

        default BooleanExpression<T> gt(U value) {
            return gt(root().of(value));
        }

        default BooleanExpression<T> le(U value) {
            return le(root().of(value));
        }

        default BooleanExpression<T> lt(U value) {
            return lt(root().of(value));
        }

        default BooleanExpression<T> between(U l, U r) {
            EntityRoot<T> eb = root();
            return between(eb.of(l), eb.of(r));
        }

        default BooleanExpression<T> notBetween(U l, U r) {
            EntityRoot<T> eb = root();
            return notBetween(eb.of(l), eb.of(r));
        }

        default BooleanExpression<T> between(ExpressionHolder<T, U> l, U r) {
            return between(l, root().of(r));
        }

        default BooleanExpression<T> between(U l, ExpressionHolder<T, U> r) {
            return between(root().of(l), r);
        }


        default BooleanExpression<T> notBetween(ExpressionHolder<T, U> l, U r) {
            return notBetween(l, root().of(r));
        }

        default BooleanExpression<T> notBetween(U l, ExpressionHolder<T, U> r) {
            return notBetween(root().of(l), r);
        }


    }

    interface NumberExpression<T, U extends Number & Comparable<U>> extends ComparableExpression<T, U> {
        NumberExpression<T, U> add(ExpressionHolder<T, U> expression);

        NumberExpression<T, U> subtract(ExpressionHolder<T, U> expression);

        NumberExpression<T, U> multiply(ExpressionHolder<T, U> expression);

        NumberExpression<T, U> divide(ExpressionHolder<T, U> expression);

        NumberExpression<T, U> mod(ExpressionHolder<T, U> expression);

        NumberExpression<T, U> sum();

        <R extends Number & Comparable<R>> NumberExpression<T, R> avg();

        NumberExpression<T, U> max();

        NumberExpression<T, U> min();

        default NumberExpression<T, U> add(U value) {
            return add(root().of(value));
        }

        default NumberExpression<T, U> subtract(U value) {
            return subtract(root().of(value));
        }

        default NumberExpression<T, U> multiply(U value) {
            return multiply(root().of(value));
        }

        default NumberExpression<T, U> divide(U value) {
            return divide(root().of(value));
        }

        default NumberExpression<T, U> mod(U value) {
            return mod(root().of(value));
        }

    }

    interface PathExpression<T, U> extends TypedExpression<T, U>, ColumnHolder<T, U> {
        <R> PathExpression<T, R> get(Path<U, R> path);

        StringExpression<T> get(StringPath<U> path);

        <R extends Number & Comparable<R>> NumberExpression<T, R> get(NumberPath<U, R> path);

        <R extends Comparable<R>> ComparableExpression<T, R> get(ComparablePath<U, R> path);

        BooleanExpression<T> get(BooleanPath<U> path);

    }

    interface StringExpression<T> extends ComparableExpression<T, String> {
        BooleanExpression<T> like(String value);

        default BooleanExpression<T> startWith(String value) {
            return like(value + '%');
        }

        default BooleanExpression<T> endsWith(String value) {
            return like('%' + value);
        }

        default BooleanExpression<T> contains(String value) {
            return like('%' + value + '%');
        }

        BooleanExpression<T> notLike(String value);

        default BooleanExpression<T> notStartWith(String value) {
            return notLike(value + '%');
        }

        default BooleanExpression<T> notEndsWith(String value) {
            return notLike('%' + value);
        }

        default BooleanExpression<T> notContains(String value) {
            return notLike('%' + value + '%');
        }

        StringExpression<T> lower();

        StringExpression<T> upper();

        StringExpression<T> substring(int a, int b);

        StringExpression<T> substring(int a);

        StringExpression<T> trim();

        NumberExpression<T, Integer> length();
    }

    interface Predicate<T> extends ExpressionHolder<T, Boolean> {
        Predicate<T> not();

    }
}
