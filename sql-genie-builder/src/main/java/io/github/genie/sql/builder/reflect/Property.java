package io.github.genie.sql.builder.reflect;

import io.github.genie.sql.builder.meta.Attribute;

public interface Property extends InstanceConstructor {
    Attribute attribute();

}
