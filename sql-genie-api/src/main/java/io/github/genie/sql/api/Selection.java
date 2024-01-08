package io.github.genie.sql.api;

import java.io.Serializable;
import java.util.List;

public interface Selection extends Serializable {

    Class<?> resultType();

    interface MultiColumn extends Selection {
        List<? extends Expression> columns();

        @Override
        default Class<?> resultType() {
            return Object[].class;
        }

    }

    interface SingleColumn extends Selection {
        Expression column();

    }

}
