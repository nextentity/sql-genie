package io.github.genie.sql;

import com.mysql.cj.jdbc.MysqlDataSource;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

@Data
public class DataSourceConfig {

    private String url = "jdbc:mysql:///sql-dsl";
    private String user = "root";
    private String password = "root";

    @NotNull
    public MysqlDataSource getMysqlDataSource() {
        MysqlDataSource source = new MysqlDataSource();
        source.setUrl(getUrl());
        source.setUser(getUser());
        source.setPassword(getPassword());
        return source;
    }

}
