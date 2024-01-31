package io.github.genie.sql.api;

import io.github.genie.sql.api.tuple.Tuple;

import java.io.Serializable;
import java.util.List;

public interface Selection extends Serializable {

    Class<?> resultType();

    boolean distinct();

    interface MultiSelected extends Selection {
        List<? extends Expression> expressions();

        @Override
        default Class<?> resultType() {
            return Tuple.class;
        }

    }

    interface SingleSelected extends Selection {
        Expression expression();

    }

    interface ProjectionSelected extends Selection {
    }

    interface EntitySelected extends Selection {
    }

}
