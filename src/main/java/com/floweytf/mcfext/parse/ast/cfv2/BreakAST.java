package com.floweytf.mcfext.parse.ast.cfv2;

import com.floweytf.mcfext.codegen.CodeGenerator;
import com.floweytf.mcfext.parse.Diagnostics;
import com.floweytf.mcfext.parse.ast.ASTNode;
import com.floweytf.mcfext.parse.ast.CodegenContext;
import net.minecraft.commands.CommandSourceStack;

import java.util.function.Consumer;

public class BreakAST extends ASTNode {
    private final int lineNo;

    public BreakAST(int lineNo) {
        this.lineNo = lineNo;
    }

    @Override
    public void emit(Diagnostics diagnostics, CodegenContext cgCtx, CodeGenerator<CommandSourceStack> gen) {
        if (cgCtx.breakExitLabel() == null) {
            diagnostics.reportErr(lineNo, "'break' may only be used inside iterative control flow");
        }
    }

    @Override
    public void visit(Consumer<ASTNode> visitor) {

    }
}
