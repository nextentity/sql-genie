package io.github.genie.sql.core.mapping;

import lombok.Data;

import java.util.List;

@Data
public class Projection {

    private List<ProjectionAttribute> attributes;

}
