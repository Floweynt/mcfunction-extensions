package com.floweytf.mcfext.parse.ast;

import com.floweytf.mcfext.codegen.CodeGenerator;
import net.minecraft.commands.CommandSourceStack;

import java.util.List;
import java.util.function.Consumer;

public class BlockAST extends ASTNode {
    private final List<ASTNode> children;

    public BlockAST(List<ASTNode> children) {
        this.children = children;
    }

    @Override
    public void emit(CodeGenerator<CommandSourceStack> generator) {
        for (final var child : children) {
            child.emit(generator);
        }
    }

    @Override
    public void visit(Consumer<ASTNode> visitor) {
        children.forEach(visitor);
    }

    @Override
    public String toString() {
        return "BlockAST";
    }
}
