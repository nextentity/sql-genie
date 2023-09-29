package io.github.genie.sql.core;

import io.github.genie.sql.core.Expression.Meta;
import io.github.genie.sql.core.Expression.TypedExpression;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

interface Metas {

    Meta TRUE = Metas.of(true);

    static boolean isTrue(Meta meta) {
        return meta instanceof Expression.Constant constant
               && Boolean.TRUE.equals(constant.value());
    }

    static Meta of(Expression expression) {
        return expression.meta();
    }

    static Meta of(Object value) {
        return new Models.ConstantMeta(value);
    }

    static Meta of(Meta value) {
        return value;
    }

    static Expression.Paths of(Path<?, ?> path) {
        String property = asString(path);
        return fromPath(property);
    }

    static String asString(Path<?, ?> path) {
        return Util.getPropertyName(path);
    }


    static <T, U> TypedExpression<T, U> toExpression(Meta meta) {
        return new ExpressionBuilder.TypedExpressionImpl<>(meta);
    }

    static List<Meta> ofList(Expression expression) {
        return List.of(of(expression));
    }

    static Expression.Paths fromPath(String path) {
        List<String> paths = Collections.singletonList(path);
        return fromPaths(paths);
    }

    static Expression.Paths fromPaths(List<String> paths) {
        return new Models.PathsMeta(paths);
    }

    static Meta operate(Meta l, Operator o, List<? extends Meta> r) {
        return new Models.OperationMeta(l, o, r);
    }

    static <T> List<TypedExpression<T, ?>> toExpressionList(Path<?, ?>... paths) {
        return Arrays.stream(paths)
                .<Meta>map(Metas::of)
                .<TypedExpression<T, ?>>map(Metas::toExpression)
                .toList();
    }


}
