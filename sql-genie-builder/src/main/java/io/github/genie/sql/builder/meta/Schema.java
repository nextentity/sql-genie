package io.github.genie.sql.builder.meta;

import java.util.Collection;

public interface Schema extends Type {
    Collection<? extends Attribute> attributes();
}
