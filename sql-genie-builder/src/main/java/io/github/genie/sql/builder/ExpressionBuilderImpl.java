package io.github.genie.sql.builder;

import io.github.genie.sql.api.Column;
import io.github.genie.sql.api.Expression;
import io.github.genie.sql.api.ExpressionBuilder;
import io.github.genie.sql.api.ExpressionHolder;
import io.github.genie.sql.api.Lists;
import io.github.genie.sql.api.Operation;
import io.github.genie.sql.api.Operator;
import io.github.genie.sql.api.Order;
import io.github.genie.sql.api.Order.SortOrder;
import io.github.genie.sql.api.Path;
import io.github.genie.sql.api.Path.BooleanPath;
import io.github.genie.sql.api.Path.ComparablePath;
import io.github.genie.sql.api.Path.NumberPath;
import io.github.genie.sql.api.Path.StringPath;
import io.github.genie.sql.builder.QueryStructures.OrderImpl;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class ExpressionBuilderImpl<T> implements ExpressionBuilder<T> {
    @Override
    public <U> OperablePath<T, U> get(Path<T, U> path) {
        return new OperablePathImpl<>((Operation) null, Expressions.of(path));
    }

    @Override
    public OperableString<T> get(StringPath<T> path) {
        return new OperableStringImpl<>((Operation) null, Expressions.of(path));
    }

    @Override
    public <U extends Number & Comparable<U>> OperableNumber<T, U> get(NumberPath<T, U> path) {
        return new OperableNumberImpl<>((Operation) null, Expressions.of(path));
    }

    @Override
    public <U extends Comparable<U>> OperableComparable<T, U> get(ComparablePath<T, U> path) {
        return new OperableComparableImpl<>((Operation) null, Expressions.of(path));
    }

    @Override
    public OperableBoolean<T> get(BooleanPath<T> path) {
        return new OperableBooleanImpl<>((Operation) null, Expressions.of(path));
    }

    @Override
    public <U extends Number & Comparable<U>> OperableNumber<T, U> min(NumberPath<T, U> path) {
        return get(path).min();
    }

    @Override
    public <U extends Number & Comparable<U>> OperableNumber<T, U> max(NumberPath<T, U> path) {
        return get(path).max();
    }

    @Override
    public <U extends Number & Comparable<U>> OperableNumber<T, U> sum(NumberPath<T, U> path) {
        return get(path).sum();
    }

    @Override
    public <U extends Number & Comparable<U>> OperableNumber<T, U> avg(NumberPath<T, U> path) {
        return get(path).avg();
    }

    @Override
    public OperableNumber<T, Integer> count(Path<T, ?> path) {
        return get(path).count();
    }

    private static class OperableExpressionImpl<T, U> implements OperableExpression<T, U> {
        protected final Operation operation;
        protected final Expression operand;

        public OperableExpressionImpl(Operation operation, Expression operand) {
            this.operation = operation;
            this.operand = operand;
        }

        public OperableExpressionImpl(OperableExpressionImpl<?, ?> origin, Expression operand) {
            this(operand == null ? null : origin.operation, operand);
        }

        protected Expression operate(Operator operator, Object value) {
            return operate(operator, ExpressionHolders.of(value));
        }

        protected Expression operate(Operator operator, ExpressionHolder<T, ?> expression) {
            return operate(operator, Lists.of(expression));
        }

        protected Expression operate(Operator operator, Iterable<? extends ExpressionHolder<T, ?>> expressions) {
            List<Expression> args = StreamSupport.stream(expressions.spliterator(), false)
                    .map(ExpressionHolder::expression)
                    .collect(Collectors.toList());
            return Expressions.operate(this.operand, operator, args);
        }

        @Override
        public OperableBoolean<T> eq(U value) {
            return eq(ExpressionHolders.of(value));
        }

        @Override
        public OperableBoolean<T> eq(ExpressionHolder<T, U> value) {
            Expression operate = operate(Operator.EQ, value);
            return new OperableBooleanImpl<>(this, operate);
        }

        @Override
        public OperableBoolean<T> ne(U value) {
            return ne(ExpressionHolders.of(value));
        }

        @Override
        public OperableBoolean<T> ne(ExpressionHolder<T, U> value) {
            Expression operate = operate(Operator.NE, value);
            return new OperableBooleanImpl<>(this, operate);
        }

        @SafeVarargs
        @Override
        public final OperableBoolean<T> in(U... values) {
            return in(ExpressionHolders.of(values));
        }

        @Override
        public OperableBoolean<T> in(@NotNull List<? extends ExpressionHolder<T, U>> values) {
            Expression operate = operate(Operator.IN, values);
            return new OperableBooleanImpl<>(this, operate);
        }

        @Override
        public OperableBoolean<T> in(@NotNull Collection<? extends U> values) {
            return in(ExpressionHolders.of(values));
        }

        @SafeVarargs
        @Override
        public final OperableBoolean<T> notIn(U... values) {
            return notIn(ExpressionHolders.of(values));
        }

        @Override
        public OperableBoolean<T> notIn(@NotNull List<? extends ExpressionHolder<T, U>> values) {
            List<Expression> expressions = values.stream()
                    .map(ExpressionHolder::expression)
                    .collect(Collectors.toList());
            Expression operate = Expressions.operate(operand, Operator.IN, expressions);
            operate = Expressions.operate(operate, Operator.NOT);
            return new OperableBooleanImpl<>(this, operate);
        }

        @Override
        public OperableBoolean<T> notIn(@NotNull Collection<? extends U> values) {
            return notIn(ExpressionHolders.of(values));
        }

        @Override
        public OperableBoolean<T> isNull() {
            Expression operate = Expressions.operate(operand, Operator.IS_NULL);
            return new OperableBooleanImpl<>(this, operate);
        }

        @Override
        public OperableNumber<T, Integer> count() {
            Expression operate = Expressions.operate(operand, Operator.COUNT);
            return new OperableNumberImpl<>(this, operate);
        }

        @Override
        public OperableBoolean<T> isNotNull() {
            Expression operate = Expressions.operate(operand, Operator.IS_NOT_NULL);
            return new OperableBooleanImpl<>(this, operate);
        }

        @Override
        public Expression expression() {
            if (operation != null) {
                Operator operator = operation.operator();
                if (operator == Operator.OR || operator == Operator.AND) {
                    return Expressions.operate(operation, operator, operand);
                } else {
                    throw new IllegalStateException();
                }
            } else {
                return operand;
            }
        }

        protected Operation and() {
            return updateOperation(Operator.AND);
        }

        protected Operation or() {
            return updateOperation(Operator.OR);
        }

        protected Operation updateOperation(Operator operator) {
            if (operation == null) {
                return (Operation) Expressions.operate(operand, operator);
            }
            if (operation.operator() != operator) {
                throw new IllegalStateException();
            }
            return (Operation) Expressions.operate(operation, operator, operand);
        }


    }

    private static class OperableComparableImpl<T, U extends Comparable<U>>
            extends OperableExpressionImpl<T, U>
            implements OperableComparable<T, U> {

        public OperableComparableImpl(OperableExpressionImpl<?, ?> origin, Expression operand) {
            super(origin, operand);
        }

        public OperableComparableImpl(Operation operation, Expression operand) {
            super(operation, operand);
        }

        @Override
        public OperableBoolean<T> ge(U value) {
            return ge(ExpressionHolders.of(value));
        }

        @Override
        public OperableBoolean<T> gt(U value) {
            return gt(ExpressionHolders.of(value));
        }

        @Override
        public OperableBoolean<T> le(U value) {
            return le(ExpressionHolders.of(value));
        }

        @Override
        public OperableBoolean<T> lt(U value) {
            return lt(ExpressionHolders.of(value));
        }

        @Override
        public OperableBoolean<T> ge(ExpressionHolder<T, U> expression) {
            return new OperableBooleanImpl<>(this, operate(Operator.GE, expression));
        }

        @Override
        public OperableBoolean<T> gt(ExpressionHolder<T, U> expression) {
            return new OperableBooleanImpl<>(this, operate(Operator.GT, expression));
        }

        @Override
        public OperableBoolean<T> le(ExpressionHolder<T, U> expression) {
            return new OperableBooleanImpl<>(this, operate(Operator.LE, expression));
        }

        @Override
        public OperableBoolean<T> lt(ExpressionHolder<T, U> expression) {
            return new OperableBooleanImpl<>(this, operate(Operator.LT, expression));
        }

        @Override
        public OperableBoolean<T> between(U l, U r) {
            return between(ExpressionHolders.of(l), ExpressionHolders.of(r));
        }

        @Override
        public OperableBoolean<T> notBetween(U l, U r) {
            return notBetween(ExpressionHolders.of(l), ExpressionHolders.of(r));
        }

        @Override
        public OperableBoolean<T> between(ExpressionHolder<T, U> l, ExpressionHolder<T, U> r) {
            return new OperableBooleanImpl<>(this, operate(Operator.BETWEEN, List.of(l, r)));
        }

        @Override
        public OperableBoolean<T> between(ExpressionHolder<T, U> l, U r) {
            return between(l, ExpressionHolders.of(r));
        }

        @Override
        public OperableBoolean<T> between(U l, ExpressionHolder<T, U> r) {
            return between(ExpressionHolders.of(l), r);
        }

        @Override
        public OperableBoolean<T> notBetween(ExpressionHolder<T, U> l, ExpressionHolder<T, U> r) {
            Expression operate = operate(Operator.BETWEEN, List.of(l, ExpressionHolders.of(r)));
            operate = Expressions.operate(operate, Operator.NOT);
            return new OperableBooleanImpl<>(this, operate);
        }

        @Override
        public OperableBoolean<T> notBetween(ExpressionHolder<T, U> l, U r) {
            return notBetween(l, ExpressionHolders.of(r));
        }

        @Override
        public OperableBoolean<T> notBetween(U l, ExpressionHolder<T, U> r) {
            return notBetween(ExpressionHolders.of(l), r);
        }

        @Override
        public Order<T> asc() {
            return new OrderImpl<>(operand, SortOrder.ASC);
        }

        @Override
        public Order<T> desc() {
            return new OrderImpl<>(operand, SortOrder.DESC);
        }
    }

    private static class OperableBooleanImpl<T>
            extends OperableComparableImpl<T, Boolean>
            implements OperableBoolean<T> {

        public OperableBooleanImpl(OperableExpressionImpl<?, ?> origin, Expression operand) {
            super(origin, operand);
        }

        public OperableBooleanImpl(Operation operation, Expression operand) {
            super(operation, operand);
        }

        @Override
        public OperableBoolean<T> not() {
            return new OperableBooleanImpl<>(this, Expressions.operate(operand, Operator.NOT));
        }

        @Override
        public <R> OperablePath<T, R> or(Path<T, R> path) {
            return new OperablePathImpl<>(or(), Expressions.of(path));
        }

        @Override
        public <R extends Comparable<R>> OperableComparable<T, R> or(ComparablePath<T, R> path) {
            return new OperableComparableImpl<>(or(), Expressions.of(path));
        }

        @Override
        public <R extends Number & Comparable<R>> OperableNumber<T, R> or(NumberPath<T, R> path) {
            return new OperableNumberImpl<>(or(), Expressions.of(path));
        }

        @Override
        public OperableOr<T> or(BooleanPath<T> path) {
            return new OperableBooleanImpl<>(or(), Expressions.of(path));
        }

        @Override
        public OperableString<T> or(StringPath<T> path) {
            return new OperableStringImpl<>(or(), Expressions.of(path));
        }

        @Override
        public OperableOr<T> or(ExpressionHolder<T, Boolean> expression) {
            return new OperableBooleanImpl<>(or(), expression.expression());
        }

        @Override
        public OperableOr<T> or(List<ExpressionHolder<T, Boolean>> expressions) {
            if (expressions.isEmpty()) {
                return this;
            }
            List<Expression> sub = expressions
                    .subList(0, expressions.size() - 1)
                    .stream().map(ExpressionHolder::expression)
                    .collect(Collectors.toList());
            Operation operation = (Operation) Expressions.operate(or(), Operator.OR, sub);
            Expression last = expressions.get(expressions.size() - 1).expression();
            return new OperableBooleanImpl<>(operation, last);
        }

        @Override
        public <R> OperablePath<T, R> and(Path<T, R> path) {
            return new OperablePathImpl<>(and(), Expressions.of(path));
        }

        @Override
        public <R extends Comparable<R>> OperableComparable<T, R> and(ComparablePath<T, R> path) {
            return new OperableComparableImpl<>(and(), Expressions.of(path));
        }

        @Override
        public <R extends Number & Comparable<R>> OperableNumber<T, R> and(NumberPath<T, R> path) {
            return new OperableNumberImpl<>(and(), Expressions.of(path));
        }

        @Override
        public OperableAnd<T> and(BooleanPath<T> path) {
            return new OperableBooleanImpl<>(and(), Expressions.of(path));
        }

        @Override
        public OperableString<T> and(StringPath<T> path) {
            return new OperableStringImpl<>(and(), Expressions.of(path));
        }

        @Override
        public OperableAnd<T> and(ExpressionHolder<T, Boolean> expression) {
            return new OperableBooleanImpl<>(and(), expression.expression());
        }

        @Override
        public OperableAnd<T> and(List<ExpressionHolder<T, Boolean>> expressions) {
            if (expressions.isEmpty()) {
                return this;
            }
            List<Expression> sub = expressions
                    .subList(0, expressions.size() - 1)
                    .stream().map(ExpressionHolder::expression)
                    .collect(Collectors.toList());
            Operation operation = (Operation) Expressions.operate(and(), Operator.AND, sub);
            Expression last = expressions.get(expressions.size() - 1).expression();
            return new OperableBooleanImpl<>(operation, last);
        }
    }

    private static class OperablePathImpl<T, U>
            extends OperableExpressionImpl<T, U>
            implements OperablePath<T, U> {

        public OperablePathImpl(OperableExpressionImpl<?, ?> origin, Expression operand) {
            super(origin, operand);
        }

        public OperablePathImpl(Operation operation, Expression operand) {
            super(operation, operand);
        }

        private <R> Column getPath(Path<U, R> path) {
            if (operation instanceof Column) {
                return Expressions.concat((Column) operand, path);
            }
            throw new IllegalStateException();
        }

        @Override
        public <R> OperablePath<T, R> get(Path<U, R> path) {
            return new OperablePathImpl<>(this, getPath(path));
        }

        @Override
        public OperableString<T> get(StringPath<U> path) {
            return new OperableStringImpl<>(this, getPath(path));
        }

        @Override
        public <R extends Number & Comparable<R>> OperableNumber<T, R> get(NumberPath<U, R> path) {
            return new OperableNumberImpl<>(this, getPath(path));
        }

        @Override
        public <R extends Comparable<R>> OperableComparable<T, R> get(ComparablePath<U, R> path) {
            return new OperableComparableImpl<>(this, getPath(path));
        }

        @Override
        public OperableBoolean<T> get(BooleanPath<U> path) {
            return new OperableBooleanImpl<>(this, getPath(path));
        }
    }


    private static class OperableStringImpl<T>
            extends OperableComparableImpl<T, String>
            implements OperableString<T> {

        public OperableStringImpl(OperableExpressionImpl<?, ?> origin, Expression operand) {
            super(origin, operand);
        }

        public OperableStringImpl(Operation operation, Expression operand) {
            super(operation, operand);
        }

        @Override
        public OperableBoolean<T> like(String value) {
            return new OperableBooleanImpl<>(this, operate(Operator.LIKE, value));
        }

        @Override
        public OperableBoolean<T> notLike(String value) {
            Expression operate = operate(Operator.LIKE, value);
            operate = Expressions.operate(operate, Operator.NOT);
            return new OperableBooleanImpl<>(this, operate);
        }

        @Override
        public OperableString<T> lower() {
            return new OperableStringImpl<>(this, operate(Operator.LOWER, Lists.of()));
        }

        @Override
        public OperableString<T> upper() {
            return new OperableStringImpl<>(this, operate(Operator.UPPER, Lists.of()));
        }

        @Override
        public OperableString<T> substring(int a, int b) {
            List<ExpressionHolder<T, ?>> expressions =
                    Lists.of(ExpressionHolders.of(a), ExpressionHolders.of(b));
            return new OperableStringImpl<>(this, operate(Operator.SUBSTRING, expressions));
        }

        @Override
        public OperableString<T> substring(int a) {
            return new OperableStringImpl<>(this, operate(Operator.SUBSTRING, a));
        }

        @Override
        public OperableString<T> trim() {
            return new OperableStringImpl<>(this, operate(Operator.TRIM, Lists.of()));
        }

        @Override
        public OperableNumber<T, Integer> length() {
            return new OperableNumberImpl<>(this, operate(Operator.LENGTH, Lists.of()));
        }
    }

    private static class OperableNumberImpl<T, U extends Number & Comparable<U>>
            extends OperableComparableImpl<T, U>
            implements OperableNumber<T, U> {

        public OperableNumberImpl(OperableExpressionImpl<?, ?> origin, Expression operand) {
            super(origin, operand);
        }

        public OperableNumberImpl(Operation operation, Expression operand) {
            super(operation, operand);
        }

        @Override
        public OperableNumber<T, U> add(U value) {
            return add(ExpressionHolders.of(value));
        }

        @Override
        public OperableNumber<T, U> subtract(U value) {
            return subtract(ExpressionHolders.of(value));
        }

        @Override
        public OperableNumber<T, U> multiply(U value) {
            return multiply(ExpressionHolders.of(value));
        }

        @Override
        public OperableNumber<T, U> divide(U value) {
            return divide(ExpressionHolders.of(value));
        }

        @Override
        public OperableNumber<T, U> mod(U value) {
            return mod(ExpressionHolders.of(value));
        }

        @Override
        public OperableNumber<T, U> add(ExpressionHolder<T, U> value) {
            return new OperableNumberImpl<>(this, operate(Operator.ADD, value));
        }

        @Override
        public OperableNumber<T, U> subtract(ExpressionHolder<T, U> value) {
            return new OperableNumberImpl<>(this, operate(Operator.SUBTRACT, value));
        }

        @Override
        public OperableNumber<T, U> multiply(ExpressionHolder<T, U> value) {
            return new OperableNumberImpl<>(this, operate(Operator.MULTIPLY, value));
        }

        @Override
        public OperableNumber<T, U> divide(ExpressionHolder<T, U> value) {
            return new OperableNumberImpl<>(this, operate(Operator.DIVIDE, value));
        }

        @Override
        public OperableNumber<T, U> mod(ExpressionHolder<T, U> value) {
            return new OperableNumberImpl<>(this, operate(Operator.MOD, value));
        }

        @Override
        public <R extends Number & Comparable<R>> OperableNumber<T, R> sum() {
            return new OperableNumberImpl<>(this, operate(Operator.SUM, Lists.of()));
        }

        @Override
        public <R extends Number & Comparable<R>> OperableNumber<T, R> avg() {
            return new OperableNumberImpl<>(this, operate(Operator.AVG, Lists.of()));
        }

        @Override
        public <R extends Number & Comparable<R>> OperableNumber<T, R> max() {
            return new OperableNumberImpl<>(this, operate(Operator.MAX, Lists.of()));
        }

        @Override
        public <R extends Number & Comparable<R>> OperableNumber<T, R> min() {
            return new OperableNumberImpl<>(this, operate(Operator.MIN, Lists.of()));
        }
    }
}
