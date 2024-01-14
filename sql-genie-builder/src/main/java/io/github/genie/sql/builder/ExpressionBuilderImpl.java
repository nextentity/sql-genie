package io.github.genie.sql.builder;

import io.github.genie.sql.api.EntityRoot;
import io.github.genie.sql.api.Expression;
import io.github.genie.sql.api.Column;
import io.github.genie.sql.api.Operation;
import io.github.genie.sql.api.ExpressionOperator.ComparableOperator;
import io.github.genie.sql.api.ExpressionOperator.NumberOperator;
import io.github.genie.sql.api.ExpressionOperator.PathOperator;
import io.github.genie.sql.api.ExpressionOperator.StringOperator;
import io.github.genie.sql.api.ExpressionHolder;
import io.github.genie.sql.api.Lists;
import io.github.genie.sql.api.Operator;
import io.github.genie.sql.api.Order;
import io.github.genie.sql.api.Order.SortOrder;
import io.github.genie.sql.api.Path;
import io.github.genie.sql.api.Path.BooleanPath;
import io.github.genie.sql.api.Path.ComparablePath;
import io.github.genie.sql.api.Path.NumberPath;
import io.github.genie.sql.api.Path.StringPath;
import io.github.genie.sql.api.TypedExpression;
import io.github.genie.sql.api.TypedExpression.BooleanExpression;
import io.github.genie.sql.api.TypedExpression.ComparableExpression;
import io.github.genie.sql.api.TypedExpression.NumberExpression;
import io.github.genie.sql.api.TypedExpression.PathExpression;
import io.github.genie.sql.api.TypedExpression.Predicate;
import io.github.genie.sql.api.TypedExpression.StringExpression;
import io.github.genie.sql.builder.DefaultExpressionOperator.ComparableOperatorImpl;
import io.github.genie.sql.builder.DefaultExpressionOperator.NumberOperatorImpl;
import io.github.genie.sql.builder.DefaultExpressionOperator.PathOperatorImpl;
import io.github.genie.sql.builder.DefaultExpressionOperator.StringOperatorImpl;
import io.github.genie.sql.builder.QueryStructures.OrderImpl;
import lombok.Data;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class ExpressionBuilderImpl<T> implements EntityRoot<T> {

    private static final ExpressionBuilderImpl<?> INSTANCE = new ExpressionBuilderImpl<>();

    public static <T> EntityRoot<T> of() {
        return TypeCastUtil.cast(INSTANCE);
    }

    protected ExpressionBuilderImpl() {
    }

    @Override
    public <U> ExpressionHolder<T, U> of(U value) {
        return ExpressionHolders.of(value);
    }

    @Override
    public <U> PathExpression<T, U> get(Path<T, U> path) {
        return new OperablePathImpl<>((Operation) null, Expressions.of(path));
    }

    @Override
    public StringExpression<T> get(StringPath<T> path) {
        return new OperableStringImpl<>((Operation) null, Expressions.of(path));
    }

    @Override
    public <U extends Number & Comparable<U>> NumberExpression<T, U> get(NumberPath<T, U> path) {
        return new OperableNumberImpl<>((Operation) null, Expressions.of(path));
    }

    @Override
    public <U extends Comparable<U>> ComparableExpression<T, U> get(ComparablePath<T, U> path) {
        return new OperableComparableImpl<>((Operation) null, Expressions.of(path));
    }

    @Override
    public BooleanExpression<T> get(BooleanPath<T> path) {
        return new OperableBooleanImpl<>((Operation) null, Expressions.of(path));
    }

    static <T> BooleanExpression<T> ofBooleanExpression(Expression expression) {
        return new OperableBooleanImpl<>((Operation) null, expression);
    }

    private static class OperableExpressionImpl<T, U> implements TypedExpression<T, U> {
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
        public BooleanExpression<T> eq(U value) {
            return eq(ExpressionHolders.of(value));
        }

        @Override
        public BooleanExpression<T> eq(ExpressionHolder<T, U> value) {
            Expression operate = operate(Operator.EQ, value);
            return new OperableBooleanImpl<>(this, operate);
        }

        @Override
        public BooleanExpression<T> ne(U value) {
            return ne(ExpressionHolders.of(value));
        }

        @Override
        public BooleanExpression<T> ne(ExpressionHolder<T, U> value) {
            Expression operate = operate(Operator.NE, value);
            return new OperableBooleanImpl<>(this, operate);
        }

        @SafeVarargs
        @Override
        public final BooleanExpression<T> in(U... values) {
            return in(ExpressionHolders.of(values));
        }

        @Override
        public BooleanExpression<T> in(@NotNull List<? extends ExpressionHolder<T, U>> values) {
            Expression operate = operate(Operator.IN, values);
            return new OperableBooleanImpl<>(this, operate);
        }

        @Override
        public BooleanExpression<T> in(@NotNull Collection<? extends U> values) {
            return in(ExpressionHolders.of(values));
        }

        @SafeVarargs
        @Override
        public final BooleanExpression<T> notIn(U... values) {
            return notIn(ExpressionHolders.of(values));
        }

        @Override
        public BooleanExpression<T> notIn(@NotNull List<? extends ExpressionHolder<T, U>> values) {
            List<Expression> expressions = values.stream()
                    .map(ExpressionHolder::expression)
                    .collect(Collectors.toList());
            Expression operate = Expressions.operate(operand, Operator.IN, expressions);
            operate = Expressions.operate(operate, Operator.NOT);
            return new OperableBooleanImpl<>(this, operate);
        }

        @Override
        public BooleanExpression<T> notIn(@NotNull Collection<? extends U> values) {
            return notIn(ExpressionHolders.of(values));
        }

        @Override
        public BooleanExpression<T> isNull() {
            Expression operate = Expressions.operate(operand, Operator.IS_NULL);
            return new OperableBooleanImpl<>(this, operate);
        }

        @Override
        public NumberExpression<T, Integer> count() {
            Expression operate = Expressions.operate(operand, Operator.COUNT);
            return new OperableNumberImpl<>(this, operate);
        }

        @Override
        public BooleanExpression<T> isNotNull() {
            Expression operate = Expressions.operate(operand, Operator.IS_NOT_NULL);
            return new OperableBooleanImpl<>(this, operate);
        }

        @Override
        public EntityRoot<T> root() {
            return ExpressionBuilderImpl.of();
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
            implements ComparableExpression<T, U> {

        public OperableComparableImpl(OperableExpressionImpl<?, ?> origin, Expression operand) {
            super(origin, operand);
        }

        public OperableComparableImpl(Operation operation, Expression operand) {
            super(operation, operand);
        }

        @Override
        public BooleanExpression<T> ge(ExpressionHolder<T, U> expression) {
            return new OperableBooleanImpl<>(this, operate(Operator.GE, expression));
        }

        @Override
        public BooleanExpression<T> gt(ExpressionHolder<T, U> expression) {
            return new OperableBooleanImpl<>(this, operate(Operator.GT, expression));
        }

        @Override
        public BooleanExpression<T> le(ExpressionHolder<T, U> expression) {
            return new OperableBooleanImpl<>(this, operate(Operator.LE, expression));
        }

        @Override
        public BooleanExpression<T> lt(ExpressionHolder<T, U> expression) {
            return new OperableBooleanImpl<>(this, operate(Operator.LT, expression));
        }

        @Override
        public BooleanExpression<T> between(ExpressionHolder<T, U> l, ExpressionHolder<T, U> r) {
            return new OperableBooleanImpl<>(this, operate(Operator.BETWEEN, List.of(l, r)));
        }

        @Override
        public BooleanExpression<T> notBetween(ExpressionHolder<T, U> l, ExpressionHolder<T, U> r) {
            Expression operate = operate(Operator.BETWEEN, List.of(l, ExpressionHolders.of(r)));
            operate = Expressions.operate(operate, Operator.NOT);
            return new OperableBooleanImpl<>(this, operate);
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
            implements BooleanExpression<T> {

        public OperableBooleanImpl(OperableExpressionImpl<?, ?> origin, Expression operand) {
            super(origin, operand);
        }

        public OperableBooleanImpl(Operation operation, Expression operand) {
            super(operation, operand);
        }

        @Override
        public BooleanExpression<T> not() {
            return new OperableBooleanImpl<>(this, Expressions.operate(operand, Operator.NOT));
        }

        @Override
        public <R> PathOperator<T, R, OrOperator<T>> or(Path<T, R> path) {
            PathExpression<T, R> expression = new OperablePathImpl<>(or(), Expressions.of(path));
            return new PathOperatorImpl<>(expression, this::newOrOperator);
        }

        @NotNull
        OrOperator<T> newOrOperator(TypedExpression<?, ?> expression) {
            if (expression instanceof OrOperator) {
                return TypeCastUtil.unsafeCast(expression);
            }
            OperableExpressionImpl<?, ?> expr = (OperableExpressionImpl<?, ?>) expression;
            return new OperableBooleanImpl<>(expr.operation, expr.operand);
        }

        @NotNull
        AndOperator<T> newAndOperator(TypedExpression<?, ?> expression) {
            if (expression instanceof AndOperator) {
                return TypeCastUtil.unsafeCast(expression);
            }
            OperableExpressionImpl<?, ?> expr = (OperableExpressionImpl<?, ?>) expression;
            return new OperableBooleanImpl<>(expr.operation, expr.operand);
        }

        @Override
        public <R extends Comparable<R>> ComparableOperator<T, R, OrOperator<T>> or(ComparablePath<T, R> path) {
            ComparableExpression<T, R> expression = new OperableComparableImpl<>(or(), Expressions.of(path));
            return new ComparableOperatorImpl<>(expression, this::newOrOperator);
        }

        @Override
        public <R extends Number & Comparable<R>> NumberOperator<T, R, OrOperator<T>> or(NumberPath<T, R> path) {
            NumberExpression<T, R> expression = new OperableNumberImpl<>(or(), Expressions.of(path));
            return new NumberOperatorImpl<>(expression, this::newOrOperator);
        }

        @Override
        public OrOperator<T> or(BooleanPath<T> path) {
            return new OperableBooleanImpl<>(or(), Expressions.of(path));
        }

        @Override
        public StringOperator<T, ? extends OrOperator<T>> or(StringPath<T> path) {
            StringExpression<T> expression = new OperableStringImpl<>(or(), Expressions.of(path));
            return new StringOperatorImpl<>(expression, this::newOrOperator);
        }

        @Override
        public OrOperator<T> or(ExpressionHolder<T, Boolean> expression) {
            return new OperableBooleanImpl<>(or(), expression.expression());
        }

        @Override
        public OrOperator<T> or(List<? extends ExpressionHolder<T, Boolean>> expressions) {
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
        public <R> PathOperator<T, R, AndOperator<T>> and(Path<T, R> path) {
            PathExpression<T, R> expression = new OperablePathImpl<>(and(), Expressions.of(path));
            return new PathOperatorImpl<>(expression, this::newAndOperator);
        }

        @Override
        public <R extends Comparable<R>> ComparableOperator<T, R, AndOperator<T>> and(ComparablePath<T, R> path) {
            ComparableExpression<T, R> expression = new OperableComparableImpl<>(and(), Expressions.of(path));
            return new ComparableOperatorImpl<>(expression, this::newAndOperator);
        }

        @Override
        public <R extends Number & Comparable<R>> NumberOperator<T, R, AndOperator<T>> and(NumberPath<T, R> path) {
            NumberExpression<T, R> expression = new OperableNumberImpl<>(and(), Expressions.of(path));
            return new NumberOperatorImpl<>(expression, this::newAndOperator);
        }

        @Override
        public AndOperator<T> and(BooleanPath<T> path) {
            BooleanExpression<T> expression = new OperableBooleanImpl<>(and(), Expressions.of(path));
            return new OperableBooleanImpl<>(and(), expression.expression());
        }

        @Override
        public StringOperator<T, AndOperator<T>> and(StringPath<T> path) {
            StringExpression<T> expression = new OperableStringImpl<>(and(), Expressions.of(path));
            return new StringOperatorImpl<>(expression, this::newAndOperator);
        }

        @Override
        public AndOperator<T> and(ExpressionHolder<T, Boolean> expression) {
            return new OperableBooleanImpl<>(and(), expression.expression());
        }

        @Override
        public AndOperator<T> and(List<? extends ExpressionHolder<T, Boolean>> expressions) {
            BooleanExpression<T> expr = this;
            if (!expressions.isEmpty()) {
                List<Expression> sub = expressions
                        .subList(0, expressions.size() - 1)
                        .stream().map(ExpressionHolder::expression)
                        .collect(Collectors.toList());
                Operation operation = (Operation) Expressions.operate(and(), Operator.AND, sub);
                Expression last = expressions.get(expressions.size() - 1).expression();
                expr = new OperableBooleanImpl<>(operation, last);
            }
            return expr;
        }

        @Override
        public Predicate<T> then() {
            return new PredicateImpl<>(expression());
        }
    }

    @Data
    @Accessors(fluent = true)
    private static class PredicateImpl<T> implements Predicate<T> {
        private final Expression expression;

        private PredicateImpl(Expression expression) {
            this.expression = expression;
        }

        @Override
        public Predicate<T> not() {
            return new PredicateImpl<>(Expressions.operate(expression, Operator.NOT));
        }

        @Override
        public Expression expression() {
            return expression;
        }
    }

    private static class OperablePathImpl<T, U>
            extends OperableExpressionImpl<T, U>
            implements PathExpression<T, U> {

        public OperablePathImpl(OperableExpressionImpl<?, ?> origin, Expression operand) {
            super(origin, operand);
        }

        public OperablePathImpl(Operation operation, Expression operand) {
            super(operation, operand);
        }

        private <R> Column getPath(Path<U, R> path) {
            if (operand instanceof Column) {
                return Expressions.concat((Column) operand, path);
            }
            throw new IllegalStateException();
        }

        @Override
        public <R> PathExpression<T, R> get(Path<U, R> path) {
            return new OperablePathImpl<>(this, getPath(path));
        }

        @Override
        public StringExpression<T> get(StringPath<U> path) {
            return new OperableStringImpl<>(this, getPath(path));
        }

        @Override
        public <R extends Number & Comparable<R>> NumberExpression<T, R> get(NumberPath<U, R> path) {
            return new OperableNumberImpl<>(this, getPath(path));
        }

        @Override
        public <R extends Comparable<R>> ComparableExpression<T, R> get(ComparablePath<U, R> path) {
            return new OperableComparableImpl<>(this, getPath(path));
        }

        @Override
        public BooleanExpression<T> get(BooleanPath<U> path) {
            return new OperableBooleanImpl<>(this, getPath(path));
        }
    }


    private static class OperableStringImpl<T>
            extends OperableComparableImpl<T, String>
            implements StringExpression<T> {

        public OperableStringImpl(OperableExpressionImpl<?, ?> origin, Expression operand) {
            super(origin, operand);
        }

        public OperableStringImpl(Operation operation, Expression operand) {
            super(operation, operand);
        }

        @Override
        public BooleanExpression<T> like(String value) {
            return new OperableBooleanImpl<>(this, operate(Operator.LIKE, value));
        }

        @Override
        public BooleanExpression<T> notLike(String value) {
            Expression operate = operate(Operator.LIKE, value);
            operate = Expressions.operate(operate, Operator.NOT);
            return new OperableBooleanImpl<>(this, operate);
        }

        @Override
        public StringExpression<T> lower() {
            return new OperableStringImpl<>(this, operate(Operator.LOWER, Lists.of()));
        }

        @Override
        public StringExpression<T> upper() {
            return new OperableStringImpl<>(this, operate(Operator.UPPER, Lists.of()));
        }

        @Override
        public StringExpression<T> substring(int a, int b) {
            List<ExpressionHolder<T, ?>> expressions =
                    Lists.of(ExpressionHolders.of(a), ExpressionHolders.of(b));
            return new OperableStringImpl<>(this, operate(Operator.SUBSTRING, expressions));
        }

        @Override
        public StringExpression<T> substring(int a) {
            return new OperableStringImpl<>(this, operate(Operator.SUBSTRING, a));
        }

        @Override
        public StringExpression<T> trim() {
            return new OperableStringImpl<>(this, operate(Operator.TRIM, Lists.of()));
        }

        @Override
        public NumberExpression<T, Integer> length() {
            return new OperableNumberImpl<>(this, operate(Operator.LENGTH, Lists.of()));
        }
    }

    private static class OperableNumberImpl<T, U extends Number & Comparable<U>>
            extends OperableComparableImpl<T, U>
            implements NumberExpression<T, U> {

        public OperableNumberImpl(OperableExpressionImpl<?, ?> origin, Expression operand) {
            super(origin, operand);
        }

        public OperableNumberImpl(Operation operation, Expression operand) {
            super(operation, operand);
        }

        @Override
        public NumberExpression<T, U> add(ExpressionHolder<T, U> expression) {
            return new OperableNumberImpl<>(this, operate(Operator.ADD, expression));
        }

        @Override
        public NumberExpression<T, U> subtract(ExpressionHolder<T, U> expression) {
            return new OperableNumberImpl<>(this, operate(Operator.SUBTRACT, expression));
        }

        @Override
        public NumberExpression<T, U> multiply(ExpressionHolder<T, U> expression) {
            return new OperableNumberImpl<>(this, operate(Operator.MULTIPLY, expression));
        }

        @Override
        public NumberExpression<T, U> divide(ExpressionHolder<T, U> expression) {
            return new OperableNumberImpl<>(this, operate(Operator.DIVIDE, expression));
        }

        @Override
        public NumberExpression<T, U> mod(ExpressionHolder<T, U> expression) {
            return new OperableNumberImpl<>(this, operate(Operator.MOD, expression));
        }

        @Override
        public NumberExpression<T, U> sum() {
            return new OperableNumberImpl<>(this, operate(Operator.SUM, Lists.of()));
        }

        @Override
        public <R extends Number & Comparable<R>> NumberExpression<T, R> avg() {
            return new OperableNumberImpl<>(this, operate(Operator.AVG, Lists.of()));
        }

        @Override
        public NumberExpression<T, U> max() {
            return new OperableNumberImpl<>(this, operate(Operator.MAX, Lists.of()));
        }

        @Override
        public NumberExpression<T, U> min() {
            return new OperableNumberImpl<>(this, operate(Operator.MIN, Lists.of()));
        }
    }
}
