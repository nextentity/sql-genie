package io.github.genie.sql.builder.meta;

public interface AnyToOneAttribute extends Attribute, EntityType {

    String joinColumnName();

    String joinName();

    String referencedColumnName();

}
