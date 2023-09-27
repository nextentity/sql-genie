package io.github.genie.sql.core;

import io.github.genie.sql.core.ExpressionChainBuilder.*;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

class PathOperationImpl<T, U, B> implements PathOperation<T, U, B>, CommonOperation<T, U, B>, CustomizerOperator {

    public static final Meta EMPTY_PATH = Metas.fromPaths(List.of());

    List<Meta> expressions = List.of();
    Meta left = Metas.of(true);
    Meta right = EMPTY_PATH;
    boolean and = true;

    static PathOperationImpl<?, ?, ?> and(Meta mixin, Path<?, ?> path) {
        return and(mixin, path, newInstance(path, Operator.OR));
    }

    static PathOperationImpl<?, ?, ?> and(Meta mixin, Path<?, ?> path, PathOperationImpl<?, ?, ?> out) {
        if (mixin instanceof PathOperationImpl<?, ?, ?> p) {
            out.expressions = p.expressions;
            out.left = p.merge();
        } else {
            out.left = mixin;
        }
        out.right = Metas.of(path);
        return out;
    }

    static PathOperationImpl<?, ?, ?> or(Meta mixin, Path<?, ?> path) {
        return or(mixin, path, newInstance(path, Operator.AND));
    }

    public static PathOperationImpl<?, ?, ?> newInstance(Path<?, ?> path, Operator operator) {
        PathOperationImpl<?, ?, ?> out;
        if (path instanceof Path.StringPath) {
            out = new StringOpt<>();
        } else if (path instanceof Path.NumberPath) {
            out = new NumberOpt<>();
        } else if (path instanceof Path.BooleanPath) {
            out = new BooleanOpt<>();
        } else if (path instanceof Path.ComparablePath) {
            return new ComparableOpt<>();
        } else {
            out = new PathOperationImpl<>();
        }
        return out;
    }

    static PathOperationImpl<?, ?, ?> or(Meta mixin, Path<?, ?> path, PathOperationImpl<?, ?, ?> out) {
        if (mixin instanceof PathOperationImpl<?, ?, ?> p) {
            out.expressions = Util.concat(p.expressions, p.merge());
        } else {
            out.expressions = List.of(mixin);
        }
        out.right = Metas.of(path);
        out.and = false;
        return out;
    }


    @Override
    public Meta meta() {
        Meta merge = merge();
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
        return Metas.operate(() -> l, Operator.OR, r);
    }

    @Override
    public <X, Y> BooleanOperation<X, Y> operateAsBoolean(Operator operator, List<? extends Meta> rightOperand) {
        Meta basic = operateRight(operator, rightOperand);
        var r = and
                ? new LogicAndConnectorImpl<>(expressions, left, basic)
                : new LogicOrConnectorImpl<>(expressions, left, basic);
        // noinspection unchecked
        return (BooleanOperation<X, Y>) r;
    }

    @Override
    public <X, Y> StringOperation<X, Y> operateAsString(Operator operator, List<? extends Meta> rightOperand) {
        Meta basic = operateRight(operator, rightOperand);
        return new StringOpt<>(expressions, left, basic);
    }


    @Override
    public <X, Y extends Number & Comparable<Y>, Z> NumberOperation<X, Y, Z>
    operateAsNumber(Operator operator, List<? extends Meta> rightOperand) {
        Meta basic = operateRight(operator, rightOperand);
        return new NumberOpt<>(expressions, left, basic);
    }

    Meta merge() {
        return Metas.isTrue(left) ? right : Metas.operate(() -> left, Operator.AND, List.of(right));
    }


    static <T, B> Root<T, B> of() {
        return Util.cast(ExpressionBuilderImpl.EMPTY);
    }

    private PathOperationImpl() {
    }

    public PathOperationImpl(List<Meta> expressions, Meta left, Meta right) {
        this.expressions = expressions;
        this.left = left;
        this.right = right;
    }


    @Override
    public <V, R extends PathOperation<T, V, B> & CommonOperation<T, V, B>> R get(Path<U, V> path) {
        return Util.cast(new PathOperationImpl<>(expressions, left, toPaths(path)));
    }

    @Override
    public StringOperation<T, B> get(Path.StringPath<T> path) {
        return new StringOpt<>(expressions, left, toPaths(path));
    }

    @Override
    public <V extends Number & Comparable<V>> NumberOperation<T, V, B> get(Path.NumberPath<T, V> path) {
        return new NumberOpt<>(expressions, left, toPaths(path));
    }

    @Override
    public <V extends Comparable<V>> ComparableOperation<T, V, B> get(Path.ComparablePath<T, V> path) {
        return new ComparableOpt<>(expressions, left, toPaths(path));
    }

    @Override
    public BooleanOperation<T, B> get(Path.BooleanPath<T> path) {
        return new BooleanOpt<>(expressions, left, toPaths(path));
    }

    private Meta toPaths(Path<?, ?> path) {
        if (this.right == null) {
            this.right = EMPTY_PATH;
        }
        Meta basic = right;
        if (basic instanceof Paths p) {
            List<String> paths = Util.concat(p.paths(), Metas.asString(path));
            return Metas.fromPaths(paths);
        }
        throw new IllegalStateException();
    }

    @NotNull
    private Meta operateRight(Operator operator, List<? extends Meta> rightOperand) {
        return Metas.operate(() -> right, operator, rightOperand);
    }


    @NoArgsConstructor
    static class ComparableOpt<T, U extends Comparable<U>, B>
            extends PathOperationImpl<T, U, B>
            implements ComparableOperation<T, U, B> {
        public ComparableOpt(List<Meta> expressions, Meta left, Meta right) {
            super(expressions, left, right);
        }
    }

    @NoArgsConstructor
    static class BooleanOpt<T, B>
            extends PathOperationImpl<T, Boolean, B>
            implements BooleanOperation<T, B> {
        public BooleanOpt(List<Meta> expressions, Meta left, Meta right) {
            super(expressions, left, right);
        }
    }

    static class LogicAndConnectorImpl<T>
            extends BooleanOpt<T, LogicAndConnector<T>>
            implements LogicAndConnector<T> {
        public LogicAndConnectorImpl(List<Meta> expressions, Meta left, Meta right) {
            super(expressions, left, right);
        }
    }

    static class LogicOrConnectorImpl<T>
            extends BooleanOpt<T, LogicOrConnector<T>>
            implements LogicOrConnector<T> {
        public LogicOrConnectorImpl(List<Meta> expressions, Meta left, Meta right) {
            super(expressions, left, right);
        }
    }


    @NoArgsConstructor
    static class StringOpt<T, B> extends PathOperationImpl<T, String, B> implements StringOperation<T, B> {
        public StringOpt(List<Meta> expressions, Meta left, Meta right) {
            super(expressions, left, right);
        }
    }

    @NoArgsConstructor
    static class NumberOpt<T, U extends Number & Comparable<U>, B> extends PathOperationImpl<T, U, B>
            implements NumberOperation<T, U, B> {
        public NumberOpt(List<Meta> expressions, Meta left, Meta right) {
            super(expressions, left, right);
        }
    }

    static class ExpressionBuilderImpl<T, B>
            extends PathOperationImpl<T, T, B>
            implements Root<T, B> {
        private static final ExpressionBuilderImpl<?, ?> EMPTY = new ExpressionBuilderImpl<>();

        public ExpressionBuilderImpl() {
        }

    }


    @Override
    public String toString() {
        return meta().toString();
    }

}
    
