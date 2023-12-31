package io.github.genie.sql.test.projection;

import java.util.HashMap;
import java.util.Map;

public interface UserInterface {

    int getId();

    int getRandomNumber();

    String getUsername();

    Integer getPid();

    boolean isValid();

    default Map<String, Object> asMap() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("id", getId());
        map.put("randomNumber", getRandomNumber());
        map.put("username", getUsername());
        map.put("pid", getPid());
        map.put("valid", isValid());
        return map;
    }
}
