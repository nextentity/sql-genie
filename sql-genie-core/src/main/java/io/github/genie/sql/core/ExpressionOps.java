package io.github.genie.sql.core;

import io.github.genie.sql.core.Expression.TypedExpression;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface ExpressionOps<T, U, B> extends TypedExpression<T, U> {

    B eq(U value);

    B eq(TypedExpression<T, U> value);

    B ne(U value);

    B ne(TypedExpression<T, U> value);

    @SuppressWarnings({"unchecked"})
    B in(U... values);

    B in(@NotNull List<? extends TypedExpression<T, U>> values);

    B isNull();

    NumberOps<T, Integer, B> count();

    B isNotNull();


    interface RootPath<T> extends PathOps<T, T, PredicateOps<T>> {

    }

    interface PathOps<T, U, B> extends ExpressionOps<T, U, B> {

        <V> PathOps<T, V, B> get(Path<U, V> path);

        StringOps<T, B> get(Path.StringPath<T> path);

        <V extends Number & Comparable<V>> NumberOps<T, V, B> get(Path.NumberPath<T, V> path);

        <V extends Comparable<V>> ComparableOps<T, V, B> get(Path.ComparablePath<T, V> path);

        B get(Path.BooleanPath<T> path);

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

        NumberOps<T, U, B> add(TypedExpression<T, U> value);

        NumberOps<T, U, B> subtract(TypedExpression<T, U> value);

        NumberOps<T, U, B> multiply(TypedExpression<T, U> value);

        NumberOps<T, U, B> divide(TypedExpression<T, U> value);

        NumberOps<T, U, B> mod(TypedExpression<T, U> value);

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

        B ge(TypedExpression<T, U> value);

        B gt(TypedExpression<T, U> value);

        B le(TypedExpression<T, U> value);

        B lt(TypedExpression<T, U> value);

        B between(TypedExpression<T, U> l, TypedExpression<T, U> r);

        B between(TypedExpression<T, U> l, U r);

        B between(U l, TypedExpression<T, U> r);

        Ordering<T> asc();

        Ordering<T> desc();

    }

    interface OpsOr<T> {
        <R> PathOps<T, R, OrConnector<T>> or(Path<T, R> path);


        <R extends Comparable<R>>
        ComparableOps<T, R, OrConnector<T>> or(Path.ComparablePath<T, R> path);


        <R extends Number & Comparable<R>>
        NumberOps<T, R, OrConnector<T>> or(Path.NumberPath<T, R> path);

        OrConnector<T> or(Path.BooleanPath<T> path);

        StringOps<T, OrConnector<T>> or(Path.StringPath<T> path);

    }

    interface OpsAnd<T> {
        <R> PathOps<T, R, AndConnector<T>> and(Path<T, R> path);


        <R extends Comparable<R>>
        ComparableOps<T, R, AndConnector<T>> and(Path.ComparablePath<T, R> path);


        <R extends Number & Comparable<R>>
        NumberOps<T, R, AndConnector<T>> and(Path.NumberPath<T, R> path);

        StringOps<T, AndConnector<T>> and(Path.StringPath<T> path);

        AndConnector<T> and(Path.BooleanPath<T> path);
    }

    interface PredicateOps<T> extends BooleanOps<T, PredicateOps<T>>, OpsAnd<T>, OpsOr<T> {

        PredicateOps<T> not();

    }

    interface BooleanOps<T, B> extends Predicate<T>, ComparableOps<T, Boolean, B> {

        B and(TypedExpression<T, Boolean> value);

        B or(TypedExpression<T, Boolean> value);

        @SuppressWarnings("unchecked")
        B and(TypedExpression<T, Boolean>... values);

        @SuppressWarnings("unchecked")
        B or(TypedExpression<T, Boolean>... values);

        B and(List<TypedExpression<T, Boolean>> values);

        B or(List<TypedExpression<T, Boolean>> values);


    }


    interface OrConnector<T> extends BooleanOps<T, OrConnector<T>>, OpsOr<T> {

    }

    interface AndConnector<T> extends BooleanOps<T, AndConnector<T>>, OpsAnd<T> {

    }

}
