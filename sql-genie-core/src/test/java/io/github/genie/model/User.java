package io.github.genie.model;

import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;

import java.util.Date;
import java.util.Objects;

@Data
public class User {

    private Integer id;

    private Integer randomNumber;

    private String username;

    private Date time;

    private Integer pid;

    private Integer companyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "companyId", insertable = false, updatable = false)
    private Company company;


    private boolean valid;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User user)) {
            return false;
        }

        if (!Objects.equals(getRandomNumber(), user.getRandomNumber())) return false;
        if (isValid() != user.isValid()) return false;
        if (!Objects.equals(getUsername(), user.getUsername())) return false;
        // if (!Objects.equals(time, user.time)) return false;
        return Objects.equals(getPid(), user.getPid());
    }

    @Override
    public int hashCode() {
        int result = getRandomNumber();
        String username = getUsername();
        result = 31 * result + (username != null ? username.hashCode() : 0);
        Date time = getTime();
        result = 31 * result + (time != null ? time.hashCode() : 0);
        Integer pid = getPid();
        result = 31 * result + (pid != null ? pid.hashCode() : 0);
        boolean valid = isValid();
        result = 31 * result + (valid ? 1 : 0);
        return result;
    }
}
