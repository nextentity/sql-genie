package io.github.genie.sql.api;

import io.github.genie.sql.api.Path.BooleanPath;
import io.github.genie.sql.api.Path.ComparablePath;
import io.github.genie.sql.api.Path.NumberPath;
import io.github.genie.sql.api.Path.StringPath;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

@SuppressWarnings("unused")
public interface ExpressionOperator<T, U, B> extends ExpressionHolder<T, U> {

    B eq(U value);

    B eq(ExpressionHolder<T, U> value);

    B ne(U value);

    B ne(ExpressionHolder<T, U> value);

    @SuppressWarnings({"unchecked"})
    B in(U... values);

    B in(@NotNull List<? extends ExpressionHolder<T, U>> values);

    B in(@NotNull Collection<? extends U> values);

    @SuppressWarnings({"unchecked"})
    B notIn(U... values);

    B notIn(@NotNull List<? extends ExpressionHolder<T, U>> values);

    B notIn(@NotNull Collection<? extends U> values);

    B isNull();

    NumberOperator<T, Integer, B> count();

    B isNotNull();

    interface Root<T> extends PathOperator<T, T, Predicate<T>> {

        @Override
        <U> PathOperator<T, U, Predicate<T>> get(Path<T, U> path);

        @Override
        StringOperator<T, Predicate<T>> get(StringPath<T> path);

        @Override
        <U extends Number & Comparable<U>> NumberOperator<T, U, Predicate<T>> get(NumberPath<T, U> path);

        @Override
        <U extends Comparable<U>> ComparableOperator<T, U, Predicate<T>> get(ComparablePath<T, U> path);

        @Override
        Predicate<T> get(BooleanPath<T> path);

    }

    interface Predicate<T> extends BooleanOperator<T, Predicate<T>>, PredicateAndOperator<T>, PredicateOrOperator<T> {
        Predicate<T> not();
    }

    interface PathOperator<T, U, B> extends ExpressionOperator<T, U, B> {

        <V> PathOperator<T, V, B> get(Path<U, V> path);

        StringOperator<T, B> get(StringPath<U> path);

        <V extends Number & Comparable<V>> NumberOperator<T, V, B> get(NumberPath<U, V> path);

        <V extends Comparable<V>> ComparableOperator<T, V, B> get(ComparablePath<U, V> path);

        B get(BooleanPath<U> path);

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

        default B nitStartWith(String value) {
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

    interface NumberOperator<T, U extends Number & Comparable<U>, B> extends ComparableOperator<T, U, B> {
        NumberOperator<T, U, B> add(U value);

        NumberOperator<T, U, B> subtract(U value);

        NumberOperator<T, U, B> multiply(U value);

        NumberOperator<T, U, B> divide(U value);

        NumberOperator<T, U, B> mod(U value);

        NumberOperator<T, U, B> add(ExpressionHolder<T, U> value);

        NumberOperator<T, U, B> subtract(ExpressionHolder<T, U> value);

        NumberOperator<T, U, B> multiply(ExpressionHolder<T, U> value);

        NumberOperator<T, U, B> divide(ExpressionHolder<T, U> value);

        NumberOperator<T, U, B> mod(ExpressionHolder<T, U> value);

        <V extends Number & Comparable<V>> NumberOperator<T, V, B> sum();

        <V extends Number & Comparable<V>> NumberOperator<T, V, B> avg();

        <V extends Number & Comparable<V>> NumberOperator<T, V, B> max();

        <V extends Number & Comparable<V>> NumberOperator<T, V, B> min();

    }

    interface ComparableOperator<T, U extends Comparable<U>, B> extends ExpressionOperator<T, U, B> {

        B ge(U value);

        B gt(U value);

        B le(U value);

        B lt(U value);

        B between(U l, U r);

        B notBetween(U l, U r);

        B ge(ExpressionHolder<T, U> value);

        B gt(ExpressionHolder<T, U> value);

        B le(ExpressionHolder<T, U> value);

        B lt(ExpressionHolder<T, U> value);

        B between(ExpressionHolder<T, U> l, ExpressionHolder<T, U> r);

        B between(ExpressionHolder<T, U> l, U r);

        B between(U l, ExpressionHolder<T, U> r);

        B notBetween(ExpressionHolder<T, U> l, ExpressionHolder<T, U> r);

        B notBetween(ExpressionHolder<T, U> l, U r);

        B notBetween(U l, ExpressionHolder<T, U> r);

        Order<T> asc();

        Order<T> desc();

    }

    interface PredicateOrOperator<T> {
        <R> PathOperator<T, R, OrConnector<T>> or(Path<T, R> path);


        <R extends Comparable<R>>
        ComparableOperator<T, R, OrConnector<T>> or(ComparablePath<T, R> path);


        <R extends Number & Comparable<R>>
        NumberOperator<T, R, OrConnector<T>> or(NumberPath<T, R> path);

        OrConnector<T> or(BooleanPath<T> path);

        StringOperator<T, OrConnector<T>> or(StringPath<T> path);

        OrConnector<T> or(ExpressionHolder<T, Boolean> value);

        OrConnector<T> or(List<ExpressionHolder<T, Boolean>> values);

    }

    interface PredicateAndOperator<T> {
        <R> PathOperator<T, R, AndConnector<T>> and(Path<T, R> path);


        <R extends Comparable<R>>
        ComparableOperator<T, R, AndConnector<T>> and(ComparablePath<T, R> path);


        <R extends Number & Comparable<R>>
        NumberOperator<T, R, AndConnector<T>> and(NumberPath<T, R> path);

        StringOperator<T, AndConnector<T>> and(StringPath<T> path);

        AndConnector<T> and(BooleanPath<T> path);

        AndConnector<T> and(ExpressionHolder<T, Boolean> value);

        AndConnector<T> and(List<ExpressionHolder<T, Boolean>> values);
    }

    interface BooleanOperator<T, B> extends ComparableOperator<T, Boolean, B> {

        Predicate<T> then();

    }


    interface OrConnector<T> extends BooleanOperator<T, OrConnector<T>>, PredicateOrOperator<T> {

    }

    interface AndConnector<T> extends BooleanOperator<T, AndConnector<T>>, PredicateAndOperator<T> {

    }


}
