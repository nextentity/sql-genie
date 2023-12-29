package io.github.genie.sql.builder;

import io.github.genie.sql.api.Column;
import io.github.genie.sql.api.Constant;
import io.github.genie.sql.api.Expression;
import io.github.genie.sql.api.ExpressionHolder;
import io.github.genie.sql.api.ExpressionOperator.PathOperator;
import io.github.genie.sql.api.ExpressionOperator.Predicate;
import io.github.genie.sql.api.Operation;
import io.github.genie.sql.api.Operator;
import io.github.genie.sql.api.Path;
import io.github.genie.sql.builder.QueryStructures.ColumnMeta;
import io.github.genie.sql.builder.QueryStructures.ConstantMeta;
import io.github.genie.sql.builder.QueryStructures.OperationMeta;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public interface ExpressionBuilders {

    Expression TRUE = ExpressionBuilders.of(true);

    static boolean isTrue(Expression expression) {
        return expression instanceof Constant constant
               && Boolean.TRUE.equals(constant.value());
    }

    static Expression of(ExpressionHolder<?, ?> expression) {
        return expression.expression();
    }

    static Expression of(Object value) {
        return new ConstantMeta(value);
    }

    static Expression of(Expression value) {
        return value;
    }

    static Column of(Path<?, ?> path) {
        String property = asString(path);
        return fromPath(property);
    }

    static String asString(Path<?, ?> path) {
        return Util.getPropertyName(path);
    }


    static Column fromPath(String path) {
        List<String> paths = new ArrayList<>(1);
        paths.add(path);
        return fromPaths(paths);
    }

    static Column fromPaths(List<String> paths) {
        Objects.requireNonNull(paths);
        if (paths.getClass() != ArrayList.class) {
            paths = new ArrayList<>(paths);
        }
        return new ColumnMeta(paths);
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

    static <T> List<PathOperator<T, ?, Predicate<T>>> toExpressionList(Collection<Path<T, ?>> paths) {
        return paths.stream()
                .<PathOperator<T, ?, Predicate<T>>>map(Q::get)
                .toList();
    }




}
