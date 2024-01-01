package io.github.genie.sql.builder;

import io.github.genie.sql.api.Column;
import io.github.genie.sql.api.Expression;
import io.github.genie.sql.api.Operator;

import java.util.Collections;
import java.util.List;

public class Expressions {

    public static Column ofPaths(List<String> strings) {
        return ExpressionBuilders.fromPaths(strings);
    }

    public static Column ofPath(String fieldName) {
        return ExpressionBuilders.fromPaths(Collections.singletonList(fieldName));
    }

    public static Column concat(Column join, String path) {
        return ExpressionBuilders.fromPaths(Util.concat(join.paths(), path));
    }

    public static boolean isTrue(Expression expression) {
        return ExpressionBuilders.isTrue(expression);
    }

    public static Expression operate(Expression expression,
                                     Operator operator,
                                     List<? extends Expression> expressions) {
        return ExpressionBuilders.operate(expression, operator, expressions);
    }


}
