package com.floweytf.mcfext.parse.ast;

import com.floweytf.mcfext.codegen.CodeGenerator;
import com.floweytf.mcfext.execution.instr.BranchInstr;
import net.minecraft.commands.CommandSourceStack;

import java.util.function.Consumer;

/**
 * Exit from the currently executing MCFunction
 */
public class ExitAST extends ASTNode {
    @Override
    public void emit(CodeGenerator<CommandSourceStack> generator) {
        generator.emitControl(BranchInstr.exitInstr());
    }

    @Override
    public void visit(Consumer<ASTNode> visitor) {

    }

    @Override
    public String toString() {
        return "ExitAST";
    }
}
