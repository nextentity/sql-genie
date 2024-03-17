package io.github.genie.sql.test.projection;

import lombok.Getter;
import lombok.Setter;

public interface IUser {

    int getId();

    int getRandomNumber();

    String getUsername();

    U parentUser();

    @Getter
    @Setter
    final class U {
        private int id;
        private int randomNumber;
        private String username;
    }

}
