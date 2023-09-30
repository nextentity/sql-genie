package io.github.genie.sql.core;

import java.util.List;

public interface Expression {

    Meta meta();

    @SuppressWarnings("unused")
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
        Meta operand();

        Operator operator();

        List<? extends Meta> args();

    }

}
