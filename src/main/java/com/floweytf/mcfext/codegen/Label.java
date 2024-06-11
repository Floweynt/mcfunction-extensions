package com.floweytf.mcfext.codegen;

import org.jetbrains.annotations.Nullable;

public class Label {
    final int id;
    int offset;
    @Nullable
    public final String name;

    public Label(int id, @Nullable String name) {
        this.name = name;
        this.offset = -1;
        this.id = id;
    }

    public int offset() {
        if (offset == -1) {
            throw new IllegalStateException("Accessing target before it is defined");
        }
        return offset;
    }

    @Override
    public String toString() {
        if (name == null) {
            return "$" + id;
        }

        return String.format("%s$%s", name, id);
    }
}
