package com.floweytf.mcfext.parse.ast.subroutine;

import com.floweytf.mcfext.codegen.CodeGenerator;
import com.floweytf.mcfext.execution.instr.SubroutineRetInstr;
import com.floweytf.mcfext.parse.Diagnostics;
import com.floweytf.mcfext.parse.ast.ASTNode;
import com.floweytf.mcfext.parse.ast.CodegenContext;
import net.minecraft.commands.CommandSourceStack;

import java.util.function.Consumer;

/**
 * Return from the current **subroutine**. This is not related to the
 * <a href="https://minecraft.wiki/w/Commands/return">vanilla</a> command, which roughly corresponds to "exit"
 */
public class SubroutineReturnAST extends ASTNode {
    @Override
    public void emit(Diagnostics diagnostics, CodegenContext cgCtx, CodeGenerator<CommandSourceStack> gen) {
        gen.emitControl(SubroutineRetInstr.get());
    }

    @Override
    public void visit(Consumer<ASTNode> visitor) {
    }

    @Override
    public String toString() {
        return "SubroutineReturnAST";
    }
}