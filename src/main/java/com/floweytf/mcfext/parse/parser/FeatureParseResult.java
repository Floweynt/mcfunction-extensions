package com.floweytf.mcfext.parse.parser;

import com.floweytf.mcfext.parse.ast.ASTNode;
import org.jetbrains.annotations.Nullable;

class FeatureParseResult {
    enum Action {
        RETURN,
        CONTINUE,
        FALLTHROUGH
    }

    private final Action action;
    @Nullable
    private final ASTNode value;

    private FeatureParseResult(Action action, @Nullable ASTNode value) {
        this.action = action;
        this.value = value;
    }

    public static FeatureParseResult parseNext() {
        return new FeatureParseResult(Action.CONTINUE, null);
    }

    public static FeatureParseResult fallthrough() {
        return new FeatureParseResult(Action.FALLTHROUGH, null);
    }

    public static FeatureParseResult ast(ASTNode node) {
        return new FeatureParseResult(Action.RETURN, node);
    }

    public Action action() {
        return action;
    }

    public @Nullable ASTNode value() {
        return value;
    }
}
