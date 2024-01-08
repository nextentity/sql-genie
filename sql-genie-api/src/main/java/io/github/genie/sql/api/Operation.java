package io.github.genie.sql.api;

import java.util.List;

public interface Operation extends Expression {
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
