package io.github.genie.sql.core;

import io.github.genie.sql.core.ExpressionOps.PathOps;
import io.github.genie.sql.core.Models.OrderingImpl;
import io.github.genie.sql.core.Path.BooleanPath;
import io.github.genie.sql.core.Path.ComparablePath;
import io.github.genie.sql.core.Path.NumberPath;
import io.github.genie.sql.core.Path.StringPath;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

class ExpressionBuilder<T, U, B> implements PathOps<T, U, B> {

    private static final Meta TRUE = Metas.TRUE;
    private static final Meta EMPTY_PATH = Metas.fromPaths(List.of());
    protected Metadata<B> metadata;

    @NotNull
    @AllArgsConstructor
    @Data
    static class Metadata<B> {
        List<Meta> expressions;
        Meta left;
        Meta right;
        BuilderFactory<B> builder;

        public Metadata<B> clone0() {
            return new Metadata<>(expressions, left, right, builder);
        }

    }

    public ExpressionBuilder(Metadata<? extends B> metadata) {
        // noinspection unchecked
        this.metadata = (Metadata<B>) metadata;
    }

    @Override
    public <V> PathOps<T, V, B> get(Path<U, V> path) {
        return new ExpressionBuilder<>(toPaths(path));
    }

    @Override
    public StringOps<T, B> get(StringPath<T> path) {
        return new StringOpsImpl<>(toPaths(path));
    }

    @Override
    public <V extends Number & Comparable<V>> NumberOps<T, V, B> get(NumberPath<T, V> path) {
        return new NumberOpsImpl<>(toPaths(path));
    }

    @Override
    public <V extends Comparable<V>> ComparableOps<T, V, B> get(ComparablePath<T, V> path) {
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
    public B eq(TypedExpression<T, U> value) {
        return build(operateRight(Operator.EQ, value));
    }

    @Override
    public B ne(U value) {
        return build(operateRight(Operator.NE, value));
    }

    @Override
    public B ne(TypedExpression<T, U> value) {
        return build(operateRight(Operator.NE, value));
    }

    @SafeVarargs
    @Override
    public final B in(U... values) {
        return build(operateRight(Operator.IN, Arrays.stream(values).map(Metas::of).toList()));
    }

    @Override
    public B in(@NotNull List<? extends TypedExpression<T, U>> values) {
        return build(operateRight(Operator.IN, values));
    }

    @Override
    public B isNull() {
        return build(operateRight(Operator.IS_NULL, List.of()));
    }

    @Override
    public NumberOps<T, Integer, B> count() {
        return new NumberOpsImpl<>(operateRight(Operator.COUNT, List.of()));
    }

    @Override
    public B isNotNull() {
        return build(operateRight(Operator.IS_NOT_NULL, List.of()));
    }


    public B and(TypedExpression<T, Boolean> value) {
        Meta mt = meta();
        Meta meta = Metas.operate(mt, Operator.AND, Metas.ofList(value));
        return build(new Metadata<>(List.of(), TRUE, meta, metadata.getBuilder()));
    }

    public B or(TypedExpression<T, Boolean> value) {
        Meta mt = meta();
        Meta meta = Metas.operate(mt, Operator.OR, Metas.ofList(value));
        return build(new Metadata<>(List.of(), TRUE, meta, metadata.getBuilder()));
    }

    @SafeVarargs
    public final B and(TypedExpression<T, Boolean>... values) {
        Meta mt = meta();
        Meta meta = Metas.operate(mt, Operator.AND, Arrays.stream(values).map(Expression::meta).toList());
        return build(new Metadata<>(List.of(), TRUE, meta, metadata.getBuilder()));
    }

    @SafeVarargs
    public final B or(TypedExpression<T, Boolean>... values) {
        Meta mt = meta();
        Meta meta = Metas.operate(mt, Operator.OR, Arrays.stream(values).map(Expression::meta).toList());
        return build(new Metadata<>(List.of(), TRUE, meta, metadata.getBuilder()));
    }

    public B and(List<TypedExpression<T, Boolean>> values) {
        Meta mt = meta();
        Meta meta = Metas.operate(mt, Operator.AND, values.stream().map(Expression::meta).toList());
        return build(new Metadata<>(List.of(), TRUE, meta, metadata.getBuilder()));
    }

    public B or(List<TypedExpression<T, Boolean>> values) {
        Meta mt = meta();
        Meta meta = Metas.operate(mt, Operator.OR, values.stream().map(Expression::meta).toList());
        return build(new Metadata<>(List.of(), TRUE, meta, metadata.getBuilder()));
    }

    public <R> PathOps<T, R, OrConnector<T>> or(Path<T, R> path) {
        List<Meta> expressions = Util.concat(metadata.expressions, merge());
        return new ExpressionBuilder<>(new Metadata<>(expressions, TRUE, Metas.of(path), OrConnectorImpl::new));
    }

    public <R extends Comparable<R>> ComparableOps<T, R, OrConnector<T>> or(ComparablePath<T, R> path) {
        List<Meta> expressions = Util.concat(metadata.expressions, merge());
        return new ComparableOpsImpl<>(new Metadata<>(expressions, TRUE, Metas.of(path), OrConnectorImpl::new));
    }

    public <R extends Number & Comparable<R>> NumberOps<T, R, OrConnector<T>> or(NumberPath<T, R> path) {
        List<Meta> expressions = Util.concat(metadata.expressions, merge());
        return new NumberOpsImpl<>(new Metadata<>(expressions, TRUE, Metas.of(path), OrConnectorImpl::new));
    }

    public StringOps<T, OrConnector<T>> or(StringPath<T> path) {
        List<Meta> expressions = Util.concat(metadata.expressions, merge());
        return new StringOpsImpl<>(new Metadata<>(expressions, TRUE, Metas.of(path), OrConnectorImpl::new));
    }

    public OrConnector<T> or(BooleanPath<T> path) {
        List<Meta> expressions = Util.concat(metadata.expressions, merge());
        return new OrConnectorImpl<>(new Metadata<>(expressions, TRUE, Metas.of(path), OrConnectorImpl::new));
    }

    public <R> PathOps<T, R, AndConnector<T>> and(Path<T, R> path) {
        List<Meta> expressions = metadata.expressions;
        Meta left = merge();
        Meta right = Metas.of(path);
        return new ExpressionBuilder<>(new Metadata<>(expressions, left, right, AndConnectorImpl::new));
    }

    public <R extends Comparable<R>> ComparableOps<T, R, AndConnector<T>> and(ComparablePath<T, R> path) {
        List<Meta> expressions = metadata.expressions;
        Meta left = merge();
        Meta right = Metas.of(path);
        return new ComparableOpsImpl<>(new Metadata<>(expressions, left, right, AndConnectorImpl::new));
    }

    public <R extends Number & Comparable<R>> NumberOps<T, R, AndConnector<T>> and(NumberPath<T, R> path) {
        List<Meta> expressions = metadata.expressions;
        Meta left = merge();
        Meta right = Metas.of(path);
        return new NumberOpsImpl<>(new Metadata<>(expressions, left, right, AndConnectorImpl::new));
    }

    public StringOps<T, AndConnector<T>> and(StringPath<T> path) {
        List<Meta> expressions = metadata.expressions;
        Meta left = merge();
        Meta right = Metas.of(path);
        return new StringOpsImpl<>(new Metadata<>(expressions, left, right, AndConnectorImpl::new));
    }

    public AndConnector<T> and(BooleanPath<T> path) {
        List<Meta> expressions = metadata.expressions;
        Meta left = merge();
        Meta right = Metas.of(path);
        return new AndConnectorImpl<>(new Metadata<>(expressions, left, right, AndConnectorImpl::new));
    }


    @Override
    public Meta meta() {
        Meta merge = merge();

        List<Meta> expressions = metadata.getExpressions();
        if (expressions.isEmpty()) {
            return merge;
        }
        Iterator<Meta> iterator = expressions.iterator();
        Meta l = iterator.next();
        List<Meta> r = new ArrayList<>(expressions.size());
        while (iterator.hasNext()) {
            r.add(iterator.next());
        }
        r.add(merge);
        return Metas.operate(l, Operator.OR, r);
    }

    private Meta merge() {
        return Metas.isTrue(metadata.left)
                ? metadata.right
                : Metas.operate(metadata.left, Operator.AND, List.of(metadata.right));
    }

    protected Metadata<B> toPaths(Path<?, ?> path) {
        Metadata<B> r = metadata.clone0();
        if (r.right == null) {
            r.right = Metas.of(path);
        }
        if (r.right instanceof Paths p) {
            List<String> paths = Util.concat(p.paths(), Metas.asString(path));
            r.right = Metas.fromPaths(paths);
        } else {
            throw new IllegalStateException();
        }
        return r;
    }

    protected B build(Metadata<B> metadata) {
        return this.metadata.builder.build(metadata);
    }

    protected Metadata<B> operateRight(Operator operator, Object rightOperand) {
        return operateRight(operator, Metas.of(rightOperand));
    }

    protected Metadata<B> operateRight(Operator operator, Expression rightOperand) {
        return operateRight(operator, rightOperand.meta());
    }

    protected Metadata<B> operateRight(Operator operator, Meta rightOperand) {
        return operateRight(operator, List.of(rightOperand));
    }

    protected Metadata<B> operateRight(Operator operator, List<? extends Meta> rightOperand) {
        Metadata<B> res = metadata.clone0();
        res.right = Metas.operate(metadata.getRight(), operator, rightOperand);
        return res;
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
        return build(operateRight(Operator.BETWEEN, List.of(Metas.of(l), Metas.of(r))));
    }

    public B ge(TypedExpression<T, U> value) {
        return build(operateRight(Operator.GE, value));
    }

    public B gt(TypedExpression<T, U> value) {
        return build(operateRight(Operator.GT, value));
    }

    public B le(TypedExpression<T, U> value) {
        return build(operateRight(Operator.LE, value));
    }

    public B lt(TypedExpression<T, U> value) {
        return build(operateRight(Operator.LT, value));
    }

    public B between(TypedExpression<T, U> l, TypedExpression<T, U> r) {
        return build(operateRight(Operator.BETWEEN, List.of(Metas.of(l), Metas.of(r))));
    }

    public B between(TypedExpression<T, U> l, U r) {
        return build(operateRight(Operator.BETWEEN, List.of(Metas.of(l), Metas.of(r))));
    }

    public B between(U l, TypedExpression<T, U> r) {
        return build(operateRight(Operator.BETWEEN, List.of(Metas.of(l), Metas.of(r))));
    }

    public B like(String value) {
        return build(operateRight(Operator.LIKE, value));
    }

    public StringOps<T, B> lower() {
        return new StringOpsImpl<>(operateRight(Operator.LOWER, List.of()));
    }

    public StringOps<T, B> upper() {
        return new StringOpsImpl<>(operateRight(Operator.UPPER, List.of()));
    }

    public StringOps<T, B> substring(int a, int b) {
        return new StringOpsImpl<>(operateRight(Operator.SUBSTRING, List.of(Metas.of(a), Metas.of(b))));
    }

    public StringOps<T, B> substring(int a) {
        return new StringOpsImpl<>(operateRight(Operator.SUBSTRING, List.of(Metas.of(a))));
    }

    public StringOps<T, B> trim() {
        return new StringOpsImpl<>(operateRight(Operator.TRIM, List.of()));
    }

    public NumberOps<T, Integer, B> length() {
        return new NumberOpsImpl<>(operateRight(Operator.LENGTH, List.of()));
    }

    public Ordering<T> asc() {
        return OrderingImpl.of(this, Ordering.SortOrder.ASC);
    }

    public Ordering<T> desc() {
        return OrderingImpl.of(this, Ordering.SortOrder.DESC);
    }

    static class ComparableOpsImpl<T, U extends Comparable<U>, B> extends ExpressionBuilder<T, U, B> implements ComparableOps<T, U, B> {

        public ComparableOpsImpl(Metadata<B> metadata) {
            super(metadata);
        }


    }

    static class StringOpsImpl<T, B> extends ComparableOpsImpl<T, String, B> implements StringOps<T, B> {

        public StringOpsImpl(Metadata<B> metadata) {
            super(metadata);
        }


    }

    static class NumberOpsImpl<T, U extends Number & Comparable<U>, B> extends ComparableOpsImpl<T, U, B> implements NumberOps<T, U, B> {
        public NumberOpsImpl(Metadata<B> metadata) {
            super(metadata);
        }


        @Override
        public NumberOps<T, U, B> add(U value) {
            return new NumberOpsImpl<>(operateRight(Operator.ADD, value));
        }

        @Override
        public NumberOps<T, U, B> subtract(U value) {
            return new NumberOpsImpl<>(operateRight(Operator.SUBTRACT, value));
        }

        @Override
        public NumberOps<T, U, B> multiply(U value) {
            return new NumberOpsImpl<>(operateRight(Operator.MULTIPLY, value));
        }

        @Override
        public NumberOps<T, U, B> divide(U value) {
            return new NumberOpsImpl<>(operateRight(Operator.DIVIDE, value));
        }

        @Override
        public NumberOps<T, U, B> mod(U value) {
            return new NumberOpsImpl<>(operateRight(Operator.MOD, value));
        }

        @Override
        public NumberOps<T, U, B> add(TypedExpression<T, U> value) {
            return new NumberOpsImpl<>(operateRight(Operator.ADD, value));
        }

        @Override
        public NumberOps<T, U, B> subtract(TypedExpression<T, U> value) {
            return new NumberOpsImpl<>(operateRight(Operator.SUBTRACT, value));
        }

        @Override
        public NumberOps<T, U, B> multiply(TypedExpression<T, U> value) {
            return new NumberOpsImpl<>(operateRight(Operator.MULTIPLY, value));
        }

        @Override
        public NumberOps<T, U, B> divide(TypedExpression<T, U> value) {
            return new NumberOpsImpl<>(operateRight(Operator.DIVIDE, value));
        }

        @Override
        public NumberOps<T, U, B> mod(TypedExpression<T, U> value) {
            return new NumberOpsImpl<>(operateRight(Operator.MOD, value));
        }

        @Override
        public <V extends Number & Comparable<V>> NumberOps<T, V, B> sum() {
            return new NumberOpsImpl<>(operateRight(Operator.SUM, List.of()));
        }

        @Override
        public <V extends Number & Comparable<V>> NumberOps<T, V, B> avg() {
            return new NumberOpsImpl<>(operateRight(Operator.AVG, List.of()));
        }

        @Override
        public <V extends Number & Comparable<V>> NumberOps<T, V, B> max() {
            return new NumberOpsImpl<>(operateRight(Operator.MAX, List.of()));
        }

        @Override
        public <V extends Number & Comparable<V>> NumberOps<T, V, B> min() {
            return new NumberOpsImpl<>(operateRight(Operator.MIN, List.of()));
        }
    }

    static class BaseBoolOpsImpl<T, B> extends ExpressionBuilder<T, Boolean, B> implements BooleanOps<T, B> {
        public BaseBoolOpsImpl(Metadata<B> metadata) {
            super(metadata);
        }
    }

    static class OrConnectorImpl<T> extends BaseBoolOpsImpl<T, OrConnector<T>> implements OrConnector<T> {
        public OrConnectorImpl(Metadata<OrConnector<T>> metadata) {
            super(metadata);
        }
    }

    static class AndConnectorImpl<T> extends BaseBoolOpsImpl<T, AndConnector<T>> implements AndConnector<T> {
        public AndConnectorImpl(Metadata<AndConnector<T>> metadata) {
            super(metadata);
        }
    }

    static class BoolOpsImpl<T> extends BaseBoolOpsImpl<T, PredicateOps<T>> implements PredicateOps<T> {
        public BoolOpsImpl(Metadata<PredicateOps<T>> metadata) {
            super(metadata);
        }

        @Override
        public PredicateOps<T> not() {
            Meta meta = meta();
            Meta m = Metas.operate(meta, Operator.NOT, List.of());
            return new BoolOpsImpl<>(new Metadata<>(List.of(), TRUE, m, BoolOpsImpl::new));
        }


    }

    static class RootImpl<T> extends ExpressionBuilder<T, T, PredicateOps<T>> implements RootPath<T> {
        public RootImpl() {
            super(new Metadata<>(List.of(), TRUE, EMPTY_PATH, BoolOpsImpl::new));
        }

    }

    record TypedExpressionImpl<T, U>(Meta meta) implements TypedExpression<T, U> {
    }

    record PathExpressionImpl<T, U>(Paths meta) implements PathExpression<T, U> {
        public <V> PathExpression<T, V> get(Path<U, V> path) {
            return new PathExpressionImpl<>(Metas.fromPaths(
                    Util.concat(meta().paths(), Metas.asString(path))
            ));
        }
    }

    interface BuilderFactory<T> {
        T build(Metadata<T> metadata);

    }

    public static <T> BoolOpsImpl<T> ofBoolOps(Meta meta) {
        return new BoolOpsImpl<>(new Metadata<>(
                List.of(),
                TRUE,
                meta,
                BoolOpsImpl::new
        ));
    }


}
