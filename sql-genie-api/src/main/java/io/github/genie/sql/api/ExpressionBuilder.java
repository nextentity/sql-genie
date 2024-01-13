package io.github.genie.sql.api;

import io.github.genie.sql.api.ExpressionOperator.ComparableOperator;
import io.github.genie.sql.api.ExpressionOperator.NumberOperator;
import io.github.genie.sql.api.ExpressionOperator.PathOperator;
import io.github.genie.sql.api.ExpressionOperator.StringOperator;
import io.github.genie.sql.api.Path.BooleanPath;
import io.github.genie.sql.api.Path.ComparablePath;
import io.github.genie.sql.api.Path.NumberPath;
import io.github.genie.sql.api.Path.StringPath;

import java.util.List;

public interface ExpressionBuilder<T> {

    <U> OperablePath<T, U> get(Path<T, U> path);

    OperableString<T> get(StringPath<T> path);

    <U extends Number & Comparable<U>> OperableNumber<T, U> get(NumberPath<T, U> path);

    <U extends Comparable<U>> OperableComparable<T, U> get(ComparablePath<T, U> path);

    OperableBoolean<T> get(BooleanPath<T> path);

    <U extends Number & Comparable<U>> OperableNumber<T, U> min(NumberPath<T, U> path);

    <U extends Number & Comparable<U>> OperableNumber<T, U> max(NumberPath<T, U> path);

    <U extends Number & Comparable<U>> OperableNumber<T, U> sum(NumberPath<T, U> path);

    <U extends Number & Comparable<U>> OperableNumber<T, U> avg(NumberPath<T, U> path);

    OperableNumber<T, Integer> count(Path<T, ?> path);


    interface OperableExpression<T, U> extends ExpressionOperator<T, U, OperableBoolean<T>> {
        @Override
        OperableNumber<T, Integer> count();
    }

    interface OperableComparable<T, U extends Comparable<U>>
            extends ComparableOperator<T, U, OperableBoolean<T>>, OperableExpression<T, U> {
        Order<T> asc();

        Order<T> desc();
    }

    interface OperablePath<T, U> extends PathOperator<T, U, OperableBoolean<T>>, OperableExpression<T, U> {
        @Override
        <R> OperablePath<T, R> get(Path<U, R> path);

        @Override
        OperableString<T> get(StringPath<U> path);

        @Override
        <R extends Number & Comparable<R>> OperableNumber<T, R> get(NumberPath<U, R> path);

        @Override
        <R extends Comparable<R>> OperableComparable<T, R> get(ComparablePath<U, R> path);

        @Override
        OperableBoolean<T> get(BooleanPath<U> path);
    }

    interface OperableString<T> extends StringOperator<T, OperableBoolean<T>>, OperableComparable<T, String> {
        OperableString<T> lower();

        OperableString<T> upper();

        OperableString<T> substring(int a, int b);

        OperableString<T> substring(int a);

        OperableString<T> trim();

        OperableNumber<T, Integer> length();
    }

    interface OperableNumber<T, U extends Number & Comparable<U>>
            extends NumberOperator<T, U, OperableBoolean<T>>, OperableComparable<T, U> {
        @Override
        OperableNumber<T, U> add(U value);

        @Override
        OperableNumber<T, U> subtract(U value);

        @Override
        OperableNumber<T, U> multiply(U value);

        @Override
        OperableNumber<T, U> divide(U value);

        @Override
        OperableNumber<T, U> mod(U value);

        @Override
        OperableNumber<T, U> add(ExpressionHolder<T, U> value);

        @Override
        OperableNumber<T, U> subtract(ExpressionHolder<T, U> value);

        @Override
        OperableNumber<T, U> multiply(ExpressionHolder<T, U> value);

        @Override
        OperableNumber<T, U> divide(ExpressionHolder<T, U> value);

        @Override
        OperableNumber<T, U> mod(ExpressionHolder<T, U> value);

        @Override
        <R extends Number & Comparable<R>> OperableNumber<T, R> sum();

        @Override
        <R extends Number & Comparable<R>> OperableNumber<T, R> avg();

        @Override
        <R extends Number & Comparable<R>> OperableNumber<T, R> max();

        @Override
        <R extends Number & Comparable<R>> OperableNumber<T, R> min();
    }

    interface OperableBoolean<T> extends OperableComparable<T, Boolean>, OperableAnd<T>, OperableOr<T> {
        OperableBoolean<T> not();
    }

    interface OperableOr<T> {

        <R> OperablePath<T, R> or(Path<T, R> path);

        <R extends Comparable<R>> OperableComparable<T, R> or(ComparablePath<T, R> path);

        <R extends Number & Comparable<R>> OperableNumber<T, R> or(NumberPath<T, R> path);

        OperableOr<T> or(BooleanPath<T> path);

        OperableString<T> or(StringPath<T> path);

        OperableOr<T> or(ExpressionHolder<T, Boolean> expression);

        OperableOr<T> or(List<ExpressionHolder<T, Boolean>> expressions);

    }

    interface OperableAnd<T> {

        <R> OperablePath<T, R> and(Path<T, R> path);

        <R extends Comparable<R>> OperableComparable<T, R> and(ComparablePath<T, R> path);

        <R extends Number & Comparable<R>> OperableNumber<T, R> and(NumberPath<T, R> path);

        OperableAnd<T> and(BooleanPath<T> path);

        OperableString<T> and(StringPath<T> path);

        OperableAnd<T> and(ExpressionHolder<T, Boolean> expression);

        OperableAnd<T> and(List<ExpressionHolder<T, Boolean>> expressions);

    }
}
