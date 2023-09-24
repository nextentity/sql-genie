package io.github.genie.sql.core;

import java.util.List;

public interface SelectClause {

    Class<?> resultType();


    interface MultiColumn extends SelectClause {
        List<? extends Expression> columns();

        @Override
        default Class<?> resultType() {
            return Object[].class;
        }

    }

    interface SingleColumn extends SelectClause {
        Expression column();

    }


}
