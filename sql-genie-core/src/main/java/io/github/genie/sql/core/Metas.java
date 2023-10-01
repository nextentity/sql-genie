package io.github.genie.sql.core;

import io.github.genie.sql.core.Expression.*;
import io.github.genie.sql.core.ExpressionBuilder.PathExpressionImpl;
import io.github.genie.sql.core.ExpressionBuilder.TypedExpressionImpl;
import io.github.genie.sql.core.Models.ConstantMeta;
import io.github.genie.sql.core.Models.OperationMeta;
import io.github.genie.sql.core.Models.PathsMeta;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

interface Metas {

    Meta TRUE = Metas.of(true);

    static boolean isTrue(Meta meta) {
        return meta instanceof Constant constant
               && Boolean.TRUE.equals(constant.value());
    }

    static Meta of(Expression expression) {
        return expression.meta();
    }

    static Meta of(Object value) {
        return new ConstantMeta(value);
    }

    static Meta of(Meta value) {
        return value;
    }

    static Paths of(Path<?, ?> path) {
        String property = asString(path);
        return fromPath(property);
    }

    static String asString(Path<?, ?> path) {
        return Util.getPropertyName(path);
    }


    static List<Meta> ofList(Expression expression) {
        return List.of(of(expression));
    }

    static Paths fromPath(String path) {
        List<String> paths = Collections.singletonList(path);
        return fromPaths(paths);
    }

    static Paths fromPaths(List<String> paths) {
        return new PathsMeta(paths);
    }

    static Meta operate(Meta l, Operator o, List<? extends Meta> r) {
        if (o.isMultivalued() && l instanceof Operation lo && lo.operator() == o) {
            List<Meta> args = Util.concat(lo.args(), r);
            return new OperationMeta(lo.operand(), o, args);
        }
        return new OperationMeta(l, o, r);
    }

    static <T> List<PathExpression<T, ?>> toExpressionList(Path<?, ?>... paths) {
        return Arrays.stream(paths)
                .map(Metas::of)
                .<PathExpression<T, ?>>map(PathExpressionImpl::new)
                .toList();
    }


}
