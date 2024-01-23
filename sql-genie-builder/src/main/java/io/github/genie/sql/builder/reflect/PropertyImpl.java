package io.github.genie.sql.builder.reflect;

import io.github.genie.sql.builder.meta.Attribute;

public class PropertyImpl implements Property {
    private int index;
    private final Attribute attribute;

    public PropertyImpl(Attribute attribute) {
        this.attribute = attribute;
    }

    @Override
    public Attribute attribute() {
        return attribute;
    }

    @Override
    public Object newInstance(Object[] args) {
        return args[index];
    }

    void setIndex(int index) {
        this.index = index;
    }
}
