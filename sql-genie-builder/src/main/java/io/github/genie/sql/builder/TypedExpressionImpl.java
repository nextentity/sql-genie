package io.github.genie.sql.builder;

import io.github.genie.sql.api.Column;
import io.github.genie.sql.api.Root;
import io.github.genie.sql.api.Expression;
import io.github.genie.sql.api.ExpressionHolder;
import io.github.genie.sql.api.ExpressionOperator.ComparableOperator;
import io.github.genie.sql.api.ExpressionOperator.NumberOperator;
import io.github.genie.sql.api.ExpressionOperator.PathOperator;
import io.github.genie.sql.api.ExpressionOperator.StringOperator;
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
import io.github.genie.sql.api.TypedExpression;
import io.github.genie.sql.builder.DefaultExpressionOperator.ComparableOperatorImpl;
import io.github.genie.sql.builder.DefaultExpressionOperator.NumberOperatorImpl;
import io.github.genie.sql.builder.DefaultExpressionOperator.PathOperatorImpl;
import io.github.genie.sql.builder.DefaultExpressionOperator.StringOperatorImpl;
import io.github.genie.sql.builder.QueryStructures.OrderImpl;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

class TypedExpressionImpl<T, U> implements TypedExpression<T, U> {
    protected final Operation operation;
    protected final Expression operand;

    public TypedExpressionImpl(Operation operation, Expression operand) {
        this.operation = operation;
        this.operand = operand;
    }

    public TypedExpressionImpl(TypedExpressionImpl<?, ?> origin, Expression operand) {
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
        return new BooleanExpressionImpl<>(this, operate);
    }

    @Override
    public BooleanExpression<T> ne(U value) {
        return ne(ExpressionHolders.of(value));
    }

    @Override
    public BooleanExpression<T> ne(ExpressionHolder<T, U> value) {
        Expression operate = operate(Operator.NE, value);
        return new BooleanExpressionImpl<>(this, operate);
    }

    @SafeVarargs
    @Override
    public final BooleanExpression<T> in(U... values) {
        return in(ExpressionHolders.of(values));
    }

    @Override
    public BooleanExpression<T> in(@NotNull List<? extends ExpressionHolder<T, U>> values) {
        Expression operate = operate(Operator.IN, values);
        return new BooleanExpressionImpl<>(this, operate);
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
        return new BooleanExpressionImpl<>(this, operate);
    }

    @Override
    public BooleanExpression<T> notIn(@NotNull Collection<? extends U> values) {
        return notIn(ExpressionHolders.of(values));
    }

    @Override
    public BooleanExpression<T> isNull() {
        Expression operate = Expressions.operate(operand, Operator.IS_NULL);
        return new BooleanExpressionImpl<>(this, operate);
    }

    @Override
    public NumberExpression<T, Long> count() {
        Expression operate = Expressions.operate(operand, Operator.COUNT);
        return new NumberExpressionImpl<>(this, operate);
    }

    @Override
    public BooleanExpression<T> isNotNull() {
        Expression operate = Expressions.operate(operand, Operator.IS_NOT_NULL);
        return new BooleanExpressionImpl<>(this, operate);
    }

    @Override
    public Root<T> root() {
        return RootImpl.of();
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

    static class PathExpressionImpl<T, U>
            extends TypedExpressionImpl<T, U>
            implements PathExpression<T, U> {

        public PathExpressionImpl(TypedExpressionImpl<?, ?> origin, Expression operand) {
            super(origin, operand);
        }

        public PathExpressionImpl(Operation operation, Expression operand) {
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
            return new PathExpressionImpl<>(this, getPath(path));
        }

        @Override
        public StringExpression<T> get(StringPath<U> path) {
            return new StringExpressionImpl<>(this, getPath(path));
        }

        @Override
        public <R extends Number & Comparable<R>> NumberExpression<T, R> get(NumberPath<U, R> path) {
            return new NumberExpressionImpl<>(this, getPath(path));
        }

        @Override
        public <R extends Comparable<R>> ComparableExpression<T, R> get(ComparablePath<U, R> path) {
            return new ComparableExpressionImpl<>(this, getPath(path));
        }

        @Override
        public BooleanExpression<T> get(BooleanPath<U> path) {
            return new BooleanExpressionImpl<>(this, getPath(path));
        }
    }

    static class ComparableExpressionImpl<T, U extends Comparable<U>>
            extends TypedExpressionImpl<T, U>
            implements ComparableExpression<T, U> {

        public ComparableExpressionImpl(TypedExpressionImpl<?, ?> origin, Expression operand) {
            super(origin, operand);
        }

        public ComparableExpressionImpl(Operation operation, Expression operand) {
            super(operation, operand);
        }

        @Override
        public BooleanExpression<T> ge(ExpressionHolder<T, U> expression) {
            return new BooleanExpressionImpl<>(this, operate(Operator.GE, expression));
        }

        @Override
        public BooleanExpression<T> gt(ExpressionHolder<T, U> expression) {
            return new BooleanExpressionImpl<>(this, operate(Operator.GT, expression));
        }

        @Override
        public BooleanExpression<T> le(ExpressionHolder<T, U> expression) {
            return new BooleanExpressionImpl<>(this, operate(Operator.LE, expression));
        }

        @Override
        public BooleanExpression<T> lt(ExpressionHolder<T, U> expression) {
            return new BooleanExpressionImpl<>(this, operate(Operator.LT, expression));
        }

        @Override
        public BooleanExpression<T> between(ExpressionHolder<T, U> l, ExpressionHolder<T, U> r) {
            return new BooleanExpressionImpl<>(this, operate(Operator.BETWEEN, List.of(l, r)));
        }

        @Override
        public BooleanExpression<T> notBetween(ExpressionHolder<T, U> l, ExpressionHolder<T, U> r) {
            Expression operate = operate(Operator.BETWEEN, List.of(l, r));
            operate = Expressions.operate(operate, Operator.NOT);
            return new BooleanExpressionImpl<>(this, operate);
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

    static class NumberExpressionImpl<T, U extends Number & Comparable<U>>
            extends ComparableExpressionImpl<T, U>
            implements NumberExpression<T, U> {

        public NumberExpressionImpl(TypedExpressionImpl<?, ?> origin, Expression operand) {
            super(origin, operand);
        }

        public NumberExpressionImpl(Operation operation, Expression operand) {
            super(operation, operand);
        }

        @Override
        public NumberExpression<T, U> add(ExpressionHolder<T, U> expression) {
            return new NumberExpressionImpl<>(this, operate(Operator.ADD, expression));
        }

        @Override
        public NumberExpression<T, U> subtract(ExpressionHolder<T, U> expression) {
            return new NumberExpressionImpl<>(this, operate(Operator.SUBTRACT, expression));
        }

        @Override
        public NumberExpression<T, U> multiply(ExpressionHolder<T, U> expression) {
            return new NumberExpressionImpl<>(this, operate(Operator.MULTIPLY, expression));
        }

        @Override
        public NumberExpression<T, U> divide(ExpressionHolder<T, U> expression) {
            return new NumberExpressionImpl<>(this, operate(Operator.DIVIDE, expression));
        }

        @Override
        public NumberExpression<T, U> mod(ExpressionHolder<T, U> expression) {
            return new NumberExpressionImpl<>(this, operate(Operator.MOD, expression));
        }

        @Override
        public NumberExpression<T, U> sum() {
            return new NumberExpressionImpl<>(this, operate(Operator.SUM, Lists.of()));
        }

        @Override
        public <R extends Number & Comparable<R>> NumberExpression<T, R> avg() {
            return new NumberExpressionImpl<>(this, operate(Operator.AVG, Lists.of()));
        }

        @Override
        public NumberExpression<T, U> max() {
            return new NumberExpressionImpl<>(this, operate(Operator.MAX, Lists.of()));
        }

        @Override
        public NumberExpression<T, U> min() {
            return new NumberExpressionImpl<>(this, operate(Operator.MIN, Lists.of()));
        }
    }

    static class StringExpressionImpl<T>
            extends ComparableExpressionImpl<T, String>
            implements StringExpression<T> {

        public StringExpressionImpl(TypedExpressionImpl<?, ?> origin, Expression operand) {
            super(origin, operand);
        }

        public StringExpressionImpl(Operation operation, Expression operand) {
            super(operation, operand);
        }

        @Override
        public BooleanExpression<T> like(String value) {
            return new BooleanExpressionImpl<>(this, operate(Operator.LIKE, value));
        }

        @Override
        public BooleanExpression<T> notLike(String value) {
            Expression operate = operate(Operator.LIKE, value);
            operate = Expressions.operate(operate, Operator.NOT);
            return new BooleanExpressionImpl<>(this, operate);
        }

        @Override
        public StringExpression<T> lower() {
            return new StringExpressionImpl<>(this, operate(Operator.LOWER, Lists.of()));
        }

        @Override
        public StringExpression<T> upper() {
            return new StringExpressionImpl<>(this, operate(Operator.UPPER, Lists.of()));
        }

        @Override
        public StringExpression<T> substring(int a, int b) {
            List<ExpressionHolder<T, ?>> expressions =
                    Lists.of(ExpressionHolders.of(a), ExpressionHolders.of(b));
            return new StringExpressionImpl<>(this, operate(Operator.SUBSTRING, expressions));
        }

        @Override
        public StringExpression<T> substring(int a) {
            return new StringExpressionImpl<>(this, operate(Operator.SUBSTRING, a));
        }

        @Override
        public StringExpression<T> trim() {
            return new StringExpressionImpl<>(this, operate(Operator.TRIM, Lists.of()));
        }

        @Override
        public NumberExpression<T, Integer> length() {
            return new NumberExpressionImpl<>(this, operate(Operator.LENGTH, Lists.of()));
        }
    }

    static class BooleanExpressionImpl<T>
            extends ComparableExpressionImpl<T, Boolean>
            implements BooleanExpression<T> {

        public BooleanExpressionImpl(TypedExpressionImpl<?, ?> origin, Expression operand) {
            super(origin, operand);
        }

        public BooleanExpressionImpl(Operation operation, Expression operand) {
            super(operation, operand);
        }

        @Override
        public BooleanExpression<T> not() {
            return new BooleanExpressionImpl<>(this, Expressions.operate(operand, Operator.NOT));
        }

        @Override
        public <R> PathOperator<T, R, OrOperator<T>> or(Path<T, R> path) {
            PathExpression<T, R> expression = new PathExpressionImpl<>(or(), Expressions.of(path));
            return new PathOperatorImpl<>(expression, this::newOrOperator);
        }

        @NotNull
        OrOperator<T> newOrOperator(TypedExpression<?, ?> expression) {
            if (expression instanceof OrOperator) {
                return TypeCastUtil.unsafeCast(expression);
            }
            TypedExpressionImpl<?, ?> expr = (TypedExpressionImpl<?, ?>) expression;
            return new BooleanExpressionImpl<>(expr.operation, expr.operand);
        }

        @NotNull
        AndOperator<T> newAndOperator(TypedExpression<?, ?> expression) {
            if (expression instanceof AndOperator) {
                return TypeCastUtil.unsafeCast(expression);
            }
            TypedExpressionImpl<?, ?> expr = (TypedExpressionImpl<?, ?>) expression;
            return new BooleanExpressionImpl<>(expr.operation, expr.operand);
        }

        @Override
        public <R extends Comparable<R>> ComparableOperator<T, R, OrOperator<T>> or(ComparablePath<T, R> path) {
            ComparableExpression<T, R> expression = new ComparableExpressionImpl<>(or(), Expressions.of(path));
            return new ComparableOperatorImpl<>(expression, this::newOrOperator);
        }

        @Override
        public <R extends Number & Comparable<R>> NumberOperator<T, R, OrOperator<T>> or(NumberPath<T, R> path) {
            NumberExpression<T, R> expression = new NumberExpressionImpl<>(or(), Expressions.of(path));
            return new NumberOperatorImpl<>(expression, this::newOrOperator);
        }

        @Override
        public OrOperator<T> or(BooleanPath<T> path) {
            return new BooleanExpressionImpl<>(or(), Expressions.of(path));
        }

        @Override
        public StringOperator<T, ? extends OrOperator<T>> or(StringPath<T> path) {
            StringExpression<T> expression = new StringExpressionImpl<>(or(), Expressions.of(path));
            return new StringOperatorImpl<>(expression, this::newOrOperator);
        }

        @Override
        public OrOperator<T> or(ExpressionHolder<T, Boolean> expression) {
            return new BooleanExpressionImpl<>(or(), expression.expression());
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
            return new BooleanExpressionImpl<>(operation, last);
        }

        @Override
        public <R> PathOperator<T, R, AndOperator<T>> and(Path<T, R> path) {
            PathExpression<T, R> expression = new PathExpressionImpl<>(and(), Expressions.of(path));
            return new PathOperatorImpl<>(expression, this::newAndOperator);
        }

        @Override
        public <R extends Comparable<R>> ComparableOperator<T, R, AndOperator<T>> and(ComparablePath<T, R> path) {
            ComparableExpression<T, R> expression = new ComparableExpressionImpl<>(and(), Expressions.of(path));
            return new ComparableOperatorImpl<>(expression, this::newAndOperator);
        }

        @Override
        public <R extends Number & Comparable<R>> NumberOperator<T, R, AndOperator<T>> and(NumberPath<T, R> path) {
            NumberExpression<T, R> expression = new NumberExpressionImpl<>(and(), Expressions.of(path));
            return new NumberOperatorImpl<>(expression, this::newAndOperator);
        }

        @Override
        public AndOperator<T> and(BooleanPath<T> path) {
            BooleanExpression<T> expression = new BooleanExpressionImpl<>(and(), Expressions.of(path));
            return new BooleanExpressionImpl<>(and(), expression.expression());
        }

        @Override
        public StringOperator<T, AndOperator<T>> and(StringPath<T> path) {
            StringExpression<T> expression = new StringExpressionImpl<>(and(), Expressions.of(path));
            return new StringOperatorImpl<>(expression, this::newAndOperator);
        }

        @Override
        public AndOperator<T> and(ExpressionHolder<T, Boolean> expression) {
            return new BooleanExpressionImpl<>(and(), expression.expression());
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
                expr = new BooleanExpressionImpl<>(operation, last);
            }
            return expr;
        }

        @Override
        public Predicate<T> then() {
            return new PredicateImpl<>(expression());
        }
    }

    @Accessors(fluent = true)
    static class PredicateImpl<T> implements Predicate<T> {
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
}
