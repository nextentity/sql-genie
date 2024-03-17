package io.github.genie.sql.test.entity;

import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.Hibernate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Objects;

import static javax.persistence.ConstraintMode.NO_CONSTRAINT;

@SuppressWarnings("JpaDataSourceORMInspection")
@javax.persistence.Entity
@ToString
@Getter
@Setter
public class User extends EnableOptimisticLock implements Cloneable {

    @Id
    private int id;

    private int randomNumber;

    private String username;

    private Date time;

    private Integer pid;

    private Double timestamp;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pid", insertable = false, updatable = false)
    @ToString.Exclude
    private User parentUser;

    private boolean valid;

    private Gender gender;

    private Date instant;

    private Long testLong;

    private Integer testInteger;

    private LocalDate testLocalDate;

    private LocalDateTime testLocalDateTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "randomNumber", updatable = false, insertable = false, foreignKey = @ForeignKey(NO_CONSTRAINT))
    @ToString.Exclude
    private User randomUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "testInteger", updatable = false, insertable = false, foreignKey = @ForeignKey(NO_CONSTRAINT))
    @ToString.Exclude
    private User testUser;

    public User() {
    }

    public boolean isNew() {
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        User user = (User) o;
        return getId() == user.getId()
               && getRandomNumber() == user.getRandomNumber()
               && isValid() == user.isValid()
               && Objects.equals(getUsername(), user.getUsername())
               && Objects.equals(getTime(), user.getTime())
               && Objects.equals(getPid(), user.getPid())
               && Objects.equals(getTimestamp(), user.getTimestamp())
               && getGender() == user.getGender()
               && Objects.equals(getInstant(), user.getInstant())
               && Objects.equals(getTestLong(), user.getTestLong())
               && Objects.equals(getTestInteger(), user.getTestInteger())
               && Objects.equals(getTestLocalDate(), user.getTestLocalDate())
               && Objects.equals(getTestLocalDateTime(), user.getTestLocalDateTime());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getRandomNumber(), getUsername(), getTime(), getPid(),
                getTimestamp(), isValid(), getGender(), getInstant(), getTestLong(),
                getTestInteger(), getTestLocalDate(), getTestLocalDateTime());
    }

    @Override
    public User clone() {
        try {
            return (User) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
