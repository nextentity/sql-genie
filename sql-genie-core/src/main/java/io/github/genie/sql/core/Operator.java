package io.github.genie.sql.core;

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

    ADD("+", 4),
    SUBTRACT("-", 4),
    MULTIPLY("*", 3),
    DIVIDE("/", 3),
    MOD("mod", 3),
    NULLIF("nullif", 0),
    IF_NULL("ifnull", 0),


    // aggregate function
    MIN("min", 0),

    MAX("max", 0),
    COUNT("count", 0),
    AVG("avg", 0),
    SUM("sum", 0);

    private final String sign;
    private final int priority;
    private final boolean multivalued;

    Operator(String sign, int priority, boolean multivalued) {
        this.sign = sign;
        this.priority = priority;
        this.multivalued = multivalued;
    }

    Operator(String sign, int priority) {
        this.sign = sign;
        this.priority = priority;
        multivalued = false;
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

    public boolean isMultivalued() {
        return multivalued;
    }
}
