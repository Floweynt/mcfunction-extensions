package com.floweytf.mcfext.parse.ast.cfv2;

import com.floweytf.mcfext.codegen.CodeGenerator;
import com.floweytf.mcfext.parse.Diagnostic;
import com.floweytf.mcfext.parse.ParseContext;
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
    public void emit(ParseContext parseCtx, CodegenContext codegenCtx, CodeGenerator<CommandSourceStack> generator) {
        if (codegenCtx.breakExitLabel() == null) {
            parseCtx.reportErr(lineNo, Diagnostic.ERR_BAD_BREAK);
        }
    }

    @Override
    public void visit(Consumer<ASTNode> visitor) {

    }
}
