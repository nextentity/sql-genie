package io.github.genie.sql.core;


import io.github.genie.sql.core.ExpressionBuilder.Expression;
import io.github.genie.sql.core.ExpressionBuilder.Paths;

import java.io.Serializable;
import java.util.List;

public interface QueryStructure extends Serializable {

    Selection select();

    Class<?> from();

    Expression where();

    List<? extends Expression> groupBy();

    List<? extends Ordering<?>> orderBy();

    Expression having();

    Integer offset();

    Integer limit();

    LockModeType lockType();

    List<? extends Paths> fetch();
}
