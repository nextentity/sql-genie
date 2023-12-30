package io.github.genie.sql.api;

import lombok.Getter;

public enum Operator {

    NOT("not", 10),

    AND("and", 11, true),

    OR("or", 13, true),
    GT(">", 8),
    EQ("=", 8),
    NE("<>", 8),
    GE(">=", 8),
    LT("<", 8),
    LE("<=", 8),
    LIKE("like", 8),

    IS_NULL("is null", 13),
    IS_NOT_NULL("is not null", 13),
    IN("in", 0),
    BETWEEN("between", 8),

    LOWER("lower", 0),
    UPPER("upper", 0),
    SUBSTRING("substring", 0),
    TRIM("trim", 0),
    LENGTH("length", 0),

    ADD("+", 4, true),
    SUBTRACT("-", 4),
    MULTIPLY("*", 3, true),
    DIVIDE("/", 3),
    MOD("mod", 3),
    NULLIF("nullif", 0),
    IF_NULL("ifnull", 0),


    // aggregate function
    MIN("min", 0, false, true),
    MAX("max", 0, false, true),
    COUNT("count", 0, false, true),
    AVG("avg", 0, false, true),
    SUM("sum", 0, false, true),
    ;

    private final String sign;
    private final int priority;
    @Getter
    private final boolean multivalued;
    @Getter
    private final boolean agg;

    Operator(String sign, int priority, boolean multivalued, boolean agg) {
        this.sign = sign;
        this.priority = priority;
        this.multivalued = multivalued;
        this.agg = agg;
    }

    Operator(String sign, int priority, boolean multivalued) {
        this(sign, priority, multivalued, false);
    }

    Operator(String sign, int priority) {
        this(sign, priority, false);

    }

    public String sign() {
        return sign;
    }

    @Override
    public String toString() {
        return sign;
    }

    public int priority() {
        return priority;
    }

}
