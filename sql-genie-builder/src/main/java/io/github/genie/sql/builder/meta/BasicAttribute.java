package io.github.genie.sql.builder.meta;

public interface BasicAttribute extends Attribute {

    String columnName();

    boolean hasVersion();

}
