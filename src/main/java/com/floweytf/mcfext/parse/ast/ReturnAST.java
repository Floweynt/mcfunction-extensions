package com.floweytf.mcfext.parse.ast;

import com.floweytf.mcfext.codegen.CodeGenerator;
import com.floweytf.mcfext.execution.instr.BranchInstr;
import com.floweytf.mcfext.parse.Diagnostics;
import net.minecraft.commands.CommandSourceStack;

import java.util.function.Consumer;

/**
 * Exit from the currently executing MCFunction
 */
public class ReturnAST extends ASTNode {
    @Override
    public void emit(Diagnostics diagnostics, CodegenContext cgCtx, CodeGenerator<CommandSourceStack> gen) {
        gen.emitControl(BranchInstr.exit());
    }

    @Override
    public void visit(Consumer<ASTNode> visitor) {

    }

    @Override
    public String toString() {
        return "ReturnAST";
    }
}
