package io.github.genie.sql.api;

import java.util.List;

non-sealed public interface Column extends Expression {
    List<String> paths();

}
