package com.floweytf.mcfext.parse.ast;

import com.floweytf.mcfext.codegen.CodeGenerator;
import com.floweytf.mcfext.parse.Diagnostics;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.execution.UnboundEntryAction;

import java.util.function.Consumer;

public class CommandAST extends ASTNode {
    private final UnboundEntryAction<CommandSourceStack> action;

    public CommandAST(UnboundEntryAction<CommandSourceStack> action) {
        this.action = action;
    }

    @Override
    public void emit(Diagnostics diagnostics, CodegenContext cgCtx, CodeGenerator<CommandSourceStack> gen) {
        gen.emitPlain(action);
    }

    @Override
    public void visit(Consumer<ASTNode> visitor) {

    }

    @Override
    public String toString() {
        return "CommandAST[" + action + "]";
    }
}
