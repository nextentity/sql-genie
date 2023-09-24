package io.github.genie.sql.core;

import io.github.genie.sql.core.Path.ComparablePath;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static io.github.genie.sql.core.BasicExpressions.*;

public class ChainApi {

    @SuppressWarnings("unused")
    public interface OperateableConnector<T, U> extends Expression.TypedExpression<T, U> {

        default BooleanConnector<T> eq(U value) {
            return operateAsPredicate(this, Operator.EQ, List.of(() -> BasicExpressions.of(value)));
        }

        default BooleanConnector<T> eq(TypedExpression<T, U> expression) {
            return operateAsPredicate(this, Operator.EQ, List.of(expression));
        }

        default BooleanConnector<T> ne(U value) {
            return operateAsPredicate(this, Operator.NE, List.of(() -> BasicExpressions.of(value)));
        }

        default BooleanConnector<T> ne(TypedExpression<T, U> expression) {
            return operateAsPredicate(this, Operator.NE, List.of(expression));
        }

        @SuppressWarnings({"unchecked"})
        default BooleanConnector<T> in(U... values) {
            List<Expression> list = Arrays.stream(values)
                    .map(BasicExpressions::of)
                    .<Expression>map(it -> () -> it)
                    .toList();
            return operateAsPredicate(this, Operator.IN, list);
        }

        default BooleanConnector<T> in(@NotNull List<? extends TypedExpression<T, U>> values) {
            return operateAsPredicate(this, Operator.IN, values);
        }

        default BooleanConnector<T> isNull() {
            return operateAsPredicate(this, Operator.IS_NULL, Collections.emptyList());
        }

        default NumberConnector<T, Integer> count() {
            return operateAsNumber(this, Operator.COUNT, List.of());
        }

        default Ordering<T> asc() {
            return new OrderingImpl<>(this, Ordering.SortOrder.ASC);
        }

        default Ordering<T> desc() {
            return new OrderingImpl<>(this, Ordering.SortOrder.DESC);
        }

        default BooleanConnector<T> isNotNull() {
            return operateAsPredicate(this, Operator.IS_NOT_NULL, Collections.emptyList());
        }

    }

    interface BooleanConnector<T> extends OperateableConnector<T, Boolean>, Predicate<T> {
        default BooleanConnector<T> and(TypedExpression<T, Boolean> value) {
            return operateAsPredicate(this, Operator.AND, List.of(value));
        }

        default BooleanConnector<T> or(TypedExpression<T, Boolean> value) {
            return operateAsPredicate(this, Operator.OR, List.of(value));
        }

        @SuppressWarnings("unchecked")
        default BooleanConnector<T> and(TypedExpression<T, Boolean>... values) {
            return operateAsPredicate(this, Operator.AND, Arrays.asList(values));
        }

        @SuppressWarnings("unchecked")
        default BooleanConnector<T> or(TypedExpression<T, Boolean>... values) {
            return operateAsPredicate(this, Operator.OR, Arrays.asList(values));
        }

        default BooleanConnector<T> and(List<TypedExpression<T, Boolean>> values) {
            return operateAsPredicate(this, Operator.AND, values);
        }

        default BooleanConnector<T> or(List<TypedExpression<T, Boolean>> values) {
            return operateAsPredicate(this, Operator.OR, values);
        }

        default <R, S extends PathVisitor<T, R> & OperateableConnector<T, R>> S and(Path<T, R> path) {
            return Util.cast(ExpressionBuilders.and(this, path));
        }

        default <R, S extends PathVisitor<T, R> & OperateableConnector<T, R>> S or(Path<T, R> path) {
            return Util.cast(ExpressionBuilders.or(this, path));
        }

        default <R extends Comparable<R>, S extends PathVisitor<T, R> & ComparableConnector<T, R>>
        S and(ComparablePath<T, R> path) {
            return Util.cast(ExpressionBuilders.and(this, path));
        }

        default <R extends Comparable<R>, S extends PathVisitor<T, R> & ComparableConnector<T, R>>
        S or(ComparablePath<T, R> path) {
            return Util.cast(ExpressionBuilders.or(this, path));
        }

        default <R extends Number & Comparable<R>, S extends PathVisitor<T, R> & NumberConnector<T, R>>
        S and(Path.NumberPath<T, R> path) {
            return Util.cast(ExpressionBuilders.and(this, path));
        }

        default <R extends Number & Comparable<R>, S extends PathVisitor<T, R> & NumberConnector<T, R>>
        S or(Path.NumberPath<T, R> path) {
            return Util.cast(ExpressionBuilders.or(this, path));
        }

    }

    interface ComparableConnector<T, U extends Comparable<U>> extends OperateableConnector<T, U> {

        default BooleanConnector<T> ge(U value) {
            return operateAsPredicate(this, Operator.GE, List.of(() -> BasicExpressions.of(value)));
        }

        default BooleanConnector<T> gt(U value) {
            return operateAsPredicate(this, Operator.GT, List.of(() -> BasicExpressions.of(value)));
        }

        default BooleanConnector<T> le(U value) {
            return operateAsPredicate(this, Operator.LE, List.of(() -> BasicExpressions.of(value)));
        }

        default BooleanConnector<T> lt(U value) {
            return operateAsPredicate(this, Operator.LT, List.of(() -> BasicExpressions.of(value)));
        }

        default BooleanConnector<T> between(U l, U r) {
            return operateAsPredicate(this, Operator.BETWEEN,
                    List.of(() -> BasicExpressions.of(l), () -> BasicExpressions.of(r)));
        }

        default BooleanConnector<T> ge(ComparableConnector<T, U> value) {
            return operateAsPredicate(this, Operator.GE, List.of(value));
        }

        default BooleanConnector<T> gt(ComparableConnector<T, U> value) {
            return operateAsPredicate(this, Operator.GT, List.of(BasicExpressions.of(value)));
        }

        default BooleanConnector<T> le(ComparableConnector<T, U> value) {
            return operateAsPredicate(this, Operator.LE, List.of(BasicExpressions.of(value)));
        }

        default BooleanConnector<T> lt(ComparableConnector<T, U> value) {
            return operateAsPredicate(this, Operator.LT, List.of(BasicExpressions.of(value)));
        }

        default BooleanConnector<T> between(ComparableConnector<T, U> l, ComparableConnector<T, U> r) {
            return operateAsPredicate(this, Operator.BETWEEN, List.of(BasicExpressions.of(l), BasicExpressions.of(r)));
        }

        default BooleanConnector<T> between(ComparableConnector<T, U> l, U r) {
            return operateAsPredicate(this, Operator.BETWEEN,
                    List.of(l, () -> BasicExpressions.of(r)));
        }

        default BooleanConnector<T> between(U l, ComparableConnector<T, U> r) {
            return operateAsPredicate(this, Operator.BETWEEN, List.of(() -> BasicExpressions.of(l), r));
        }


    }

    interface NumberConnector<T, U extends Number & Comparable<U>> extends ComparableConnector<T, U> {

        default NumberConnector<T, U> add(U value) {
            return operateAsNumber(this, Operator.ADD, List.<Expression>of(() -> BasicExpressions.of(value)));
        }

        default NumberConnector<T, U> subtract(U value) {
            return operateAsNumber(this, Operator.SUBTRACT, List.of(() -> BasicExpressions.of(value)));
        }

        default NumberConnector<T, U> multiply(U value) {
            return operateAsNumber(this, Operator.MULTIPLY, List.of(() -> BasicExpressions.of(value)));
        }

        default NumberConnector<T, U> divide(U value) {
            return operateAsNumber(this, Operator.DIVIDE, List.of(() -> BasicExpressions.of(value)));
        }

        default NumberConnector<T, U> mod(U value) {
            return operateAsNumber(this, Operator.MOD, List.of(() -> BasicExpressions.of(value)));
        }

        default NumberConnector<T, U> add(NumberConnector<T, U> value) {
            return operateAsNumber(this, Operator.ADD, List.of(BasicExpressions.of(value)));
        }

        default NumberConnector<T, U> subtract(NumberConnector<T, U> value) {
            return operateAsNumber(this, Operator.SUBTRACT, List.of(BasicExpressions.of(value)));
        }

        default NumberConnector<T, U> multiply(NumberConnector<T, U> value) {
            return operateAsNumber(this, Operator.MULTIPLY, List.of(BasicExpressions.of(value)));
        }

        default NumberConnector<T, U> divide(NumberConnector<T, U> value) {
            return operateAsNumber(this, Operator.DIVIDE, List.of(BasicExpressions.of(value)));
        }

        default NumberConnector<T, U> mod(NumberConnector<T, U> value) {
            return operateAsNumber(this, Operator.MOD, List.of(BasicExpressions.of(value)));
        }

        default <V extends Number & Comparable<V>> NumberConnector<T, V> sum() {
            return operateAsNumber(this, Operator.SUM, List.of());
        }

        default <V extends Number & Comparable<V>> NumberConnector<T, V> avg() {
            return operateAsNumber(this, Operator.AVG, List.of());
        }

        default <V extends Number & Comparable<V>> NumberConnector<T, V> max() {
            return operateAsNumber(this, Operator.MAX, List.of());
        }

        default <V extends Number & Comparable<V>> NumberConnector<T, V> min() {
            return operateAsNumber(this, Operator.MIN, List.of());
        }


    }

    interface StringConnector<T> extends ComparableConnector<T, String> {

        default BooleanConnector<T> like(String value) {
            return operateAsPredicate(this, Operator.LIKE, List.of(() -> BasicExpressions.of(value)));
        }

        default BooleanConnector<T> startWith(String value) {
            return like(value + "%");
        }

        default BooleanConnector<T> endsWith(String value) {
            return like("%" + value);
        }

        default BooleanConnector<T> contains(String value) {
            return like("%" + value + "%");
        }

        default StringConnector<T> lower() {
            return operateAsString(this, Operator.LOWER, List.of());
        }

        default StringConnector<T> upper() {
            return operateAsString(this, Operator.UPPER, List.of());
        }

        default StringConnector<T> substring(int a, int b) {
            return operateAsString(this, Operator.SUBSTRING,
                    List.of(() -> BasicExpressions.of(a), () -> BasicExpressions.of(b)));
        }

        default StringConnector<T> substring(int a) {
            return operateAsString(this, Operator.SUBSTRING,
                    List.of(() -> BasicExpressions.of(a)));
        }

        default StringConnector<T> trim() {
            return operateAsString(this, Operator.TRIM, List.of());
        }

        default NumberConnector<T, Integer> length() {
            return operateAsNumber(this, Operator.LENGTH, List.of());
        }

    }

}
