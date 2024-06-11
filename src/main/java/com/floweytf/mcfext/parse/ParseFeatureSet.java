package com.floweytf.mcfext.parse;

import com.google.common.collect.ImmutableMap;

import java.util.Map;

/**
 * A set of features that can be enabled during compilation, in order to maintain backwards compatibility.
 */
public class ParseFeatureSet {
    private interface FlagSetter {
        void set(ParseFeatureSet instance, boolean value);
    }

    private static final Map<String, FlagSetter> CONSUMERS =
        ImmutableMap.<String, FlagSetter>builder()
            .put("cfv2", (s, f) -> s.v2ControlFlow = f)
            .put("subroutine", (s, f) -> s.subroutines = f)
            .put("debug_dump", (s, f) -> s.debugDump = f)
            .build();

    private boolean v2ControlFlow = false;
    private boolean subroutines = false;
    private boolean debugDump = false;

    public boolean isV2ControlFlow() {
        return v2ControlFlow;
    }

    public boolean isSubroutines() {
        return subroutines;
    }

    public boolean isDebugDump() {
        return debugDump;
    }

    public boolean set(String name, boolean value) {
        final var handler = CONSUMERS.get(name);
        if (handler != null) {
            handler.set(this, value);
            return false;
        }
        return true;
    }

    public boolean enable(String name) {
        return set(name, true);
    }

    public boolean disable(String name) {
        return set(name, false);
    }
}