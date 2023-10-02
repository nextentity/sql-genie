package io.github.genie.sql.core;

import io.github.genie.sql.core.Path.BooleanPath;
import io.github.genie.sql.core.Path.ComparablePath;
import io.github.genie.sql.core.Path.NumberPath;
import io.github.genie.sql.core.Path.StringPath;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@SuppressWarnings("unused")
public interface ExpressionOps<T, U, B> extends Expression<T, U> {

    B eq(U value);

    B eq(Expression<T, U> value);

    B ne(U value);

    B ne(Expression<T, U> value);

    @SuppressWarnings({"unchecked"})
    B in(U... values);

    B in(@NotNull List<? extends Expression<T, U>> values);

    @SuppressWarnings({"unchecked"})
    B notIn(U... values);

    B notIn(@NotNull List<? extends Expression<T, U>> values);

    B isNull();

    NumberOps<T, Integer, B> count();

    B isNotNull();

    interface Root<T> extends PathOps<T, T, Predicate<T>> {

        @Override
        <U> PathExpr<T, U> get(Path<T, U> path);

        @Override
        StringExpr<T> get(StringPath<T> path);

        @Override
        <U extends Number & Comparable<U>> NumberExpr<T, U> get(NumberPath<T, U> path);

        @Override
        <U extends Comparable<U>> ComparableExpr<T, U> get(ComparablePath<T, U> path);

        @Override
        Predicate<T> get(BooleanPath<T> path);

    }

    interface ComparableExpr<T, U extends Comparable<U>> extends ComparableOps<T, U, Predicate<T>> {
    }

    interface NumberExpr<T, U extends Number & Comparable<U>> extends NumberOps<T, U, Predicate<T>> {
    }

    interface PathExpr<T, U> extends PathOps<T, U, Predicate<T>> {
        @Override
        <V> PathExpr<T, V> get(Path<U, V> path);
    }

    interface Predicate<T> extends BooleanOps<T, Predicate<T>>, OpsAnd<T>, OpsOr<T> {
        Predicate<T> not();
    }

    interface StringExpr<T> extends StringOps<T, Predicate<T>> {
    }


    interface PathOps<T, U, B> extends ExpressionOps<T, U, B> {

        <V> PathOps<T, V, B> get(Path<U, V> path);

        StringOps<T, B> get(StringPath<T> path);

        <V extends Number & Comparable<V>> NumberOps<T, V, B> get(NumberPath<T, V> path);

        <V extends Comparable<V>> ComparableOps<T, V, B> get(ComparablePath<T, V> path);

        B get(BooleanPath<T> path);

    }


    interface StringOps<T, B> extends ComparableOps<T, String, B> {

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


        StringOps<T, B> lower();

        StringOps<T, B> upper();

        StringOps<T, B> substring(int a, int b);

        StringOps<T, B> substring(int a);

        StringOps<T, B> trim();

        NumberOps<T, Integer, B> length();

    }

    interface NumberOps<T, U extends Number & Comparable<U>, B> extends ComparableOps<T, U, B> {
        NumberOps<T, U, B> add(U value);

        NumberOps<T, U, B> subtract(U value);

        NumberOps<T, U, B> multiply(U value);

        NumberOps<T, U, B> divide(U value);

        NumberOps<T, U, B> mod(U value);

        NumberOps<T, U, B> add(Expression<T, U> value);

        NumberOps<T, U, B> subtract(Expression<T, U> value);

        NumberOps<T, U, B> multiply(Expression<T, U> value);

        NumberOps<T, U, B> divide(Expression<T, U> value);

        NumberOps<T, U, B> mod(Expression<T, U> value);

        <V extends Number & Comparable<V>> NumberOps<T, V, B> sum();

        <V extends Number & Comparable<V>> NumberOps<T, V, B> avg();

        <V extends Number & Comparable<V>> NumberOps<T, V, B> max();

        <V extends Number & Comparable<V>> NumberOps<T, V, B> min();

    }

    interface ComparableOps<T, U extends Comparable<U>, B> extends ExpressionOps<T, U, B> {

        B ge(U value);

        B gt(U value);

        B le(U value);

        B lt(U value);

        B between(U l, U r);

        B notBetween(U l, U r);

        B ge(Expression<T, U> value);

        B gt(Expression<T, U> value);

        B le(Expression<T, U> value);

        B lt(Expression<T, U> value);

        B between(Expression<T, U> l, Expression<T, U> r);

        B between(Expression<T, U> l, U r);

        B between(U l, Expression<T, U> r);

        B notBetween(Expression<T, U> l, Expression<T, U> r);

        B notBetween(Expression<T, U> l, U r);

        B notBetween(U l, Expression<T, U> r);

        Ordering<T> asc();

        Ordering<T> desc();

    }

    interface OpsOr<T> {
        <R> PathOps<T, R, OrConnector<T>> or(Path<T, R> path);


        <R extends Comparable<R>>
        ComparableOps<T, R, OrConnector<T>> or(ComparablePath<T, R> path);


        <R extends Number & Comparable<R>>
        NumberOps<T, R, OrConnector<T>> or(NumberPath<T, R> path);

        OrConnector<T> or(BooleanPath<T> path);

        StringOps<T, OrConnector<T>> or(StringPath<T> path);

        OrConnector<T> or(Expression<T, Boolean> value);

        OrConnector<T> or(List<Expression<T, Boolean>> values);

    }

    interface OpsAnd<T> {
        <R> PathOps<T, R, AndConnector<T>> and(Path<T, R> path);


        <R extends Comparable<R>>
        ComparableOps<T, R, AndConnector<T>> and(ComparablePath<T, R> path);


        <R extends Number & Comparable<R>>
        NumberOps<T, R, AndConnector<T>> and(NumberPath<T, R> path);

        StringOps<T, AndConnector<T>> and(StringPath<T> path);

        AndConnector<T> and(BooleanPath<T> path);

        AndConnector<T> and(Expression<T, Boolean> value);

        AndConnector<T> and(List<Expression<T, Boolean>> values);
    }

    interface BooleanOps<T, B> extends ComparableOps<T, Boolean, B> {

        Predicate<T> then();

    }


    interface OrConnector<T> extends BooleanOps<T, OrConnector<T>>, OpsOr<T> {

    }

    interface AndConnector<T> extends BooleanOps<T, AndConnector<T>>, OpsAnd<T> {

    }


}
