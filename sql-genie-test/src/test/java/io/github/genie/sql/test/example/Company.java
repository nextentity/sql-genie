package io.github.genie.sql.test.example;

import javax.persistence.Entity;
import javax.persistence.Id;
import lombok.Data;

@Data
@Entity
public class Company {

    @Id
    private Integer id;
    private String name;
    private String addr;

}
