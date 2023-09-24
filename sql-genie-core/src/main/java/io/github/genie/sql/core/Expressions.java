package io.github.genie.sql.core;

import java.util.List;

public class Expressions {

    public static Expression.Paths ofPaths(List<String> strings) {
        return BasicExpressions.of(strings);
    }

    public static Expression.Paths ofPath(String fieldName) {
        return BasicExpressions.of(List.of(fieldName));
    }

    public static Expression.Paths concat(Expression.Paths join, String path) {
        return BasicExpressions.concat(join, path);
    }

    public static boolean isTrue(Predicate<?> predicate) {
        return BasicExpressions.isTrue(predicate);
    }

    public static boolean isTrue(Expression.Meta predicate) {
        return BasicExpressions.isTrue(predicate);
    }

    public static Expression.Meta operate(Expression expression,
                                          Operator operator,
                                          List<? extends Expression> expressions) {
        return BasicExpressions.operate(expression, operator, expressions);
    }


}
