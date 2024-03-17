package io.github.genie.sql.test.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Version;

@Getter
@Setter
@NoArgsConstructor
@Entity
public class InsertTest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private Integer num;
    @Version
    private int version;

    public InsertTest(Integer id, Integer num) {
        this.id = id;
        this.num = num;
    }
}
