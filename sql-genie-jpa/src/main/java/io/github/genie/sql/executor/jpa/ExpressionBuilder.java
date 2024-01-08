package io.github.genie.sql.executor.jpa;


import io.github.genie.sql.api.Column;
import io.github.genie.sql.api.Constant;
import io.github.genie.sql.api.Expression;
import io.github.genie.sql.api.Operation;
import io.github.genie.sql.api.Operator;
import io.github.genie.sql.builder.Expressions;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.FetchParent;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Root;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.github.genie.sql.builder.TypeCastUtil.unsafeCast;

@SuppressWarnings("PatternVariableCanBeUsed")
public class ExpressionBuilder {

    protected final Root<?> root;

    protected final CriteriaBuilder cb;

    protected final Map<Column, FetchParent<?, ?>> fetched = new HashMap<>();

    public ExpressionBuilder(Root<?> root, CriteriaBuilder cb) {
        this.root = root;
        this.cb = cb;
    }


    public jakarta.persistence.criteria.Expression<?> toExpression(Expression expression) {
        if (expression instanceof Constant) {
            Constant cv = (Constant) expression;
            return cb.literal(cv.value());
        }
        if (expression instanceof Column) {
            Column pv = (Column) expression;
            return getPath(pv);
        }
        if (expression instanceof Operation) {
            Operation ov = (Operation) expression;
            List<? extends Expression> args = ov.args();
            Operator operator = ov.operator();
            jakarta.persistence.criteria.Expression<?> e0 = toExpression(ov.operand());
            Expression e1 = ov.firstArg();
            Expression e2 = ov.secondArg();
            switch (operator) {
                case NOT:
                    return cb.not(cast(e0));
                case AND: {
                    jakarta.persistence.criteria.Expression<Boolean> res = cast(e0);
                    for (Expression arg : args) {
                        res = cb.and(res, cast(toExpression(arg)));
                    }
                    return res;
                }
                case OR: {
                    jakarta.persistence.criteria.Expression<Boolean> res = cast(e0);
                    for (Expression arg : args) {
                        res = cb.or(res, cast(toExpression(arg)));
                    }
                    return res;
                }
                case GT: {
                    if (e1 instanceof Constant) {
                        Constant cv = (Constant) e1;
                        if (cv.value() instanceof Number) {
                            return cb.gt(cast(e0), (Number) cv.value());
                        } else if (cv.value() instanceof Comparable) {
                            Comparable<Object> value = unsafeCast(cv.value());
                            return cb.greaterThan(cast(e0), value);
                        }
                    }
                    return cb.gt(cast(e0), cast(toExpression(e1)));
                }
                case EQ: {
                    if (e1 instanceof Constant) {
                        Constant cv = (Constant) e1;
                        return cb.equal(cast(e0), cv.value());
                    }
                    return cb.equal(e0, toExpression(e1));
                }
                case NE: {
                    if (e1 instanceof Constant) {
                        Constant cv = (Constant) e1;
                        return cb.notEqual(e0, cv.value());
                    }
                    return cb.notEqual(e0, toExpression(e1));
                }
                case GE: {
                    if (e1 instanceof Constant) {
                        Constant cv = (Constant) e1;
                        if (cv.value() instanceof Number) {
                            return cb.ge(cast(e0), (Number) cv.value());
                        } else if (cv.value() instanceof Comparable) {
                            Comparable<Object> comparable = unsafeCast(cv.value());
                            return cb.greaterThanOrEqualTo(cast(e0), comparable);
                        }
                    }
                    return cb.ge(cast(e0), cast(toExpression(e1)));
                }
                case LT: {
                    if (e1 instanceof Constant) {
                        Constant cv = (Constant) e1;
                        Object ve1 = cv.value();
                        if (ve1 instanceof Number) {
                            return cb.lt(cast(e0), (Number) ve1);
                        } else if (ve1 instanceof Comparable) {
                            Comparable<Object> ve11 = unsafeCast(ve1);
                            return cb.lessThan(cast(e0), ve11);
                        }
                    }
                    return cb.lt(cast(e0), cast(toExpression(e1)));
                }
                case LE: {
                    if (e1 instanceof Constant) {
                        Constant cv = (Constant) e1;
                        Object ve1 = cv.value();
                        if (ve1 instanceof Number) {
                            return cb.le(cast(e0), (Number) ve1);
                        } else if (ve1 instanceof Comparable) {
                            Comparable<Object> ve11 = unsafeCast(ve1);
                            return cb.lessThanOrEqualTo(cast(e0), ve11);
                        }
                    }
                    return cb.le(cast(e0), cast(toExpression(e1)));
                }
                case LIKE: {
                    if (e1 instanceof Constant && ((Constant) e1).value() instanceof String) {
                        String scv = (String) ((Constant) e1).value();
                        return cb.like(cast(e0), scv);
                    }
                    return cb.like(cast(e0), cast(toExpression(e1)));
                }
                case IS_NULL:
                    return cb.isNull(e0);
                case IS_NOT_NULL:
                    return cb.isNotNull(e0);
                case IN: {
                    if (args.size() > 1) {
                        CriteriaBuilder.In<Object> in = cb.in(e0);
                        for (Expression arg : args) {
                            if (arg instanceof Constant) {
                                Constant cv = (Constant) arg;
                                in = in.value(cv.value());
                            } else {
                                in = in.value(toExpression(arg));
                            }
                        }
                        return in;
                    } else {
                        return cb.literal(false);
                    }
                }
                case BETWEEN: {
                    if (e1 instanceof Constant
                            && e2 instanceof Constant
                            && ((Constant) e1).value() instanceof Comparable
                            && ((Constant) e2).value() instanceof Comparable) {
                        Constant cv2 = (Constant) e2;
                        Constant cv1 = (Constant) e1;
                        Comparable<Object> v1 = unsafeCast(cv1.value());
                        Comparable<Object> v2 = unsafeCast(cv2.value());
                        return cb.between(cast(e0), v1, v2);
                    }
                    return cb.between(
                            cast(e0),
                            cast(toExpression(e1)),
                            cast(toExpression(e2))
                    );
                }
                case LOWER:
                    return cb.lower(cast(e0));
                case UPPER:
                    return cb.upper(cast(e0));
                case SUBSTRING: {
                    if (args.size() == 1) {
                        if (e1 instanceof Constant
                                && ((Constant) e1).value() instanceof Number) {
                            Number number = (Number) ((Constant) e1).value();
                            return cb.substring(cast(e0), number.intValue());
                        }
                        return cb.substring(cast(e0), cast(toExpression(e1)));
                    } else if (args.size() == 2) {
                        if (e1 instanceof Constant
                                && ((Constant) e1).value() instanceof Number
                                && e2 instanceof Constant
                                && ((Constant) e2).value() instanceof Number) {
                            Number n2 = (Number) ((Constant) e2).value();
                            Number n1 = (Number) ((Constant) e1).value();
                            return cb.substring(cast(e0), n1.intValue(), n2.intValue());
                        }
                        return cb.substring(
                                cast(e0),
                                cast(toExpression(e1)),
                                cast(toExpression(e2))
                        );
                    } else {
                        throw new IllegalArgumentException("argument length error");
                    }
                }
                case TRIM:
                    return cb.trim(cast(e0));
                case LENGTH:
                    return cb.length(cast(e0));
                case ADD: {
                    if (e1 instanceof Constant && ((Constant) e1).value() instanceof Number) {
                        Number number = (Number) ((Constant) e1).value();
                        return cb.sum(cast(e0), number);
                    }
                    return cb.sum(cast(e0), cast(toExpression(e1)));
                }
                case SUBTRACT: {
                    if (e1 instanceof Constant && ((Constant) e1).value() instanceof Number) {
                        Number number = (Number) ((Constant) e1).value();
                        return cb.diff(cast(e0), number);
                    }
                    return cb.diff(cast(e0), cast(toExpression(e1)));
                }
                case MULTIPLY: {
                    if (e1 instanceof Constant && ((Constant) e1).value() instanceof Number) {
                        Constant cv1 = (Constant) e1;
                        return cb.prod(cast(e0), (Number) cv1.value());
                    }
                    return cb.prod(cast(e0), cast(toExpression(e1)));
                }
                case DIVIDE: {
                    if (e1 instanceof Constant && ((Constant) e1).value() instanceof Number) {
                        Number number = (Number) ((Constant) e1).value();
                        return cb.quot(cast(e0), number);
                    }
                    return cb.quot(cast(e0), cast(toExpression(e1)));
                }
                case MOD: {
                    if (e1 instanceof Constant
                            && ((Constant) e1).value() instanceof Integer) {
                        Constant cv1 = (Constant) e1;
                        return cb.mod(cast(e0), ((Integer) cv1.value()));
                    }
                    return cb.mod(cast(e0), cast(toExpression(e1)));
                }
                case NULLIF: {
                    if (e1 instanceof Constant) {
                        Constant cv = (Constant) e1;
                        return cb.nullif(cast(e0), ((Integer) cv.value()));
                    }
                    return cb.nullif(e0, toExpression(e1));
                }
                case IF_NULL: {
                    if (e1 instanceof Constant) {
                        Constant cv = (Constant) e1;
                        return cb.coalesce(cast(e0), ((Integer) cv.value()));
                    }
                    return cb.coalesce(e0, toExpression(e1));
                }
                case MIN:
                    return cb.min(cast(e0));
                case MAX:
                    return cb.max(cast(e0));
                case COUNT:
                    return cb.count(cast(e0));
                case AVG:
                    return cb.avg(cast(e0));
                case SUM:
                    return cb.sum(cast(e0));
                default:
                    throw new UnsupportedOperationException(operator.name());
            }
        } else {
            throw new UnsupportedOperationException("unknown expression type " + expression.getClass());
        }
    }

    public static <T> jakarta.persistence.criteria.Expression<T> cast(jakarta.persistence.criteria.Expression<?> expression) {
        return unsafeCast(expression);
    }

    protected Path<?> getPath(Column expression) {
        From<?, ?> r = root;
        List<String> paths = expression.paths();
        int size = paths.size();
        for (int i = 0; i < size; i++) {
            String s = paths.get(i);
            if (i != size - 1) {
                Column offset = subPaths(paths, i + 1);
                r = join(offset);
            } else {
                return r.get(s);
            }
        }

        return r;
    }

    @NotNull
    protected Column subPaths(List<String> paths, int size) {
        List<String> subPath = new ArrayList<>(size);
        for (int j = 0; j < size; j++) {
            subPath.add(paths.get(j));
        }
        return Expressions.column(subPath);
    }

    private Join<?, ?> join(Column offset) {
        return (Join<?, ?>) fetched.compute(offset, (k, v) -> {
            if (v instanceof Join<?, ?>) {
                return v;
            } else {
                From<?, ?> r = root;
                for (String s : offset.paths()) {
                    r = r.join(s, JoinType.LEFT);
                }
                return r;
            }
        });
    }
}