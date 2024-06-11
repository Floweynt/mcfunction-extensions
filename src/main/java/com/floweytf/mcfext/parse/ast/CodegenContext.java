package com.floweytf.mcfext.parse.ast;

import com.floweytf.mcfext.codegen.Label;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public final class CodegenContext {
    private @Nullable Label breakExitLabel;
    private final Map<String, Label> subroutines = new HashMap<>();

    public CodegenContext(@Nullable Label breakExitLabel) {
        this.breakExitLabel = breakExitLabel;
    }

    public CodegenContext() {
        this(null);
    }

    public @Nullable Label breakExitLabel() {
        return breakExitLabel;
    }

    public void breakExitLabel(@Nullable Label breakExitLabel) {
        this.breakExitLabel = breakExitLabel;
    }

    public Map<String, Label> subroutines() {
        return subroutines;
    }
}
