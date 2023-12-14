package io.github.genie.sql.core.mapping;

public interface AssociationMapping extends FieldMapping {

    String joinColumnName();

    String referencedColumnName();

    TableMapping referenced();

}
