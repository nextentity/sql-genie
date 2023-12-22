package io.github.genie.sql.core;

import io.github.genie.sql.core.Expression.Constant;
import io.github.genie.sql.core.Expression.Meta;
import io.github.genie.sql.core.Expression.Operation;
import io.github.genie.sql.core.Expression.Paths;
import io.github.genie.sql.core.ExpressionOps.PathExpr;
import io.github.genie.sql.core.Models.ConstantMeta;
import io.github.genie.sql.core.Models.OperationMeta;
import io.github.genie.sql.core.Models.PathsMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public interface Metas {

    Meta TRUE = Metas.of(true);

    static boolean isTrue(Meta meta) {
        return meta instanceof Constant constant
               && Boolean.TRUE.equals(constant.value());
    }

    static Meta of(Expression<?, ?> expression) {
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


    static Paths fromPath(String path) {
        List<String> paths = new ArrayList<>(1);
        paths.add(path);
        return fromPaths(paths);
    }

    static Paths fromPaths(List<String> paths) {
        Objects.requireNonNull(paths);
        if (paths.getClass() != ArrayList.class) {
            paths = new ArrayList<>(paths);
        }
        return new PathsMeta(paths);
    }

    static Meta operate(Meta l, Operator o, Meta r) {
        if (o.isMultivalued() && l instanceof Operation lo && lo.operator() == o) {
            List<Meta> args = Util.concat(lo.args(), r);
            return new OperationMeta(lo.operand(), o, args);
        }
        return new OperationMeta(l, o, List.of(r));
    }

    static Meta operate(Meta l, Operator o) {
        return operate(l, o, List.of());
    }

    static Meta operate(Meta l, Operator o, List<? extends Meta> r) {
        if (o == Operator.NOT
            && l instanceof Operation operation
            && operation.operator() == Operator.NOT) {
            return operation.operand();
        }
        if (o.isMultivalued() && l instanceof Operation lo && lo.operator() == o) {
            List<Meta> args = Util.concat(lo.args(), r);
            return new OperationMeta(lo.operand(), o, args);
        }
        return new OperationMeta(l, o, r);
    }

    @SafeVarargs
    static <T> List<PathExpr<T, ?>> toExpressionList(Path<T, ?>... paths) {
        return Arrays.stream(paths)
                .<PathExpr<T, ?>>map(Q::get)
                .toList();
    }


}
