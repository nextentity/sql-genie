package io.github.genie.sql.test.projection;

public interface IUser {

    int getId();

    int getRandomNumber();

    String getUsername();

    U parentUser();

    record U(int id, int randomNumber, String username) {
    }

}
