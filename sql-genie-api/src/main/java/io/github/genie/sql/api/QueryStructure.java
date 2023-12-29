package io.github.genie.sql.api;


import java.io.Serializable;
import java.util.List;

public interface QueryStructure extends Serializable {

    Selection select();

    Class<?> from();

    Expression where();

    List<? extends Expression> groupBy();

    List<? extends Order<?>> orderBy();

    Expression having();

    Integer offset();

    Integer limit();

    LockModeType lockType();

    List<? extends Column> fetch();
}
