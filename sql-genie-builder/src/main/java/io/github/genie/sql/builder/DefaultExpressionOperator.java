package io.github.genie.sql.builder;

import io.github.genie.sql.api.Column;
import io.github.genie.sql.api.Expression;
import io.github.genie.sql.api.ExpressionHolder;
import io.github.genie.sql.api.ExpressionOperator.PathOperator;
import io.github.genie.sql.api.Operator;
import io.github.genie.sql.api.Order;
import io.github.genie.sql.api.Path;
import io.github.genie.sql.builder.QueryStructures.OrderImpl;
import io.github.genie.sql.api.Path.BooleanPath;
import io.github.genie.sql.api.Path.ComparablePath;
import io.github.genie.sql.api.Path.NumberPath;
import io.github.genie.sql.api.Path.StringPath;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

class DefaultExpressionOperator<T, U, B> implements PathOperator<T, U, B> {

    private static final Expression TRUE = ExpressionBuilders.TRUE;
    private static final Expression EMPTY_PATH = ExpressionBuilders.fromPaths(List.of());
    protected Metadata<B> metadata;

    public DefaultExpressionOperator(Metadata<? extends B> metadata) {
        // noinspection unchecked
        this.metadata = (Metadata<B>) metadata;
    }

    @Override
    public <V> PathOperator<T, V, B> get(Path<U, V> path) {
        return new DefaultExpressionOperator<>(toPaths(path));
    }

    @Override
    public StringOperator<T, B> get(StringPath<T> path) {
        return new StringOpsImpl<>(toPaths(path));
    }

    @Override
    public <V extends Number & Comparable<V>> NumberOperator<T, V, B> get(NumberPath<T, V> path) {
        return new NumberOpsImpl<>(toPaths(path));
    }

    @Override
    public <V extends Comparable<V>> ComparableOperator<T, V, B> get(ComparablePath<T, V> path) {
        return new ComparableOpsImpl<>(toPaths(path));
    }

    @Override
    public B get(BooleanPath<T> path) {
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
        return build(operateRight(Operator.IN, Arrays.stream(values).map(ExpressionBuilders::of).toList()));
    }

    @Override
    public B in(@NotNull List<? extends ExpressionHolder<T, U>> values) {
        return build(operateRight(Operator.IN, values));
    }

    @SafeVarargs
    @Override
    public final B notIn(U... values) {
        Metadata<B> metadata = operateRight(Operator.IN, Arrays.stream(values).map(ExpressionBuilders::of).toList());
        return build(metadata.not());
    }

    @Override
    public B notIn(@NotNull List<? extends ExpressionHolder<T, U>> values) {
        return build(operateRight(Operator.IN, values).not());
    }

    @Override
    public B isNull() {
        return build(operateRight(Operator.IS_NULL, List.of()));
    }

    @Override
    public NumberOperator<T, Integer, B> count() {
        return new NumberOpsImpl<>(operateRight(Operator.COUNT, List.of()));
    }

    @Override
    public B isNotNull() {
        return build(operateRight(Operator.IS_NOT_NULL, List.of()));
    }


    @Override
    public Expression expression() {
        Expression merge = merge();

        List<Expression> expressions = metadata.getExpressions();
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
        return ExpressionBuilders.operate(l, Operator.OR, r);
    }

    protected Expression merge() {
        return ExpressionBuilders.isTrue(metadata.left)
                ? metadata.right
                : ExpressionBuilders.operate(metadata.left, Operator.AND, metadata.right);
    }

    protected Metadata<B> toPaths(Path<?, ?> path) {
        Metadata<B> r = metadata.clone0();
        if (r.right == null) {
            r.right = ExpressionBuilders.of(path);
        }
        if (r.right instanceof Column p) {
            List<String> paths = Util.concat(p.paths(), ExpressionBuilders.asString(path));
            r.right = ExpressionBuilders.fromPaths(paths);
        } else {
            throw new IllegalStateException();
        }
        return r;
    }

    protected B build(Metadata<B> metadata) {
        return this.metadata.builder.build(metadata);
    }

    protected Metadata<B> operateRight(Operator operator, Object rightOperand) {
        return operateRight(operator, ExpressionBuilders.of(rightOperand));
    }

    protected Metadata<B> operateRight(Operator operator, ExpressionHolder<?, ?> rightOperand) {
        return operateRight(operator, rightOperand.expression());
    }

    protected Metadata<B> operateRight(Operator operator, Expression rightOperand) {
        return operateRight(operator, List.of(rightOperand));
    }

    protected Metadata<B> operateRight(Operator operator, List<? extends Expression> rightOperand) {
        Metadata<B> res = metadata.clone0();
        res.right = ExpressionBuilders.operate(metadata.getRight(), operator, rightOperand);
        return res;
    }

    public Order<T> asc() {
        return OrderImpl.of(this, Order.SortOrder.ASC);
    }

    public Order<T> desc() {
        return OrderImpl.of(this, Order.SortOrder.DESC);
    }

    static class ComparableOpsImpl<T, U extends Comparable<U>, B> extends DefaultExpressionOperator<T, U, B> implements ComparableOperator<T, U, B> {

        public ComparableOpsImpl(Metadata<B> metadata) {
            super(metadata);
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
            return build(operateRight(Operator.BETWEEN, List.of(ExpressionBuilders.of(l), ExpressionBuilders.of(r))));
        }

        public B ge(ExpressionHolder<T, U> value) {
            return build(operateRight(Operator.GE, value));
        }

        public B gt(ExpressionHolder<T, U> value) {
            return build(operateRight(Operator.GT, value));
        }

        public B le(ExpressionHolder<T, U> value) {
            return build(operateRight(Operator.LE, value));
        }

        public B lt(ExpressionHolder<T, U> value) {
            return build(operateRight(Operator.LT, value));
        }

        public B between(ExpressionHolder<T, U> l, ExpressionHolder<T, U> r) {
            return build(operateRight(Operator.BETWEEN, List.of(ExpressionBuilders.of(l), ExpressionBuilders.of(r))));
        }

        public B between(ExpressionHolder<T, U> l, U r) {
            return build(operateRight(Operator.BETWEEN, List.of(ExpressionBuilders.of(l), ExpressionBuilders.of(r))));
        }

        public B between(U l, ExpressionHolder<T, U> r) {
            return build(operateRight(Operator.BETWEEN, List.of(ExpressionBuilders.of(l), ExpressionBuilders.of(r))));
        }


        @Override
        public B notBetween(U l, U r) {
            return build(operateRight(Operator.BETWEEN, List.of(ExpressionBuilders.of(l), ExpressionBuilders.of(r))).not());
        }

        @Override
        public B notBetween(ExpressionHolder<T, U> l, ExpressionHolder<T, U> r) {
            return build(operateRight(Operator.BETWEEN, List.of(ExpressionBuilders.of(l), ExpressionBuilders.of(r))).not());
        }

        @Override
        public B notBetween(ExpressionHolder<T, U> l, U r) {
            return build(operateRight(Operator.BETWEEN, List.of(ExpressionBuilders.of(l), ExpressionBuilders.of(r))).not());
        }

        @Override
        public B notBetween(U l, ExpressionHolder<T, U> r) {
            return build(operateRight(Operator.BETWEEN, List.of(ExpressionBuilders.of(l), ExpressionBuilders.of(r))).not());
        }
    }

    static class StringOpsImpl<T, B> extends ComparableOpsImpl<T, String, B> implements StringOperator<T, B> {

        public StringOpsImpl(Metadata<B> metadata) {
            super(metadata);
        }

        public B like(String value) {
            return build(operateRight(Operator.LIKE, value));
        }

        public StringOperator<T, B> lower() {
            return new StringOpsImpl<>(operateRight(Operator.LOWER, List.of()));
        }

        public StringOperator<T, B> upper() {
            return new StringOpsImpl<>(operateRight(Operator.UPPER, List.of()));
        }

        public StringOperator<T, B> substring(int a, int b) {
            return new StringOpsImpl<>(operateRight(Operator.SUBSTRING, List.of(ExpressionBuilders.of(a), ExpressionBuilders.of(b))));
        }

        public StringOperator<T, B> substring(int a) {
            return new StringOpsImpl<>(operateRight(Operator.SUBSTRING, List.of(ExpressionBuilders.of(a))));
        }

        public StringOperator<T, B> trim() {
            return new StringOpsImpl<>(operateRight(Operator.TRIM, List.of()));
        }

        public NumberOperator<T, Integer, B> length() {
            return new NumberOpsImpl<>(operateRight(Operator.LENGTH, List.of()));
        }

        @Override
        public B notLike(String value) {
            return build(operateRight(Operator.LIKE, value).not());
        }
    }

    static class NumberOpsImpl<T, U extends Number & Comparable<U>, B> extends ComparableOpsImpl<T, U, B> implements NumberOperator<T, U, B> {
        public NumberOpsImpl(Metadata<B> metadata) {
            super(metadata);
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
            return new NumberOpsImpl<>(operateRight(Operator.SUM, List.of()));
        }

        @Override
        public <V extends Number & Comparable<V>> NumberOperator<T, V, B> avg() {
            return new NumberOpsImpl<>(operateRight(Operator.AVG, List.of()));
        }

        @Override
        public <V extends Number & Comparable<V>> NumberOperator<T, V, B> max() {
            return new NumberOpsImpl<>(operateRight(Operator.MAX, List.of()));
        }

        @Override
        public <V extends Number & Comparable<V>> NumberOperator<T, V, B> min() {
            return new NumberOpsImpl<>(operateRight(Operator.MIN, List.of()));
        }
    }

    static class BooleanOpsImpl<T, B> extends ComparableOpsImpl<T, Boolean, B> implements BooleanOperator<T, B> {
        public BooleanOpsImpl(Metadata<B> metadata) {
            super(metadata);
        }

        public AndConnector<T> and(ExpressionHolder<T, Boolean> value) {
            Expression mt = expression();
            Expression expression = ExpressionBuilders.operate(mt, Operator.AND, value.expression());
            return new AndConnectorImpl<>(new Metadata<>(List.of(), TRUE, expression, AndConnectorImpl::new));
        }

        public OrConnector<T> or(ExpressionHolder<T, Boolean> value) {
            Expression mt = expression();
            Expression expression = ExpressionBuilders.operate(mt, Operator.OR, value.expression());
            return new OrConnectorImpl<>(new Metadata<>(List.of(), TRUE, expression, OrConnectorImpl::new));
        }

        public AndConnector<T> and(List<ExpressionHolder<T, Boolean>> values) {
            Expression mt = expression();
            Expression expression = ExpressionBuilders.operate(mt, Operator.AND, values.stream().map(ExpressionHolder::expression).toList());
            return new AndConnectorImpl<>(new Metadata<>(List.of(), TRUE, expression, AndConnectorImpl::new));
        }

        public OrConnector<T> or(List<ExpressionHolder<T, Boolean>> values) {
            Expression mt = expression();
            Expression expression = ExpressionBuilders.operate(mt, Operator.OR, values.stream().map(ExpressionHolder::expression).toList());
            return new OrConnectorImpl<>(new Metadata<>(List.of(), TRUE, expression, OrConnectorImpl::new));
        }

        @Override
        public Predicate<T> then() {
            return new PredicateOpsImpl<>(
                    new Metadata<>(List.of(), TRUE, merge(), PredicateOpsImpl::new));
        }

        public <R> PathOperator<T, R, OrConnector<T>> or(Path<T, R> path) {
            List<Expression> expressions = Util.concat(metadata.expressions, merge());
            return new DefaultExpressionOperator<>(new Metadata<>(expressions, TRUE, ExpressionBuilders.of(path), OrConnectorImpl::new));
        }

        public <R extends Comparable<R>> ComparableOperator<T, R, OrConnector<T>> or(ComparablePath<T, R> path) {
            List<Expression> expressions = Util.concat(metadata.expressions, merge());
            return new ComparableOpsImpl<>(new Metadata<>(expressions, TRUE, ExpressionBuilders.of(path), OrConnectorImpl::new));
        }

        public <R extends Number & Comparable<R>> NumberOperator<T, R, OrConnector<T>> or(NumberPath<T, R> path) {
            List<Expression> expressions = Util.concat(metadata.expressions, merge());
            return new NumberOpsImpl<>(new Metadata<>(expressions, TRUE, ExpressionBuilders.of(path), OrConnectorImpl::new));
        }

        public StringOperator<T, OrConnector<T>> or(StringPath<T> path) {
            List<Expression> expressions = Util.concat(metadata.expressions, merge());
            return new StringOpsImpl<>(new Metadata<>(expressions, TRUE, ExpressionBuilders.of(path), OrConnectorImpl::new));
        }

        public OrConnector<T> or(BooleanPath<T> path) {
            List<Expression> expressions = Util.concat(metadata.expressions, merge());
            return new OrConnectorImpl<>(new Metadata<>(expressions, TRUE, ExpressionBuilders.of(path), OrConnectorImpl::new));
        }

        public <R> PathOperator<T, R, AndConnector<T>> and(Path<T, R> path) {
            List<Expression> expressions = metadata.expressions;
            Expression left = merge();
            Expression right = ExpressionBuilders.of(path);
            return new DefaultExpressionOperator<>(new Metadata<>(expressions, left, right, AndConnectorImpl::new));
        }

        public <R extends Comparable<R>> ComparableOperator<T, R, AndConnector<T>> and(ComparablePath<T, R> path) {
            List<Expression> expressions = metadata.expressions;
            Expression left = merge();
            Expression right = ExpressionBuilders.of(path);
            return new ComparableOpsImpl<>(new Metadata<>(expressions, left, right, AndConnectorImpl::new));
        }

        public <R extends Number & Comparable<R>> NumberOperator<T, R, AndConnector<T>> and(NumberPath<T, R> path) {
            List<Expression> expressions = metadata.expressions;
            Expression left = merge();
            Expression right = ExpressionBuilders.of(path);
            return new NumberOpsImpl<>(new Metadata<>(expressions, left, right, AndConnectorImpl::new));
        }

        public StringOperator<T, AndConnector<T>> and(StringPath<T> path) {
            List<Expression> expressions = metadata.expressions;
            Expression left = merge();
            Expression right = ExpressionBuilders.of(path);
            return new StringOpsImpl<>(new Metadata<>(expressions, left, right, AndConnectorImpl::new));
        }

        public AndConnector<T> and(BooleanPath<T> path) {
            List<Expression> expressions = metadata.expressions;
            Expression left = merge();
            Expression right = ExpressionBuilders.of(path);
            return new AndConnectorImpl<>(new Metadata<>(expressions, left, right, AndConnectorImpl::new));
        }


    }

    static class OrConnectorImpl<T> extends BooleanOpsImpl<T, OrConnector<T>> implements OrConnector<T> {
        public OrConnectorImpl(Metadata<OrConnector<T>> metadata) {
            super(metadata);
        }
    }

    static class AndConnectorImpl<T> extends BooleanOpsImpl<T, AndConnector<T>> implements AndConnector<T> {
        public AndConnectorImpl(Metadata<AndConnector<T>> metadata) {
            super(metadata);
        }
    }

    static class PredicateOpsImpl<T> extends BooleanOpsImpl<T, Predicate<T>> implements Predicate<T> {
        public PredicateOpsImpl(Metadata<Predicate<T>> metadata) {
            super(metadata);
        }

        @Override
        public Predicate<T> not() {
            Expression expression = expression();
            Expression m = ExpressionBuilders.operate(expression, Operator.NOT);
            return new PredicateOpsImpl<>(new Metadata<>(List.of(), TRUE, m, PredicateOpsImpl::new));
        }


    }

    static class RootImpl<T> extends DefaultExpressionOperator<T, T, Predicate<T>> implements Root<T> {
        public RootImpl() {
            super(new Metadata<>(List.of(), TRUE, EMPTY_PATH, PredicateOpsImpl::new));
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
    static class Metadata<B> {
        List<Expression> expressions;
        Expression left;
        Expression right;
        BuilderFactory<B> builder;

        public Metadata<B> clone0() {
            return new Metadata<>(expressions, left, right, builder);
        }

        Metadata<B> not() {
            right = ExpressionBuilders.operate(right, Operator.NOT);
            return this;
        }

    }

    interface BuilderFactory<T> {
        T build(Metadata<T> metadata);

    }

    public static <T> PredicateOpsImpl<T> ofBoolOps(Expression expression) {
        return new PredicateOpsImpl<>(new Metadata<>(
                List.of(),
                TRUE,
                expression,
                PredicateOpsImpl::new
        ));
    }

    static class PathExpressionImpl<T, U>
            extends DefaultExpressionOperator<T, U, Predicate<T>>
            implements PathOperator<T, U, Predicate<T>> {
        public PathExpressionImpl(Metadata<? extends Predicate<T>> metadata) {
            super(metadata);
        }

        public @Override <V> PathOperator<T, V, Predicate<T>> get(Path<U, V> path) {
            return new PathExpressionImpl<>(toPaths(path));
        }

    }

    static class StringExpressionImpl<T>
            extends StringOpsImpl<T, Predicate<T>>
            implements StringOperator<T, Predicate<T>> {
        public StringExpressionImpl(Metadata<Predicate<T>> metadata) {
            super(metadata);
        }
    }

    static class NumberExpressionImpl<T, U extends Number & Comparable<U>>
            extends NumberOpsImpl<T, U, Predicate<T>>
            implements NumberOperator<T, U, Predicate<T>> {

        public NumberExpressionImpl(Metadata<Predicate<T>> metadata) {
            super(metadata);
        }
    }

    static class ComparableExpressionImpl<T, U extends Comparable<U>>
            extends ComparableOpsImpl<T, U, Predicate<T>>
            implements ComparableOperator<T, U, Predicate<T>> {
        public ComparableExpressionImpl(Metadata<Predicate<T>> metadata) {
            super(metadata);
        }
    }

}
