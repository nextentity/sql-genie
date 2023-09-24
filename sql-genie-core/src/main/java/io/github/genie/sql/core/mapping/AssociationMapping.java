package io.github.genie.sql.core.mapping;

public interface AssociationMapping extends FieldMapping {

    String joinColumnName();

    String referencedColumnName();

    TableMapping referenced();

    @Override
    default FieldMapping getFieldMapping(String fieldName) {
        return referenced().getFieldMapping(fieldName);
    }
}
