package io.github.genie.sql.core;

import java.io.Serializable;
import java.util.List;

@SuppressWarnings("unused")
public interface Expression<T, U> {

    Meta meta();

    sealed interface Meta extends Serializable permits Constant, Paths, Operation {
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

        default Meta firstArg() {
            List<? extends Meta> args = args();
            return args == null || args.isEmpty() ? null : args.get(0);
        }

        default Meta secondArg() {
            List<? extends Meta> args = args();
            return args == null || args.size() < 2 ? null : args.get(1);
        }

    }

}
