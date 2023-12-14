package io.github.genie.sql.core.mapping;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public interface FieldMapping extends Mapping {

    Mapping parent();

    String fieldName();

    Method getter();

    Method setter();

    Field field();

}
