package io.github.genie.sql.projection;

import com.fasterxml.jackson.core.type.TypeReference;
import io.github.genie.sql.JsonSerializablePredicateValueTest;
import io.github.genie.sql.builder.meta.EntityAttribute;

import java.util.HashMap;
import java.util.Map;

public interface UserInterface {

    int getId();

    int getRandomNumber();

    String getUsername();

    Integer getPid();

    boolean isValid();

    @EntityAttribute("parentUser.username")
    String getParentUsername();

    default Map<String, Object> asMap() {
        return JsonSerializablePredicateValueTest.mapper
                .convertValue(this, new TypeReference<>() {
                });
    }
}
