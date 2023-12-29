package io.github.genie.sql.core;

import java.io.Serializable;

public enum LockModeType implements Serializable {

    READ,
    WRITE,
    OPTIMISTIC,
    OPTIMISTIC_FORCE_INCREMENT,
    PESSIMISTIC_READ,
    PESSIMISTIC_WRITE,
    PESSIMISTIC_FORCE_INCREMENT,
    NONE

}
