package io.github.genie.sql.test;

import lombok.Data;

@Data
public class DataSourceConfig {

    private String url = "jdbc:mysql:///sql-dsl";
    private String user = "root";
    private String password = "root";

}
