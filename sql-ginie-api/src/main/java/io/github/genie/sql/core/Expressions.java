package io.github.genie.sql.core;

import io.github.genie.sql.core.ExpressionBuilder.Expression;
import io.github.genie.sql.core.ExpressionBuilder.Paths;

import java.util.List;

public class Expressions {

    public static Paths ofPaths(List<String> strings) {
        return Metas.fromPaths(strings);
    }

    public static Paths ofPath(String fieldName) {
        return Metas.fromPaths(List.of(fieldName));
    }

    public static Paths concat(Paths join, String path) {
        return Metas.fromPaths(Util.concat(join.paths(), path));
    }

    public static boolean isTrue(Expression meta) {
        return Metas.isTrue(meta);
    }

    public static Expression operate(Expression meta,
                                     Operator operator,
                                     List<? extends Expression> metas) {
        return Metas.operate(meta, operator, metas);
    }


}
