package com.floweytf.mcfext.parse.ast;

import com.floweytf.mcfext.codegen.CodeGenerator;
import com.floweytf.mcfext.parse.Diagnostics;
import net.minecraft.commands.CommandSourceStack;

import java.util.function.Consumer;

public class MacroAST extends ASTNode {
    private final int lineNo;
    private final String text;

    public MacroAST(int lineNo, String text) {
        this.lineNo = lineNo;
        this.text = text;
    }

    @Override
    public void emit(Diagnostics diagnostics, CodegenContext cgCtx, CodeGenerator<CommandSourceStack> gen) {
        gen.emitMacro(text, lineNo);
    }

    @Override
    public void visit(Consumer<ASTNode> visitor) {

    }

    @Override
    public String toString() {
        return "MacroAST[" + lineNo + ", '" + text + "']";
    }
}
