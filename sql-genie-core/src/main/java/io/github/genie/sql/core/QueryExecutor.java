package io.github.genie.sql.core;

import org.jetbrains.annotations.NotNull;

import java.util.List;

@FunctionalInterface
public interface QueryExecutor {

    <R> List<R> getList(@NotNull QueryMetadata queryMetadata);


}
