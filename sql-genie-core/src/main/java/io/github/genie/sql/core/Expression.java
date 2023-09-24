package io.github.genie.sql.core;

import java.util.List;

@FunctionalInterface
public interface Expression {

    Meta meta();

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
        Expression leftOperand();

        Operator operator();

        List<? extends Expression> rightOperand();

    }

}
