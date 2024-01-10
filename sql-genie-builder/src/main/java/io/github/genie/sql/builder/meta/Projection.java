package io.github.genie.sql.builder.meta;

import java.util.List;

public interface Projection extends Type {

    List<ProjectionAttribute> attributes();

}
