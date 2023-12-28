package io.github.genie.sql.core.executor.jpa;


import io.github.genie.sql.core.LockModeType;

public class LockModeTypeAdapter {

    public static jakarta.persistence.LockModeType of(LockModeType lockModeType) {
        return lockModeType == null ? null : jakarta.persistence.LockModeType.valueOf(lockModeType.name());
    }

}
