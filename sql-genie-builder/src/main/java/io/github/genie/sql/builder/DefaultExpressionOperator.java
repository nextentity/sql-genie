package io.github.genie.sql.builder;

import io.github.genie.sql.api.Column;
import io.github.genie.sql.api.Expression;
import io.github.genie.sql.api.ExpressionHolder;
import io.github.genie.sql.api.ExpressionOperator.PathOperator;
import io.github.genie.sql.api.Lists;
import io.github.genie.sql.api.Operator;
import io.github.genie.sql.api.Order;
import io.github.genie.sql.api.Path;
import io.github.genie.sql.api.Path.BooleanPath;
import io.github.genie.sql.api.Path.ComparablePath;
import io.github.genie.sql.api.Path.NumberPath;
import io.github.genie.sql.api.Path.StringPath;
import io.github.genie.sql.builder.QueryStructures.OrderImpl;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static io.github.genie.sql.api.Lists.of;
import static io.github.genie.sql.builder.Expressions.TRUE;

@SuppressWarnings("PatternVariableCanBeUsed")
class DefaultExpressionOperator<T, U, B> implements PathOperator<T, U, B> {

    private static final Expression EMPTY_PATH = Expressions.column(of());
    protected final Context<B> context;

    public DefaultExpressionOperator(Context<? extends B> context) {
        this.context = TypeCastUtil.unsafeCast(context);
    }

    @Override
    public <V> PathOperator<T, V, B> get(Path<U, V> path) {
        return new DefaultExpressionOperator<>(toPaths(path));
    }

    @Override
    public StringOperator<T, B> get(StringPath<U> path) {
        return new StringOpsImpl<>(toPaths(path));
    }

    @Override
    public <V extends Number & Comparable<V>> NumberOperator<T, V, B> get(NumberPath<U, V> path) {
        return new NumberOpsImpl<>(toPaths(path));
    }

    @Override
    public <V extends Comparable<V>> ComparableOperator<T, V, B> get(ComparablePath<U, V> path) {
        return new ComparableOpsImpl<>(toPaths(path));
    }

    @Override
    public B get(BooleanPath<U> path) {
        return build(toPaths(path));
    }

    @Override
    public B eq(U value) {
        return build(operateRight(Operator.EQ, value));
    }

    @Override
    public B eq(ExpressionHolder<T, U> value) {
        return build(operateRight(Operator.EQ, value));
    }

    @Override
    public B ne(U value) {
        return build(operateRight(Operator.NE, value));
    }

    @Override
    public B ne(ExpressionHolder<T, U> value) {
        return build(operateRight(Operator.NE, value));
    }

    @SafeVarargs
    @Override
    public final B in(U... values) {
        List<Expression> expressions = Arrays.stream(values)
                .map(Expressions::of)
                .collect(Collectors.toList());
        return build(operateRight(Operator.IN, expressions));
    }

    @Override
    public B in(@NotNull List<? extends ExpressionHolder<T, U>> values) {
        return build(operateRight(Operator.IN, values));
    }

    @Override
    public B in(@NotNull Collection<? extends U> values) {
        Context<B> ctx = operateRight(Operator.IN, values.stream()
                .map(Expressions::of)
                .collect(Collectors.toList()));
        return build(ctx);
    }

    @SafeVarargs
    @Override
    public final B notIn(U... values) {
        List<Expression> expressions = Arrays.stream(values)
                .map(Expressions::of)
                .collect(Collectors.toList());
        Context<B> ctx = operateRight(Operator.IN, expressions);
        return build(ctx.not());
    }

    @Override
    public B notIn(@NotNull List<? extends ExpressionHolder<T, U>> values) {
        return build(operateRight(Operator.IN, values).not());
    }

    @Override
    public B notIn(@NotNull Collection<? extends U> values) {
        List<Expression> expressions = values.stream()
                .map(Expressions::of)
                .collect(Collectors.toList());
        Context<B> ctx = operateRight(Operator.IN, expressions);
        return build(ctx.not());
    }

    @Override
    public B isNull() {
        return build(operateRight(Operator.IS_NULL, Lists.of()));
    }

    @Override
    public NumberOperator<T, Integer, B> count() {
        return new NumberOpsImpl<>(operateRight(Operator.COUNT, Lists.of()));
    }

    @Override
    public B isNotNull() {
        return build(operateRight(Operator.IS_NOT_NULL, Lists.of()));
    }

    @Override
    public Expression expression() {
        Expression merge = merge();

        List<Expression> expressions = context.getExpressions();
        if (expressions.isEmpty()) {
            return merge;
        }
        Iterator<Expression> iterator = expressions.iterator();
        Expression l = iterator.next();
        List<Expression> r = new ArrayList<>(expressions.size());
        while (iterator.hasNext()) {
            r.add(iterator.next());
        }
        r.add(merge);
        return Expressions.operate(l, Operator.OR, r);
    }

    protected Expression merge() {
        return Expressions.isTrue(context.left)
                ? context.right
                : Expressions.operate(context.left, Operator.AND, context.right);
    }

    protected Context<B> toPaths(Path<?, ?> path) {
        Context<B> r = context.clone0();
        if (r.right == null) {
            r.right = Expressions.of(path);
        }
        if (r.right instanceof Column) {
            Column p = (Column) r.right;
            r.right = Expressions.concat(p, path);
        } else {
            throw new IllegalStateException();
        }
        return r;
    }

    protected B build(Context<B> context) {
        return this.context.builder.apply(context);
    }

    protected Context<B> operateRight(Operator operator, Object rightOperand) {
        return operateRight(operator, Expressions.of(rightOperand));
    }

    protected Context<B> operateRight(Operator operator, ExpressionHolder<?, ?> rightOperand) {
        return operateRight(operator, rightOperand.expression());
    }

    protected Context<B> operateRight(Operator operator, Expression rightOperand) {
        return operateRight(operator, Lists.of(rightOperand));
    }

    protected Context<B> operateRight(Operator operator, List<? extends Expression> rightOperand) {
        Context<B> res = context.clone0();
        res.right = Expressions.operate(context.getRight(), operator, rightOperand);
        return res;
    }

    public Order<T> asc() {
        return OrderImpl.of(this, Order.SortOrder.ASC);
    }

    public Order<T> desc() {
        return OrderImpl.of(this, Order.SortOrder.DESC);
    }

    static class ComparableOpsImpl<T, U extends Comparable<U>, B> extends DefaultExpressionOperator<T, U, B> implements ComparableOperator<T, U, B> {

        public ComparableOpsImpl(Context<B> context) {
            super(context);
        }

        public B ge(U value) {
            return build(operateRight(Operator.GE, value));
        }

        public B gt(U value) {
            return build(operateRight(Operator.GT, value));
        }

        public B le(U value) {
            return build(operateRight(Operator.LE, value));
        }

        public B lt(U value) {
            return build(operateRight(Operator.LT, value));
        }

        public B between(U l, U r) {
            return build(operateRight(Operator.BETWEEN, Lists.of(Expressions.of(l), Expressions.of(r))));
        }

        public B ge(ExpressionHolder<T, U> expression) {
            return build(operateRight(Operator.GE, expression));
        }

        public B gt(ExpressionHolder<T, U> expression) {
            return build(operateRight(Operator.GT, expression));
        }

        public B le(ExpressionHolder<T, U> expression) {
            return build(operateRight(Operator.LE, expression));
        }

        public B lt(ExpressionHolder<T, U> expression) {
            return build(operateRight(Operator.LT, expression));
        }

        public B between(ExpressionHolder<T, U> l, ExpressionHolder<T, U> r) {
            return build(operateRight(Operator.BETWEEN, Lists.of(Expressions.of(l), Expressions.of(r))));
        }

        public B between(ExpressionHolder<T, U> l, U r) {
            return build(operateRight(Operator.BETWEEN, Lists.of(Expressions.of(l), Expressions.of(r))));
        }

        public B between(U l, ExpressionHolder<T, U> r) {
            return build(operateRight(Operator.BETWEEN, Lists.of(Expressions.of(l), Expressions.of(r))));
        }

        @Override
        public B notBetween(U l, U r) {
            return build(operateRight(Operator.BETWEEN, Lists.of(Expressions.of(l), Expressions.of(r))).not());
        }

        @Override
        public B notBetween(ExpressionHolder<T, U> l, ExpressionHolder<T, U> r) {
            return build(operateRight(Operator.BETWEEN, Lists.of(Expressions.of(l), Expressions.of(r))).not());
        }

        @Override
        public B notBetween(ExpressionHolder<T, U> l, U r) {
            return build(operateRight(Operator.BETWEEN, Lists.of(Expressions.of(l), Expressions.of(r))).not());
        }

        @Override
        public B notBetween(U l, ExpressionHolder<T, U> r) {
            return build(operateRight(Operator.BETWEEN, Lists.of(Expressions.of(l), Expressions.of(r))).not());
        }
    }

    static class StringOpsImpl<T, B> extends ComparableOpsImpl<T, String, B> implements StringOperator<T, B> {

        public StringOpsImpl(Context<B> context) {
            super(context);
        }

        public B like(String value) {
            return build(operateRight(Operator.LIKE, value));
        }

        public StringOperator<T, B> lower() {
            return new StringOpsImpl<>(operateRight(Operator.LOWER, Lists.of()));
        }

        public StringOperator<T, B> upper() {
            return new StringOpsImpl<>(operateRight(Operator.UPPER, Lists.of()));
        }

        public StringOperator<T, B> substring(int a, int b) {
            return new StringOpsImpl<>(operateRight(Operator.SUBSTRING, Lists.of(Expressions.of(a), Expressions.of(b))));
        }

        public StringOperator<T, B> substring(int a) {
            return new StringOpsImpl<>(operateRight(Operator.SUBSTRING, Lists.of(Expressions.of(a))));
        }

        public StringOperator<T, B> trim() {
            return new StringOpsImpl<>(operateRight(Operator.TRIM, Lists.of()));
        }

        public NumberOperator<T, Integer, B> length() {
            return new NumberOpsImpl<>(operateRight(Operator.LENGTH, Lists.of()));
        }

        @Override
        public B notLike(String value) {
            return build(operateRight(Operator.LIKE, value).not());
        }
    }

    static class NumberOpsImpl<T, U extends Number & Comparable<U>, B> extends ComparableOpsImpl<T, U, B> implements NumberOperator<T, U, B> {
        public NumberOpsImpl(Context<B> context) {
            super(context);
        }

        @Override
        public NumberOperator<T, U, B> add(U value) {
            return new NumberOpsImpl<>(operateRight(Operator.ADD, value));
        }

        @Override
        public NumberOperator<T, U, B> subtract(U value) {
            return new NumberOpsImpl<>(operateRight(Operator.SUBTRACT, value));
        }

        @Override
        public NumberOperator<T, U, B> multiply(U value) {
            return new NumberOpsImpl<>(operateRight(Operator.MULTIPLY, value));
        }

        @Override
        public NumberOperator<T, U, B> divide(U value) {
            return new NumberOpsImpl<>(operateRight(Operator.DIVIDE, value));
        }

        @Override
        public NumberOperator<T, U, B> mod(U value) {
            return new NumberOpsImpl<>(operateRight(Operator.MOD, value));
        }

        @Override
        public NumberOperator<T, U, B> add(ExpressionHolder<T, U> value) {
            return new NumberOpsImpl<>(operateRight(Operator.ADD, value));
        }

        @Override
        public NumberOperator<T, U, B> subtract(ExpressionHolder<T, U> value) {
            return new NumberOpsImpl<>(operateRight(Operator.SUBTRACT, value));
        }

        @Override
        public NumberOperator<T, U, B> multiply(ExpressionHolder<T, U> value) {
            return new NumberOpsImpl<>(operateRight(Operator.MULTIPLY, value));
        }

        @Override
        public NumberOperator<T, U, B> divide(ExpressionHolder<T, U> value) {
            return new NumberOpsImpl<>(operateRight(Operator.DIVIDE, value));
        }

        @Override
        public NumberOperator<T, U, B> mod(ExpressionHolder<T, U> value) {
            return new NumberOpsImpl<>(operateRight(Operator.MOD, value));
        }

        @Override
        public <V extends Number & Comparable<V>> NumberOperator<T, V, B> sum() {
            return new NumberOpsImpl<>(operateRight(Operator.SUM, Lists.of()));
        }

        @Override
        public <V extends Number & Comparable<V>> NumberOperator<T, V, B> avg() {
            return new NumberOpsImpl<>(operateRight(Operator.AVG, Lists.of()));
        }

        @Override
        public <V extends Number & Comparable<V>> NumberOperator<T, V, B> max() {
            return new NumberOpsImpl<>(operateRight(Operator.MAX, Lists.of()));
        }

        @Override
        public <V extends Number & Comparable<V>> NumberOperator<T, V, B> min() {
            return new NumberOpsImpl<>(operateRight(Operator.MIN, Lists.of()));
        }
    }

    static class BooleanOpsImpl<T, B> extends ComparableOpsImpl<T, Boolean, B> implements BooleanOperator<T, B> {
        public BooleanOpsImpl(Context<B> context) {
            super(context);
        }

        public AndConnector<T> and(ExpressionHolder<T, Boolean> value) {
            Expression mt = expression();
            Expression expression = Expressions.operate(mt, Operator.AND, value.expression());
            return new AndConnectorImpl<>(new Context<>(Lists.of(), TRUE, expression, AndConnectorImpl::new));
        }

        public OrConnector<T> or(ExpressionHolder<T, Boolean> value) {
            Expression mt = expression();
            Expression expression = Expressions.operate(mt, Operator.OR, value.expression());
            return new OrConnectorImpl<>(new Context<>(Lists.of(), TRUE, expression, OrConnectorImpl::new));
        }

        public AndConnector<T> and(List<ExpressionHolder<T, Boolean>> expressions) {
            Expression mt = expression();
            Expression expression = Expressions.operate(mt, Operator.AND, expressions.stream().map(ExpressionHolder::expression).collect(Collectors.toList()));
            return new AndConnectorImpl<>(new Context<>(Lists.of(), TRUE, expression, AndConnectorImpl::new));
        }

        public OrConnector<T> or(List<ExpressionHolder<T, Boolean>> expressions) {
            Expression mt = expression();
            Expression expression = Expressions.operate(mt, Operator.OR, expressions.stream().map(ExpressionHolder::expression).collect(Collectors.toList()));
            return new OrConnectorImpl<>(new Context<>(Lists.of(), TRUE, expression, OrConnectorImpl::new));
        }

        @Override
        public Predicate<T> then() {
            return new PredicateOpsImpl<>(
                    new Context<>(Lists.of(), TRUE, merge(), PredicateOpsImpl::new));
        }

        public <R> PathOperator<T, R, OrConnector<T>> or(Path<T, R> path) {
            List<Expression> expressions = Util.concat(context.expressions, merge());
            return new DefaultExpressionOperator<>(new Context<>(expressions, TRUE, Expressions.of(path), OrConnectorImpl::new));
        }

        public <R extends Comparable<R>> ComparableOperator<T, R, OrConnector<T>> or(ComparablePath<T, R> path) {
            List<Expression> expressions = Util.concat(context.expressions, merge());
            return new ComparableOpsImpl<>(new Context<>(expressions, TRUE, Expressions.of(path), OrConnectorImpl::new));
        }

        public <R extends Number & Comparable<R>> NumberOperator<T, R, OrConnector<T>> or(NumberPath<T, R> path) {
            List<Expression> expressions = Util.concat(context.expressions, merge());
            return new NumberOpsImpl<>(new Context<>(expressions, TRUE, Expressions.of(path), OrConnectorImpl::new));
        }

        public StringOperator<T, OrConnector<T>> or(StringPath<T> path) {
            List<Expression> expressions = Util.concat(context.expressions, merge());
            return new StringOpsImpl<>(new Context<>(expressions, TRUE, Expressions.of(path), OrConnectorImpl::new));
        }

        public OrConnector<T> or(BooleanPath<T> path) {
            List<Expression> expressions = Util.concat(context.expressions, merge());
            return new OrConnectorImpl<>(new Context<>(expressions, TRUE, Expressions.of(path), OrConnectorImpl::new));
        }

        public <R> PathOperator<T, R, AndConnector<T>> and(Path<T, R> path) {
            List<Expression> expressions = context.expressions;
            Expression left = merge();
            Expression right = Expressions.of(path);
            return new DefaultExpressionOperator<>(new Context<>(expressions, left, right, AndConnectorImpl::new));
        }

        public <R extends Comparable<R>> ComparableOperator<T, R, AndConnector<T>> and(ComparablePath<T, R> path) {
            List<Expression> expressions = context.expressions;
            Expression left = merge();
            Expression right = Expressions.of(path);
            return new ComparableOpsImpl<>(new Context<>(expressions, left, right, AndConnectorImpl::new));
        }

        public <R extends Number & Comparable<R>> NumberOperator<T, R, AndConnector<T>> and(NumberPath<T, R> path) {
            List<Expression> expressions = context.expressions;
            Expression left = merge();
            Expression right = Expressions.of(path);
            return new NumberOpsImpl<>(new Context<>(expressions, left, right, AndConnectorImpl::new));
        }

        public StringOperator<T, AndConnector<T>> and(StringPath<T> path) {
            List<Expression> expressions = context.expressions;
            Expression left = merge();
            Expression right = Expressions.of(path);
            return new StringOpsImpl<>(new Context<>(expressions, left, right, AndConnectorImpl::new));
        }

        public AndConnector<T> and(BooleanPath<T> path) {
            List<Expression> expressions = context.expressions;
            Expression left = merge();
            Expression right = Expressions.of(path);
            return new AndConnectorImpl<>(new Context<>(expressions, left, right, AndConnectorImpl::new));
        }

    }

    static class OrConnectorImpl<T> extends BooleanOpsImpl<T, OrConnector<T>> implements OrConnector<T> {
        public OrConnectorImpl(Context<OrConnector<T>> context) {
            super(context);
        }
    }

    static class AndConnectorImpl<T> extends BooleanOpsImpl<T, AndConnector<T>> implements AndConnector<T> {
        public AndConnectorImpl(Context<AndConnector<T>> context) {
            super(context);
        }
    }

    static class PredicateOpsImpl<T> extends BooleanOpsImpl<T, Predicate<T>> implements Predicate<T> {
        public PredicateOpsImpl(Context<Predicate<T>> context) {
            super(context);
        }

        @Override
        public Predicate<T> not() {
            Expression expression = expression();
            Expression m = Expressions.operate(expression, Operator.NOT);
            return new PredicateOpsImpl<>(new Context<>(Lists.of(), TRUE, m, PredicateOpsImpl::new));
        }

    }

    static class RootImpl<T> extends DefaultExpressionOperator<T, T, Predicate<T>> implements Root<T> {
        public RootImpl() {
            super(new Context<>(Lists.of(), TRUE, EMPTY_PATH, PredicateOpsImpl::new));
        }

        public <U> PathOperator<T, U, Predicate<T>> get(Path<T, U> path) {
            return new PathExpressionImpl<>(toPaths(path));
        }

        @Override
        public StringOperator<T, Predicate<T>> get(StringPath<T> path) {
            return new StringExpressionImpl<>(toPaths(path));
        }

        @Override
        public <V extends Number & Comparable<V>> NumberOperator<T, V, Predicate<T>> get(NumberPath<T, V> path) {
            return new NumberExpressionImpl<>(toPaths(path));
        }

        @Override
        public <V extends Comparable<V>> ComparableOperator<T, V, Predicate<T>> get(ComparablePath<T, V> path) {
            return new ComparableExpressionImpl<>(toPaths(path));
        }

    }

    @NotNull
    @AllArgsConstructor
    @Data
    protected static class Context<B> {
        List<Expression> expressions;
        Expression left;
        Expression right;
        Function<Context<B>, B> builder;

        public Context<B> clone0() {
            return new Context<>(expressions, left, right, builder);
        }

        Context<B> not() {
            right = Expressions.operate(right, Operator.NOT);
            return this;
        }

    }

    public static <T> Predicate<T> ofBoolOps(Expression expression) {
        return new PredicateOpsImpl<>(new Context<>(
                Lists.of(),
                TRUE,
                expression,
                PredicateOpsImpl::new
        ));
    }

    static class PathExpressionImpl<T, U>
            extends DefaultExpressionOperator<T, U, Predicate<T>>
            implements PathOperator<T, U, Predicate<T>> {
        public PathExpressionImpl(Context<? extends Predicate<T>> context) {
            super(context);
        }

        public @Override <V> PathOperator<T, V, Predicate<T>> get(Path<U, V> path) {
            return new PathExpressionImpl<>(toPaths(path));
        }

    }

    static class StringExpressionImpl<T>
            extends StringOpsImpl<T, Predicate<T>>
            implements StringOperator<T, Predicate<T>> {
        public StringExpressionImpl(Context<Predicate<T>> context) {
            super(context);
        }
    }

    static class NumberExpressionImpl<T, U extends Number & Comparable<U>>
            extends NumberOpsImpl<T, U, Predicate<T>>
            implements NumberOperator<T, U, Predicate<T>> {

        public NumberExpressionImpl(Context<Predicate<T>> context) {
            super(context);
        }
    }

    static class ComparableExpressionImpl<T, U extends Comparable<U>>
            extends ComparableOpsImpl<T, U, Predicate<T>>
            implements ComparableOperator<T, U, Predicate<T>> {
        public ComparableExpressionImpl(Context<Predicate<T>> context) {
            super(context);
        }
    }

}
