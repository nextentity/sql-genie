package io.github.genie.sql.core;

import io.github.genie.sql.core.Expression.Meta;

import java.util.Collections;
import java.util.List;

interface Metas {

    static boolean isTrue(Meta meta) {
        return meta instanceof Expression.Constant constant
               && Boolean.TRUE.equals(constant.value());
    }

    static Meta of(Expression expression) {
        return expression.meta();
    }

    static Meta of(Object value) {
        return new BasicExpressions.ConstantExpression(value);
    }

    static Meta of(Meta value) {
        return value;
    }

    static Meta of(Path<?, ?> path) {
        String property = asString(path);
        return fromPath(property);
    }

    static String asString(Path<?, ?> path) {
        return GetterReferenceName.getPropertyName(path);
    }

    static List<Meta> ofList(Expression expression) {
        return List.of(of(expression));
    }

    static List<Meta> ofList(Object value) {
        return List.of(of(value));
    }

    static List<Meta> ofList(Meta value) {
        return List.of(value);
    }

    static List<Meta> ofList(Path<?, ?> path) {
        return List.of(of(path));
    }

    static Meta fromPath(String path) {
        List<String> paths = Collections.singletonList(path);
        return fromPaths(paths);
    }

    static Meta fromPaths(List<String> paths) {
        return new BasicExpressions.PathsExpression(paths);
    }

    static Meta operate(Expression l, Operator o, List<? extends Meta> r) {
        return new BasicExpressions.OperationExpression(l, o, r.stream().<Expression>map(it -> () -> it).toList());
    }


}
