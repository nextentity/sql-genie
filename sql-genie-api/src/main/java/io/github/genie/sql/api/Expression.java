package io.github.genie.sql.api;

import java.io.Serializable;

sealed public interface Expression extends ExpressionHolder<Object, Object>, Serializable permits Constant, Column, Operation {
    @Override
    default Expression expression() {
        return this;
    }

}
