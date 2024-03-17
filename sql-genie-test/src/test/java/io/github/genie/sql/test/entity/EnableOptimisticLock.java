package io.github.genie.sql.test.entity;

import javax.persistence.MappedSuperclass;
import javax.persistence.Version;
import lombok.AccessLevel;
import lombok.Getter;

@MappedSuperclass
public class EnableOptimisticLock {

    @Version
    @Getter(AccessLevel.PRIVATE)
    private int optLock;

}
