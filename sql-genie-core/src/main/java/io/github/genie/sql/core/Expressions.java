package io.github.genie.sql.core;

import io.github.genie.sql.core.Expression.Meta;
import io.github.genie.sql.core.Expression.Paths;

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

    public static boolean isTrue(Predicate<?> predicate) {
        return Metas.isTrue(predicate.meta());
    }

    public static boolean isTrue(Meta predicate) {
        return Metas.isTrue(predicate);
    }

    public static Meta operate(Meta meta,
                               Operator operator,
                               List<? extends Meta> metas) {
        return Metas.operate(meta, operator, metas);
    }


}
