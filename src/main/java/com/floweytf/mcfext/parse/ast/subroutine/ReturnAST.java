package com.floweytf.mcfext.parse.ast.subroutine;

import com.floweytf.mcfext.codegen.CodeGenerator;
import com.floweytf.mcfext.execution.instr.RetInstr;
import com.floweytf.mcfext.parse.ast.ASTNode;
import net.minecraft.commands.CommandSourceStack;

import java.util.function.Consumer;

/**
 * Return from the current **subroutine**. This is not related to the
 * <a href="https://minecraft.wiki/w/Commands/return">vanilla</a> command, which roughly corresponds to "exit"
 */
public class ReturnAST extends ASTNode {
    @Override
    public void emit(CodeGenerator<CommandSourceStack> generator) {
        generator.emitControl(RetInstr.get());
    }

    @Override
    public void visit(Consumer<ASTNode> visitor) {
    }

    @Override
    public String toString() {
        return "ReturnAST";
    }
}