package io.github.genie.sql.core;

import io.github.genie.sql.core.Expression.Meta;
import io.github.genie.sql.core.Expression.TypedExpression;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public class ExpressionChainBuilder {

    interface Root<T, B> extends PathOperation<T, T, B> {

    }

    interface PathOperation<T, U, B> {

        <V, R extends PathOperation<T, V, B> & CommonOperation<T, V, B>> R get(Path<U, V> path);

        StringOperation<T, B> get(Path.StringPath<T> path);

        <V extends Number & Comparable<V>> NumberOperation<T, V, B> get(Path.NumberPath<T, V> path);

        <V extends Comparable<V>> ComparableOperation<T, V, B> get(Path.ComparablePath<T, V> path);

        BooleanOperation<T, B> get(Path.BooleanPath<T> path);

    }


    interface CommonOperation<T, U, B> extends TypedExpression<T, U> {

        default B eq(U value) {
            return operateAsBoolean(this, Operator.EQ, Metas.ofList(value));
        }

        default B eq(TypedExpression<T, U> value) {
            return operateAsBoolean(this, Operator.EQ, Metas.ofList(value));
        }

        default B ne(U value) {
            return operateAsBoolean(this, Operator.NE, Metas.ofList(value));
        }

        default B ne(TypedExpression<T, U> value) {
            return operateAsBoolean(this, Operator.NE, Metas.ofList(value));
        }

        @SuppressWarnings({"unchecked"})
        default B in(U... values) {
            return operateAsBoolean(this, Operator.IN, Arrays.stream(values).map(Metas::of).toList());
        }

        default B in(@NotNull List<? extends TypedExpression<T, U>> values) {
            return operateAsBoolean(this, Operator.IN, values.stream().map(Metas::of).toList());
        }

        default B isNull() {
            return operateAsBoolean(this, Operator.IS_NULL, List.of());
        }

        default NumberOperation<T, Integer, B> count() {
            return operateAsNumber(this, Operator.COUNT, List.of());
        }

        default B isNotNull() {
            return operateAsBoolean(this, Operator.IS_NOT_NULL, List.of());
        }


    }

    interface StringOperation<T, B> extends ComparableOperation<T, String, B> {

        default B like(String value) {
            return operateAsBoolean(this, Operator.LIKE, Metas.ofList(value));
        }

        default B startWith(String value) {
            return like(value + "%");
        }

        default B endsWith(String value) {
            return like("%" + value);
        }

        default B contains(String value) {
            return like("%" + value + "%");

        }

        default StringOperation<T, B> lower() {
            return operateAsString(this, Operator.LOWER, List.of());

        }

        default StringOperation<T, B> upper() {
            return operateAsString(this, Operator.UPPER, List.of());

        }

        default StringOperation<T, B> substring(int a, int b) {
            return operateAsString(this, Operator.LOWER, List.of());

        }

        default StringOperation<T, B> substring(int a) {
            return operateAsString(this, Operator.SUBSTRING, Metas.ofList(a));
        }

        default StringOperation<T, B> trim() {
            return operateAsString(this, Operator.TRIM, List.of());

        }

        default NumberOperation<T, Integer, B> length() {
            return operateAsNumber(this, Operator.LENGTH, List.of());
        }

    }

    interface NumberOperation<T, U extends Number & Comparable<U>, B> extends ComparableOperation<T, U, B> {
        default NumberOperation<T, U, B> add(U value) {
            return operateAsNumber(this, Operator.ADD, Metas.ofList(value));
        }

        default NumberOperation<T, U, B> subtract(U value) {
            return operateAsNumber(this, Operator.SUBTRACT, Metas.ofList(value));
        }

        default NumberOperation<T, U, B> multiply(U value) {
            return operateAsNumber(this, Operator.MULTIPLY, Metas.ofList(value));
        }

        default NumberOperation<T, U, B> divide(U value) {
            return operateAsNumber(this, Operator.DIVIDE, Metas.ofList(value));
        }

        default NumberOperation<T, U, B> mod(U value) {
            return operateAsNumber(this, Operator.MOD, Metas.ofList(value));
        }

        default NumberOperation<T, U, B> add(TypedExpression<T, U> value) {
            return operateAsNumber(this, Operator.ADD, Metas.ofList(value));
        }

        default NumberOperation<T, U, B> subtract(TypedExpression<T, U> value) {
            return operateAsNumber(this, Operator.SUBTRACT, Metas.ofList(value));
        }

        default NumberOperation<T, U, B> multiply(TypedExpression<T, U> value) {
            return operateAsNumber(this, Operator.MULTIPLY, Metas.ofList(value));
        }

        default NumberOperation<T, U, B> divide(TypedExpression<T, U> value) {
            return operateAsNumber(this, Operator.DIVIDE, Metas.ofList(value));
        }

        default NumberOperation<T, U, B> mod(TypedExpression<T, U> value) {
            return operateAsNumber(this, Operator.MOD, Metas.ofList(value));
        }

        default <V extends Number & Comparable<V>> NumberOperation<T, V, B> sum() {
            return operateAsNumber(this, Operator.SUM, List.of());
        }

        default <V extends Number & Comparable<V>> NumberOperation<T, V, B> avg() {
            return operateAsNumber(this, Operator.AVG, List.of());
        }

        default <V extends Number & Comparable<V>> NumberOperation<T, V, B> max() {
            return operateAsNumber(this, Operator.MAX, List.of());
        }

        default <V extends Number & Comparable<V>> NumberOperation<T, V, B> min() {
            return operateAsNumber(this, Operator.MIN, List.of());
        }

    }

    interface ComparableOperation<T, U extends Comparable<U>, B> extends CommonOperation<T, U, B> {

        default B ge(U value) {
            return operateAsBoolean(this, Operator.GE, Metas.ofList(value));
        }

        default B gt(U value) {
            return operateAsBoolean(this, Operator.GT, Metas.ofList(value));
        }

        default B le(U value) {
            return operateAsBoolean(this, Operator.LE, Metas.ofList(value));
        }

        default B lt(U value) {
            return operateAsBoolean(this, Operator.LT, Metas.ofList(value));
        }

        default B between(U l, U r) {
            return operateAsBoolean(this, Operator.BETWEEN, List.of(Metas.of(l), Metas.of(r)));
        }

        default B ge(TypedExpression<T, U> value) {
            return operateAsBoolean(this, Operator.GE, Metas.ofList(value));
        }

        default B gt(TypedExpression<T, U> value) {
            return operateAsBoolean(this, Operator.GT, Metas.ofList(value));
        }

        default B le(TypedExpression<T, U> value) {
            return operateAsBoolean(this, Operator.LE, Metas.ofList(value));
        }

        default B lt(TypedExpression<T, U> value) {
            return operateAsBoolean(this, Operator.LT, Metas.ofList(value));
        }

        default B between(TypedExpression<T, U> l, TypedExpression<T, U> r) {
            return operateAsBoolean(this, Operator.BETWEEN, List.of(Metas.of(l), Metas.of(r)));
        }

        default B between(TypedExpression<T, U> l, U r) {
            return operateAsBoolean(this, Operator.BETWEEN, List.of(Metas.of(l), Metas.of(r)));
        }

        default B between(U l, TypedExpression<T, U> r) {
            return operateAsBoolean(this, Operator.BETWEEN, List.of(Metas.of(l), Metas.of(r)));
        }

        default Ordering<T> asc() {
            return new OrderingImpl<>(this, Ordering.SortOrder.ASC);
        }

        default Ordering<T> desc() {
            return new OrderingImpl<>(this, Ordering.SortOrder.DESC);
        }

    }

    interface BooleanOperation<T, B> extends Predicate<T>, CommonOperation<T, Boolean, B> {

        default B and(TypedExpression<T, Boolean> value) {
            return operateAsBoolean(this, Operator.AND, Metas.ofList(value));
        }

        default B or(TypedExpression<T, Boolean> value) {
            return operateAsBoolean(this, Operator.OR, Metas.ofList(value));
        }

        @SuppressWarnings("unchecked")
        default B and(TypedExpression<T, Boolean>... values) {
            return operateAsBoolean(this, Operator.AND, Arrays.stream(values).map(Metas::of).toList());
        }

        @SuppressWarnings("unchecked")
        default B or(TypedExpression<T, Boolean>... values) {
            return operateAsBoolean(this, Operator.OR, Arrays.stream(values).map(Metas::of).toList());
        }

        default B and(List<TypedExpression<T, Boolean>> values) {
            return operateAsBoolean(this, Operator.AND, values.stream().map(Metas::of).toList());
        }

        default B or(List<TypedExpression<T, Boolean>> values) {
            return operateAsBoolean(this, Operator.OR, values.stream().map(Metas::of).toList());
        }


    }

    // interface LogicConnector<T> extends LogicAndConnector<T>, LogicOrConnector<T>, BooleanOperation<T, LogicConnector<T>> {
    //
    //
    // }


    interface LogicOrConnector<T> extends BooleanOperation<T, LogicOrConnector<T>> {
        default <R, S extends PathOperation<T, R, LogicOrConnector<T>>
                & CommonOperation<T, R, LogicOrConnector<T>>>
        S or(Path<T, R> path) {
            return Util.cast(PathOperationImpl.or(meta(), path));
        }


        default <R extends Comparable<R>, S extends PathOperation<T, R, LogicOrConnector<T>> & CommonOperation<T, R, LogicOrConnector<T>>>
        S or(Path.ComparablePath<T, R> path) {
            return Util.cast(PathOperationImpl.or(meta(), path));
        }


        default <R extends Number & Comparable<R>, S extends PathOperation<T, R, LogicOrConnector<T>> & NumberOperation<T, R, LogicOrConnector<T>>>
        S or(Path.NumberPath<T, R> path) {
            return Util.cast(PathOperationImpl.or(meta(), path));
        }
    }

    interface LogicAndConnector<T> extends BooleanOperation<T, LogicAndConnector<T>> {
        default <R, S extends PathOperation<T, R, LogicAndConnector<T>> & CommonOperation<T, R, LogicAndConnector<T>>> S and(Path<T, R> path) {
            return Util.cast(PathOperationImpl.and(meta(), path));
        }


        default <R extends Comparable<R>, S extends PathOperation<T, R, LogicAndConnector<T>>
                & CommonOperation<T, R, LogicAndConnector<T>>>
        S and(Path.ComparablePath<T, R> path) {
            return Util.cast(PathOperationImpl.and(meta(), path));
        }


        default <R extends Number & Comparable<R>,
                S extends PathOperation<T, R, LogicAndConnector<T>>
                        & NumberOperation<T, R, LogicAndConnector<T>>>
        S and(Path.NumberPath<T, R> path) {
            return Util.cast(PathOperationImpl.and(meta(), path));
        }


    }

    static <T> T operateAsBoolean(Expression leftOperand,
                                  Operator operator,
                                  List<? extends Meta> rightOperand) {
        // noinspection unchecked
        return (T) doOperateAsBoolean(leftOperand, operator, rightOperand);

    }

    private static <T, B> BooleanOperation<T, B> doOperateAsBoolean(Expression leftOperand, Operator operator, List<? extends Meta> rightOperand) {
        return leftOperand instanceof CustomizerOperator cst
                ? cst.operateAsBoolean(operator, rightOperand)
                : () -> Metas.operate(leftOperand, operator, rightOperand);
    }

    static <T, B> StringOperation<T, B> operateAsString(Expression leftOperand,
                                                        Operator operator,
                                                        List<? extends Meta> rightOperand) {
        return leftOperand instanceof CustomizerOperator cst
                ? cst.operateAsString(operator, rightOperand)
                : () -> Metas.operate(leftOperand, operator, rightOperand);
    }

    static <T, U extends Number & Comparable<U>, B> NumberOperation<T, U, B> operateAsNumber(Expression leftOperand,
                                                                                             Operator operator,
                                                                                             List<? extends Meta> rightOperand) {
        return leftOperand instanceof CustomizerOperator cst
                ? cst.operateAsNumber(operator, rightOperand)
                : () -> Metas.operate(leftOperand, operator, rightOperand);

    }

    interface CustomizerOperator {
        <T, B> BooleanOperation<T, B> operateAsBoolean(Operator operator, List<? extends Meta> rightOperand);

        <T, U extends Number & Comparable<U>, B> NumberOperation<T, U, B>
        operateAsNumber(Operator operator, List<? extends Meta> rightOperand);

        <T, B> StringOperation<T, B> operateAsString(Operator operator, List<? extends Meta> rightOperand);
    }

}
