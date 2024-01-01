package io.github.genie.sql.executor.jpa;


import io.github.genie.sql.api.LockModeType;

public class LockModeTypeAdapter {

    public static javax.persistence.LockModeType of(LockModeType lockModeType) {
        return lockModeType == null ? null : javax.persistence.LockModeType.valueOf(lockModeType.name());
    }

}
