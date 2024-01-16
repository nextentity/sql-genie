package io.github.genie.sql.projection;

import io.github.genie.sql.builder.meta.EntityAttribute;
import io.github.genie.sql.entity.User;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class UserModel implements UserInterface {

    private int id;

    private int randomNumber;

    private String username;

    private Integer pid;

    private boolean valid;

    @EntityAttribute("parentUser.username")
    private String parentUsername;

    public UserModel(User user) {

        id = user.getId();
        randomNumber = user.getRandomNumber();
        username = user.getUsername();
        pid = user.getPid();
        valid = user.isValid();

    }
}
