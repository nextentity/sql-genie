package io.github.genie.sql.builder.meta;

public interface EntityType extends ObjectType {

    Attribute id();

    String tableName();

    Attribute getAttribute(String fieldName);

    Attribute version();

}
