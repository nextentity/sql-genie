package io.github.genie.sql.core;

import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ExpressionBuilders<T, U> implements
        PathVisitor<T, U>,
        OperateableExpression<T, U>,
        BasicExpressions.CustomizerOperator {
    public static final Paths EMPTY_PATH = BasicExpressions.of(List.of());

    List<Expression> expressions = List.of();
    Expression left = () -> BasicExpressions.TRUE;
    Expression right = () -> EMPTY_PATH;

    static ExpressionBuilders<?, ?> and(Expression mixin, Path<?, ?> path) {
        return and(mixin, path, newInstance(path));
    }

    static ExpressionBuilders<?, ?> and(Expression mixin, Path<?, ?> path, ExpressionBuilders<?, ?> out) {
        if (mixin instanceof ExpressionBuilders<?, ?> p) {
            out.expressions = p.expressions;
            out.left = p.merge();
        } else {
            out.left = mixin;
        }
        out.right = () -> BasicExpressions.of(path);
        return out;
    }

    static ExpressionBuilders<?, ?> or(Expression mixin, Path<?, ?> path) {
        return or(mixin, path, newInstance(path));
    }

    public static ExpressionBuilders<?, ?> newInstance(Path<?, ?> path) {
        ExpressionBuilders<?, ?> out;
        if (path instanceof Path.StringPath) {
            out = new StringOpt<>();
        } else if (path instanceof Path.NumberPath) {
            out = new NumberOpt<>();
        } else if (path instanceof Path.BooleanPath) {
            out = new BooleanOpt<>();
        } else if (path instanceof Path.ComparablePath) {
            return new ComparableOpt<>();
        } else {
            out = new ExpressionBuilders<>();
        }
        return out;
    }

    static ExpressionBuilders<?, ?> or(Expression mixin, Path<?, ?> path, ExpressionBuilders<?, ?> out) {
        if (mixin instanceof ExpressionBuilders<?, ?> p) {
            out.expressions = Util.concat(p.expressions, p.merge());
        } else {
            out.expressions = List.of(mixin);
        }
        out.right = () -> BasicExpressions.of(path);
        return out;
    }


    @Override
    public Meta meta() {
        Expression merge = merge();
        if (expressions.isEmpty()) {
            return merge.meta();
        }
        Iterator<Expression> iterator = expressions.iterator();
        Expression l = iterator.next();
        List<Expression> r = new ArrayList<>(expressions.size());
        while (iterator.hasNext()) {
            r.add(iterator.next());
        }
        r.add(merge);
        return BasicExpressions.operate(l, Operator.OR, r);
    }


    @Override
    public <R> BooleanExpression<R> operateAsPredicate(Operator operator,
                                                       List<? extends Expression> rightOperand) {
        Meta basic = operateRight(operator, rightOperand);
        return new BooleanOpt<>(expressions, left, () -> basic);
    }

    @Override
    public <X, Y extends Comparable<Y>>
    ComparableExpression<X, Y> operateAsOperableComparable(Operator operator,
                                                           List<? extends Expression> rightOperand) {
        Meta basic = operateRight(operator, rightOperand);
        return new ComparableOpt<>(expressions, left, () -> basic);
    }

    @Override
    public <X> StringExpression<X> operateAsOperableString(Operator operator,
                                                           List<? extends Expression> rightOperand) {
        Meta basic = operateRight(operator, rightOperand);
        return new StringOpt<>(expressions, left, () -> basic);
    }

    @Override
    public <X, Y extends Number & Comparable<Y>>
    NumberExpression<X, Y> operateAsOperableNumber(Operator operator,
                                                   List<? extends Expression> rightOperand) {
        Meta basic = operateRight(operator, rightOperand);
        return new NumberOpt<>(expressions, left, () -> basic);
    }

    Expression merge() {
        return BasicExpressions.isTrue(left) ? right : () -> BasicExpressions.operate(left, Operator.AND, List.of(right));
    }


    static <T> ExpressionBuilderImpl<T> of() {
        return Util.cast(ExpressionBuilderImpl.EMPTY);
    }

    private ExpressionBuilders() {
    }

    public ExpressionBuilders(List<Expression> expressions, Expression left, Expression right) {
        this.expressions = expressions;
        this.left = left;
        this.right = right;
    }

    @Override
    public <V, R extends PathVisitor<T, V> & OperateableExpression<T, V>> R get(Path<U, V> path) {
        return Util.cast(new ExpressionBuilders<>(expressions, left, () -> toPaths(path)));
    }

    @Override
    public StringExpression<T> get(Path.StringPath<T> path) {
        return new StringOpt<>(expressions, left, () -> toPaths(path));
    }


    @Override
    public <V extends Number & Comparable<V>> NumberExpression<T, V> get(Path.NumberPath<T, V> path) {
        return new NumberOpt<>(expressions, left, () -> toPaths(path));
    }

    @Override
    public <V extends Comparable<V>> ComparableExpression<T, V> get(Path.ComparablePath<T, V> path) {
        return new ComparableOpt<>(expressions, left, () -> toPaths(path));
    }

    @Override
    public BooleanExpression<T> get(Path.BooleanPath<T> path) {
        return new BooleanOpt<>(expressions, left, () -> toPaths(path));
    }

    private Paths toPaths(Path<?, ?> path) {
        if (this.right == null) {
            this.right = () -> EMPTY_PATH;
        }
        Meta basic = right.meta();
        if (basic instanceof Paths p) {
            return BasicExpressions.concat(p, path);
        }
        System.out.println(basic);
        throw new IllegalStateException();
    }

    @NotNull
    private Expression.Meta operateRight(Operator operator, List<? extends Expression> rightOperand) {
        return BasicExpressions.operate(right, operator, rightOperand);
    }


    @NoArgsConstructor
    static class ComparableOpt<T, U extends Comparable<U>>
            extends ExpressionBuilders<T, U>
            implements ComparableExpression<T, U> {
        public ComparableOpt(List<Expression> expressions, Expression left, Expression right) {
            super(expressions, left, right);
        }
    }

    @NoArgsConstructor
    static class BooleanOpt<T>
            extends ExpressionBuilders<T, Boolean>
            implements BooleanExpression<T> {
        public BooleanOpt(List<Expression> expressions, Expression left, Expression right) {
            super(expressions, left, right);
        }

        @Override
        public BooleanExpression<T> not() {
            return () -> BasicExpressions.operate(this, Operator.NOT, List.of());
        }
    }

    @NoArgsConstructor
    static class StringOpt<T> extends ExpressionBuilders<T, String> implements StringExpression<T> {
        public StringOpt(List<Expression> expressions, Expression left, Expression right) {
            super(expressions, left, right);
        }

    }

    @NoArgsConstructor
    static class NumberOpt<T, U extends Number & Comparable<U>> extends ExpressionBuilders<T, U>
            implements NumberExpression<T, U> {
        public NumberOpt(List<Expression> expressions, Expression left, Expression right) {
            super(expressions, left, right);
        }
    }

    static class ExpressionBuilderImpl<T>
            extends ExpressionBuilders<T, T>
            implements RootPath<T> {
        private static final ExpressionBuilderImpl<?> EMPTY = new ExpressionBuilderImpl<>();

        public ExpressionBuilderImpl() {
        }

    }


    @Override
    public String toString() {
        return meta().toString();
    }
}
