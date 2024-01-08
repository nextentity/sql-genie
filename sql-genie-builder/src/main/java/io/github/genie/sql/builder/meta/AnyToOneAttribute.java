package io.github.genie.sql.builder.meta;

public interface AnyToOneAttribute extends Attribute {

    String joinColumnName();

    String joinName();

    String referencedColumnName();

    EntityType referenced();

}
