package io.github.genie.sql.builder.meta;

import java.util.Collection;

public interface EntityType extends Type {

    Attribute id();

    String tableName();

    Collection<? extends Attribute> attributes();

    Attribute getAttribute(String fieldName);

    Attribute version();

}
