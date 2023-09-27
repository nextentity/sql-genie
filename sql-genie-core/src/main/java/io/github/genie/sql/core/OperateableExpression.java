package io.github.genie.sql.core;


import io.github.genie.sql.core.ExpressionChainBuilder.*;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static io.github.genie.sql.core.BasicExpressions.operateAsNumber;
import static io.github.genie.sql.core.BasicExpressions.operateAsString;
import static io.github.genie.sql.core.BasicExpressions.*;


@SuppressWarnings("unused")
public interface OperateableExpression<T, U> extends Expression.TypedExpression<T, U> {

    default BooleanExpression<T> eq(U value) {
        return operateAsPredicate(this, Operator.EQ, List.of(() -> BasicExpressions.of(value)));
    }

    default BooleanExpression<T> eq(TypedExpression<T, U> expression) {
        return operateAsPredicate(this, Operator.EQ, List.of(expression));
    }

    default BooleanExpression<T> ne(U value) {
        return operateAsPredicate(this, Operator.NE, List.of(() -> BasicExpressions.of(value)));
    }

    default BooleanExpression<T> ne(TypedExpression<T, U> expression) {
        return operateAsPredicate(this, Operator.NE, List.of(expression));
    }

    @SuppressWarnings({"unchecked"})
    default BooleanExpression<T> in(U... values) {
        List<Expression> list = Arrays.stream(values)
                .map(BasicExpressions::of)
                .<Expression>map(it -> () -> it)
                .toList();
        return operateAsPredicate(this, Operator.IN, list);
    }

    default BooleanExpression<T> in(@NotNull List<? extends TypedExpression<T, U>> values) {
        return operateAsPredicate(this, Operator.IN, values);
    }

    default BooleanExpression<T> isNull() {
        return operateAsPredicate(this, Operator.IS_NULL, Collections.emptyList());
    }

    default NumberExpression<T, Integer> count() {
        return operateAsNumber(this, Operator.COUNT, List.of());
    }

    // default Ordering<T> asc() {
    //     return new OrderingImpl<>(this, Ordering.SortOrder.ASC);
    // }
    //
    // default Ordering<T> desc() {
    //     return new OrderingImpl<>(this, Ordering.SortOrder.DESC);
    // }

    default BooleanExpression<T> isNotNull() {
        return operateAsPredicate(this, Operator.IS_NOT_NULL, Collections.emptyList());
    }

    interface BooleanExpression<T> extends Predicate<T>, ComparableExpression<T, Boolean> {
        default BooleanExpression<T> and(TypedExpression<T, Boolean> value) {
            return operateAsPredicate(this, Operator.AND, List.of(value));
        }

        default BooleanExpression<T> or(TypedExpression<T, Boolean> value) {
            return operateAsPredicate(this, Operator.OR, List.of(value));
        }

        @SuppressWarnings("unchecked")
        default BooleanExpression<T> and(TypedExpression<T, Boolean>... values) {
            return operateAsPredicate(this, Operator.AND, Arrays.asList(values));
        }

        @SuppressWarnings("unchecked")
        default BooleanExpression<T> or(TypedExpression<T, Boolean>... values) {
            return operateAsPredicate(this, Operator.OR, Arrays.asList(values));
        }

        default BooleanExpression<T> and(List<TypedExpression<T, Boolean>> values) {
            return operateAsPredicate(this, Operator.AND, values);
        }

        default BooleanExpression<T> or(List<TypedExpression<T, Boolean>> values) {
            return operateAsPredicate(this, Operator.OR, values);
        }

        default BooleanExpression<T> not() {
            return operateAsPredicate(this, Operator.NOT, List.of());
        }

        default <R, S extends PathOperation<T, R, LogicOrConnector<T>>
                & CommonOperation<T, R, LogicOrConnector<T>>>
        S or(Path<T, R> path) {
            // noinspection unchecked
            return (S) PathOperationImpl.or(meta(), path);
        }


        default <R extends Comparable<R>, S extends PathOperation<T, R, LogicOrConnector<T>> & CommonOperation<T, R, LogicOrConnector<T>>>
        S or(Path.ComparablePath<T, R> path) {
            // noinspection unchecked
            return (S) PathOperationImpl.or(meta(), path);
        }


        default <R extends Number & Comparable<R>, S extends PathOperation<T, R, LogicOrConnector<T>> & NumberOperation<T, R, LogicOrConnector<T>>>
        S or(Path.NumberPath<T, R> path) {
            // noinspection unchecked
            return (S) PathOperationImpl.or(meta(), path);
        }

        default <R, S extends PathOperation<T, R, LogicAndConnector<T>> & CommonOperation<T, R, LogicAndConnector<T>>> S and(Path<T, R> path) {
            return Util.cast(PathOperationImpl.and(this.meta(), path));
        }


        default <R extends Comparable<R>, S extends PathOperation<T, R, LogicAndConnector<T>>
                & ComparableOperation<T, R, LogicAndConnector<T>>>
        S and(Path.ComparablePath<T, R> path) {
            return Util.cast(PathOperationImpl.and(this.meta(), path));
        }


        default <R extends Number & Comparable<R>, S extends PathOperation<T, R, LogicAndConnector<T>> & NumberOperation<T, R, LogicAndConnector<T>>>
        S and(Path.NumberPath<T, R> path) {
            return Util.cast(PathOperationImpl.and(this.meta(), path));
        }


        // default <R, S extends PathVisitor<T, R> & OperateableExpression<T, R>> S and(Path<T, R> path) {
        //     return Util.cast(ExpressionBuilders.and(this, path));
        // }
        //
        // default <R, S extends PathVisitor<T, R> & OperateableExpression<T, R>> S or(Path<T, R> path) {
        //     return Util.cast(ExpressionBuilders.or(this, path));
        // }
        //
        // default <R extends Comparable<R>, S extends PathVisitor<T, R> & ComparableExpression<T, R>>
        // S and(ComparablePath<T, R> path) {
        //     return Util.cast(ExpressionBuilders.and(this, path));
        // }
        //
        // default <R extends Comparable<R>, S extends PathVisitor<T, R> & ComparableExpression<T, R>>
        // S or(ComparablePath<T, R> path) {
        //     return Util.cast(ExpressionBuilders.or(this, path));
        // }
        //
        // default <R extends Number & Comparable<R>, S extends PathVisitor<T, R> & NumberExpression<T, R>>
        // S and(Path.NumberPath<T, R> path) {
        //     return Util.cast(ExpressionBuilders.and(this, path));
        // }
        //
        // default <R extends Number & Comparable<R>, S extends PathVisitor<T, R> & NumberExpression<T, R>>
        // S or(Path.NumberPath<T, R> path) {
        //     return Util.cast(ExpressionBuilders.or(this, path));
        // }

    }

    interface ComparableExpression<T, U extends Comparable<U>> extends OperateableExpression<T, U> {

        default BooleanExpression<T> ge(U value) {
            return operateAsPredicate(this, Operator.GE, List.of(() -> BasicExpressions.of(value)));
        }

        default BooleanExpression<T> gt(U value) {
            return operateAsPredicate(this, Operator.GT, List.of(() -> BasicExpressions.of(value)));
        }

        default BooleanExpression<T> le(U value) {
            return operateAsPredicate(this, Operator.LE, List.of(() -> BasicExpressions.of(value)));
        }

        default BooleanExpression<T> lt(U value) {
            return operateAsPredicate(this, Operator.LT, List.of(() -> BasicExpressions.of(value)));
        }

        default BooleanExpression<T> between(U l, U r) {
            return operateAsPredicate(this, Operator.BETWEEN,
                    List.of(() -> BasicExpressions.of(l), () -> BasicExpressions.of(r)));
        }

        default BooleanExpression<T> ge(ComparableExpression<T, U> value) {
            return operateAsPredicate(this, Operator.GE, List.of(value));
        }

        default BooleanExpression<T> gt(ComparableExpression<T, U> value) {
            return operateAsPredicate(this, Operator.GT, List.of(BasicExpressions.of(value)));
        }

        default BooleanExpression<T> le(ComparableExpression<T, U> value) {
            return operateAsPredicate(this, Operator.LE, List.of(BasicExpressions.of(value)));
        }

        default BooleanExpression<T> lt(ComparableExpression<T, U> value) {
            return operateAsPredicate(this, Operator.LT, List.of(BasicExpressions.of(value)));
        }

        default BooleanExpression<T> between(ComparableExpression<T, U> l, ComparableExpression<T, U> r) {
            return operateAsPredicate(this, Operator.BETWEEN, List.of(BasicExpressions.of(l), BasicExpressions.of(r)));
        }

        default BooleanExpression<T> between(ComparableExpression<T, U> l, U r) {
            return operateAsPredicate(this, Operator.BETWEEN,
                    List.of(l, () -> BasicExpressions.of(r)));
        }

        default BooleanExpression<T> between(U l, ComparableExpression<T, U> r) {
            return operateAsPredicate(this, Operator.BETWEEN, List.of(() -> BasicExpressions.of(l), r));
        }

        default Ordering<T> asc() {
            return new OrderingImpl<>(this, Ordering.SortOrder.ASC);
        }

        default Ordering<T> desc() {
            return new OrderingImpl<>(this, Ordering.SortOrder.DESC);
        }

    }

    interface NumberExpression<T, U extends Number & Comparable<U>> extends ComparableExpression<T, U> {

        default NumberExpression<T, U> add(U value) {
            return operateAsNumber(this, Operator.ADD, List.of(() -> BasicExpressions.of(value)));
        }

        default NumberExpression<T, U> subtract(U value) {
            return operateAsNumber(this, Operator.SUBTRACT, List.of(() -> BasicExpressions.of(value)));
        }

        default NumberExpression<T, U> multiply(U value) {
            return operateAsNumber(this, Operator.MULTIPLY, List.of(() -> BasicExpressions.of(value)));
        }

        default NumberExpression<T, U> divide(U value) {
            return operateAsNumber(this, Operator.DIVIDE, List.of(() -> BasicExpressions.of(value)));
        }

        default NumberExpression<T, U> mod(U value) {
            return operateAsNumber(this, Operator.MOD, List.of(() -> BasicExpressions.of(value)));
        }

        default NumberExpression<T, U> add(NumberExpression<T, U> value) {
            return operateAsNumber(this, Operator.ADD, List.of(BasicExpressions.of(value)));
        }

        default NumberExpression<T, U> subtract(NumberExpression<T, U> value) {
            return operateAsNumber(this, Operator.SUBTRACT, List.of(BasicExpressions.of(value)));
        }

        default NumberExpression<T, U> multiply(NumberExpression<T, U> value) {
            return operateAsNumber(this, Operator.MULTIPLY, List.of(BasicExpressions.of(value)));
        }

        default NumberExpression<T, U> divide(NumberExpression<T, U> value) {
            return operateAsNumber(this, Operator.DIVIDE, List.of(BasicExpressions.of(value)));
        }

        default NumberExpression<T, U> mod(NumberExpression<T, U> value) {
            return operateAsNumber(this, Operator.MOD, List.of(BasicExpressions.of(value)));
        }

        default <V extends Number & Comparable<V>> NumberExpression<T, V> sum() {
            return operateAsNumber(this, Operator.SUM, List.of());
        }

        default <V extends Number & Comparable<V>> NumberExpression<T, V> avg() {
            return operateAsNumber(this, Operator.AVG, List.of());
        }

        default <V extends Number & Comparable<V>> NumberExpression<T, V> max() {
            return operateAsNumber(this, Operator.MAX, List.of());
        }

        default <V extends Number & Comparable<V>> NumberExpression<T, V> min() {
            return operateAsNumber(this, Operator.MIN, List.of());
        }


    }

    interface StringExpression<T> extends ComparableExpression<T, String> {

        default BooleanExpression<T> like(String value) {
            return operateAsPredicate(this, Operator.LIKE, List.of(() -> BasicExpressions.of(value)));
        }

        default BooleanExpression<T> startWith(String value) {
            return like(value + "%");
        }

        default BooleanExpression<T> endsWith(String value) {
            return like("%" + value);
        }

        default BooleanExpression<T> contains(String value) {
            return like("%" + value + "%");
        }

        default StringExpression<T> lower() {
            return operateAsString(this, Operator.LOWER, List.of());
        }

        default StringExpression<T> upper() {
            return operateAsString(this, Operator.UPPER, List.of());
        }

        default StringExpression<T> substring(int a, int b) {
            return operateAsString(this, Operator.SUBSTRING,
                    List.of(() -> BasicExpressions.of(a), () -> BasicExpressions.of(b)));
        }

        default StringExpression<T> substring(int a) {
            return operateAsString(this, Operator.SUBSTRING,
                    List.of(() -> BasicExpressions.of(a)));
        }

        default StringExpression<T> trim() {
            return operateAsString(this, Operator.TRIM, List.of());
        }

        default NumberExpression<T, Integer> length() {
            return operateAsNumber(this, Operator.LENGTH, List.of());
        }

    }

}
