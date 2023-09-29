package io.github.genie.sql.core;

import java.util.List;

public interface Expression {

    default Meta meta() {
        throw new UnsupportedOperationException();
    }

    interface TypedExpression<T, U> extends Expression {

    }

    sealed interface Meta permits Constant, Paths, Operation {

    }

    non-sealed interface Constant extends Meta {
        Object value();
    }

    non-sealed interface Paths extends Meta {
        List<String> paths();

    }

    non-sealed interface Operation extends Meta {
        Meta leftOperand();

        Operator operator();

        List<? extends Meta> rightOperand();

    }

}
