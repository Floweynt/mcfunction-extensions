package com.floweytf.mcfext.parse.ast;

import com.floweytf.mcfext.codegen.CodeGenerator;
import net.minecraft.commands.CommandSourceStack;

import java.util.function.Consumer;

public class TopLevelAST extends ASTNode {
    private final BlockAST block;

    public TopLevelAST(BlockAST block) {
        this.block = block;
    }

    @Override
    public void emit(CodeGenerator<CommandSourceStack> generator) {
        block.emit(generator);
    }

    @Override
    public void visit(Consumer<ASTNode> visitor) {
        visitor.accept(block);
    }

    @Override
    public String toString() {
        return "TopLevel";
    }
}
