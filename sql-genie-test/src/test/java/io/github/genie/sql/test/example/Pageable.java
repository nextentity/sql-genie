package io.github.genie.sql.test.example;

import io.github.genie.sql.api.Sliceable;
import lombok.Data;

import java.util.List;

@Data
public class Pageable<T> implements Sliceable<T, Page<T>> {

    private int page;
    private int size;

    public Pageable() {
    }

    public Pageable(int page, int size) {
        this.page = page;
        this.size = size;
    }

    @Override
    public int offset() {
        return (page - 1) * size;
    }

    @Override
    public int limit() {
        return size;
    }

    @Override
    public Page<T> collect(List<T> list, long total) {
        return new Page<>(list, total, this);
    }
}
