package io.github.genie.sql.test.entity;

import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Getter;

@MappedSuperclass
public class EnableOptimisticLock {

    @Version
    @Getter(AccessLevel.PRIVATE)
    private int optLock;

}
