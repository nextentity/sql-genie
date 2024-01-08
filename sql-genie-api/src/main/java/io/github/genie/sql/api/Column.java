package io.github.genie.sql.api;

import java.util.List;

public interface Column extends Expression {
    List<String> paths();

}
