package io.github.genie.sql.builder;

import io.github.genie.sql.api.EntityRoot;
import io.github.genie.sql.api.Expression;
import io.github.genie.sql.api.ExpressionHolder;
import io.github.genie.sql.api.Operation;
import io.github.genie.sql.api.Path;
import io.github.genie.sql.api.Path.BooleanPath;
import io.github.genie.sql.api.Path.ComparablePath;
import io.github.genie.sql.api.Path.NumberPath;
import io.github.genie.sql.api.Path.StringPath;
import io.github.genie.sql.api.TypedExpression.BooleanExpression;
import io.github.genie.sql.api.TypedExpression.ComparableExpression;
import io.github.genie.sql.api.TypedExpression.NumberExpression;
import io.github.genie.sql.api.TypedExpression.PathExpression;
import io.github.genie.sql.api.TypedExpression.StringExpression;
import io.github.genie.sql.builder.TypedExpressionImpl.BooleanExpressionImpl;
import io.github.genie.sql.builder.TypedExpressionImpl.ComparableExpressionImpl;
import io.github.genie.sql.builder.TypedExpressionImpl.NumberExpressionImpl;
import io.github.genie.sql.builder.TypedExpressionImpl.PathExpressionImpl;
import io.github.genie.sql.builder.TypedExpressionImpl.StringExpressionImpl;

public class EntityRootImpl<T> implements EntityRoot<T> {

    private static final EntityRootImpl<?> INSTANCE = new EntityRootImpl<>();

    public static <T> EntityRoot<T> of() {
        return TypeCastUtil.cast(INSTANCE);
    }

    protected EntityRootImpl() {
    }

    @Override
    public <U> ExpressionHolder<T, U> of(U value) {
        return ExpressionHolders.of(value);
    }

    @Override
    public <U> PathExpression<T, U> get(Path<T, U> path) {
        return new PathExpressionImpl<>((Operation) null, Expressions.of(path));
    }

    @Override
    public StringExpression<T> get(StringPath<T> path) {
        return new StringExpressionImpl<>((Operation) null, Expressions.of(path));
    }

    @Override
    public <U extends Number & Comparable<U>> NumberExpression<T, U> get(NumberPath<T, U> path) {
        return new NumberExpressionImpl<>((Operation) null, Expressions.of(path));
    }

    @Override
    public <U extends Comparable<U>> ComparableExpression<T, U> get(ComparablePath<T, U> path) {
        return new ComparableExpressionImpl<>((Operation) null, Expressions.of(path));
    }

    @Override
    public BooleanExpression<T> get(BooleanPath<T> path) {
        return new BooleanExpressionImpl<>((Operation) null, Expressions.of(path));
    }

    static <T> BooleanExpression<T> ofBooleanExpression(Expression expression) {
        return new BooleanExpressionImpl<>((Operation) null, expression);
    }

}
