package io.github.genie.sql.core;


import io.github.genie.sql.core.Expression.Constant;
import io.github.genie.sql.core.Expression.Operation;
import io.github.genie.sql.core.Expression.Paths;
import io.github.genie.sql.core.OperateableExpression.BooleanExpression;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class BasicExpressions {

    public static final Expression.Meta TRUE = of(true);

    public static <T> BooleanExpression<T> operateAsPredicate(Expression leftOperand,
                                                              Operator operator,
                                                              List<? extends Expression> rightOperand) {
        return leftOperand instanceof CustomizerOperator cst
                ? cst.operateAsPredicate(operator, rightOperand)
                : () -> operate(leftOperand, operator, rightOperand);
    }

    public static <T, U extends Comparable<U>>
    OperateableExpression.ComparableExpression<T, U> operateAsOperableComparable(Expression leftOperand,
                                                                                 Operator operator,
                                                                                 List<? extends Expression> rightOperand) {
        return leftOperand instanceof CustomizerOperator cst
                ? cst.operateAsOperableComparable(operator, rightOperand)
                : () -> operate(leftOperand, operator, rightOperand);
    }

    public static <T> OperateableExpression.StringExpression<T> operateAsString(Expression leftOperand,
                                                                                Operator operator,
                                                                                List<? extends Expression> rightOperand) {
        return leftOperand instanceof CustomizerOperator cst
                ? cst.operateAsOperableString(operator, rightOperand)
                : () -> operate(leftOperand, operator, rightOperand);
    }

    public static <T, U extends Number & Comparable<U>>
    OperateableExpression.NumberExpression<T, U> operateAsNumber(Expression leftOperand,
                                                                 Operator operator,
                                                                 List<? extends Expression> rightOperand) {
        return leftOperand instanceof CustomizerOperator cst
                ? cst.operateAsOperableNumber(operator, rightOperand)
                : () -> operate(leftOperand, operator, rightOperand);
    }

    public static Paths concat(Paths paths, Path<?, ?> path) {
        return concat(paths, getPathName(path));
    }

    public static String getPathName(Path<?, ?> path) {
        return GetterReferenceName.getPropertyName(path);
    }

    public static Paths concat(Paths paths, String path) {
        return of(Util.concat(paths.paths(), path));
    }

    public static <T extends Expression> T of(T expression) {
        return expression;
    }

    public static Constant of(Object value) {
        return new ConstantExpression(value);
    }

    public static Paths of(List<String> paths) {
        return new PathsExpression(paths);
    }

    public static Paths of(Path<?, ?> path) {
        String property = getPathName(path);
        return new PathsExpression(Collections.singletonList(property));
    }

    public static Expression.Meta operate(Expression l, Operator o, List<? extends Expression> r) {

        if (o.isMultivalued()
            && l.meta() instanceof Operation operation
            && operation.operator() == o) {
            l = operation.leftOperand();
            r = Stream.concat(operation.rightOperand().stream(), r.stream()).toList();
        }
        return new OperationExpression(l, o, r);
    }

    public static boolean isTrue(Expression expression) {
        Expression.Meta meta = expression.meta();
        return isTrue(meta);
    }

    static boolean isTrue(Expression.Meta meta) {
        return meta instanceof Constant constant
               && Boolean.TRUE.equals(constant.value());
    }


    record ConstantExpression(Object value) implements Constant {
        @Override
        public String toString() {
            return String.valueOf(value);
        }
    }

    record PathsExpression(List<String> paths) implements Paths {
        @Override
        public String toString() {
            return String.join(".", paths);
        }
    }

    static <T> List<OperateableExpression<T, ?>> list(Path<?, ?>... paths) {
        return Arrays.stream(paths)
                .<OperateableExpression<T, ?>>map(path -> () -> BasicExpressions.of(path))
                .toList();
    }

    record OperationExpression(Expression leftOperand, Operator operator,
                               List<? extends Expression> rightOperand) implements Operation {

        @Override
        public String toString() {
            Expression.Meta l = leftOperand().meta();
            List<? extends Expression.Meta> r;
            if (rightOperand() != null) {
                r = rightOperand().stream()
                        .map(Expression::meta).toList();
            } else {
                r = List.of();
            }
            if (operator().isMultivalued()) {
                return '(' + Stream.concat(Stream.of(l), r.stream())
                        .map(String::valueOf)
                        .collect(Collectors.joining(" " + operator() + ' '))
                       + ')';
            } else if (r.isEmpty()) {
                return "(" + l + ' ' + operator().sign() + ')';
            } else if (r.size() == 1) {
                return "(" + l + ' ' + operator().sign() + ' ' + r.get(0) + ")";
            } else {
                return "(" + l + ' ' + operator().sign() + ' ' + r + ")";
            }
        }

    }


    interface CustomizerOperator extends Expression {
        default <T> BooleanExpression<T> operateAsPredicate(Operator operator,
                                                            List<? extends Expression> rightOperand) {
            return () -> operate(this, operator, rightOperand);
        }

        default <T, U extends Comparable<U>>
        OperateableExpression.ComparableExpression<T, U> operateAsOperableComparable(Operator operator,
                                                                                     List<? extends Expression> rightOperand) {
            return () -> operate(this, operator, rightOperand);
        }

        default <T> OperateableExpression.StringExpression<T> operateAsOperableString(Operator operator,
                                                                                      List<? extends Expression> rightOperand) {
            return () -> operate(this, operator, rightOperand);
        }

        default <T, U extends Number & Comparable<U>>
        OperateableExpression.NumberExpression<T, U> operateAsOperableNumber(Operator operator,
                                                                             List<? extends Expression> rightOperand) {
            return () -> operate(this, operator, rightOperand);
        }
    }

}
