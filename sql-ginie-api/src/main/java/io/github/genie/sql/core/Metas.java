package io.github.genie.sql.core;

import io.github.genie.sql.core.ExpressionBuilder.Constant;
import io.github.genie.sql.core.ExpressionBuilder.Expression;
import io.github.genie.sql.core.ExpressionBuilder.Operation;
import io.github.genie.sql.core.ExpressionBuilder.Paths;
import io.github.genie.sql.core.ExpressionOperator.PathOperator;
import io.github.genie.sql.core.ExpressionOperator.Predicate;
import io.github.genie.sql.core.Models.ConstantMeta;
import io.github.genie.sql.core.Models.OperationMeta;
import io.github.genie.sql.core.Models.PathsMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public interface Metas {

    Expression TRUE = Metas.of(true);

    static boolean isTrue(Expression meta) {
        return meta instanceof Constant constant
               && Boolean.TRUE.equals(constant.value());
    }

    static Expression of(ExpressionBuilder<?, ?> expression) {
        return expression.build();
    }

    static Expression of(Object value) {
        return new ConstantMeta(value);
    }

    static Expression of(Expression value) {
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

    static Expression operate(Expression l, Operator o, Expression r) {
        if (o.isMultivalued() && l instanceof Operation lo && lo.operator() == o) {
            List<Expression> args = Util.concat(lo.args(), r);
            return new OperationMeta(lo.operand(), o, args);
        }
        return new OperationMeta(l, o, List.of(r));
    }

    static Expression operate(Expression l, Operator o) {
        return operate(l, o, List.of());
    }

    static Expression operate(Expression l, Operator o, List<? extends Expression> r) {
        if (o == Operator.NOT
            && l instanceof Operation operation
            && operation.operator() == Operator.NOT) {
            return operation.operand();
        }
        if (o.isMultivalued() && l instanceof Operation lo && lo.operator() == o) {
            List<Expression> args = Util.concat(lo.args(), r);
            return new OperationMeta(lo.operand(), o, args);
        }
        return new OperationMeta(l, o, r);
    }

    @SafeVarargs
    static <T> List<PathOperator<T, ?, Predicate<T>>> toExpressionList(Path<T, ?>... paths) {
        return Arrays.stream(paths)
                .<PathOperator<T, ?, Predicate<T>>>map(Q::get)
                .toList();
    }


}
