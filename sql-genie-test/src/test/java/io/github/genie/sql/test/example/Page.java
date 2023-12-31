package io.github.genie.sql.test.example;

import lombok.Data;

import java.util.List;

@Data
public class Page<T> {

    private List<T> list;
    private int total;
    private int page;
    private int size;

    public Page(List<T> list, int total, Pageable<T> pageable) {
        this.list = list;
        this.total = total;
        this.page = pageable.getPage();
        this.size = pageable.getSize();
    }
}
