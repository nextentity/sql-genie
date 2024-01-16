package io.github.genie.sql.builder.meta;

import java.util.Collection;

public interface Projection extends Schema {

    Collection<? extends ProjectionAttribute> attributes();

    EntityType entityType();

}
