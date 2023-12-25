package io.github.genie.sql.core;

import io.github.genie.sql.core.Expression.Meta;

import java.io.Serializable;
import java.util.List;

public interface SelectClause extends Serializable {

    Class<?> resultType();


    interface MultiColumn extends SelectClause {
        List<? extends Meta> columns();

        @Override
        default Class<?> resultType() {
            return Object[].class;
        }

    }

    interface SingleColumn extends SelectClause {
        Meta column();

    }


}
