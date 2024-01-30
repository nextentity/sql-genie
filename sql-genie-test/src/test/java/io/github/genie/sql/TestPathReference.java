package io.github.genie.sql;

import io.github.genie.sql.builder.PathReference;
import io.github.genie.sql.entity.User;

public class TestPathReference {

    public static void main(String[] args) {
        PathReference reference = PathReference.of(User::getId);
        System.out.println(reference);
    }

}
