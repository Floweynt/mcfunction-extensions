package com.floweytf.mcfext.parse.ast;

import com.floweytf.mcfext.codegen.CodeGenerator;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.execution.UnboundEntryAction;

import java.util.function.Consumer;

public class CommandAST extends ASTNode {
    private final UnboundEntryAction<CommandSourceStack> action;

    public CommandAST(UnboundEntryAction<CommandSourceStack> action) {
        this.action = action;
    }

    @Override
    public void emit(CodeGenerator<CommandSourceStack> generator) {
        generator.emitPlain(action);
    }

    @Override
    public void visit(Consumer<ASTNode> visitor) {

    }

    @Override
    public String toString() {
        return "CommandAST[" + action + "]";
    }
}
