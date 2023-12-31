package io.github.genie.sql.test.example;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

@Data
@Entity
public class Company {

    @Id
    private Integer id;
    private String name;
    private String addr;

}
