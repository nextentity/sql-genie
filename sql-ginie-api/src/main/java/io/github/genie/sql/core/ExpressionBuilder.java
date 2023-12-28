package io.github.genie.sql.core;

import java.io.Serializable;
import java.util.List;

@SuppressWarnings("unused")
public interface ExpressionBuilder<T, U> {

    Expression build();

    sealed interface Expression extends Serializable permits Constant, Paths, Operation {
    }

    non-sealed interface Constant extends Expression {
        Object value();
    }

    non-sealed interface Paths extends Expression {
        List<String> paths();

    }

    non-sealed interface Operation extends Expression {
        Expression operand();

        Operator operator();

        List<? extends Expression> args();

        default Expression firstArg() {
            List<? extends Expression> args = args();
            return args == null || args.isEmpty() ? null : args.get(0);
        }

        default Expression secondArg() {
            List<? extends Expression> args = args();
            return args == null || args.size() < 2 ? null : args.get(1);
        }

    }

}
