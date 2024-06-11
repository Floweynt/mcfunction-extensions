package com.floweytf.mcfext.parse.ast.subroutine;

import com.floweytf.mcfext.codegen.CodeGenerator;
import com.floweytf.mcfext.execution.instr.SubroutineCallInstr;
import com.floweytf.mcfext.parse.ParseContext;
import com.floweytf.mcfext.parse.ast.ASTNode;
import com.floweytf.mcfext.parse.ast.CodegenContext;
import net.minecraft.commands.CommandSourceStack;

import java.util.List;
import java.util.function.Consumer;

public class SubroutineCallAST extends ASTNode {
    private static final String ERR_SUBROUTINE_NOT_DEFINED = "subroutine '%s' not defined";

    private final String name;
    private final int lineNo;

    public SubroutineCallAST(String name, int lineNo) {
        this.name = name;
        this.lineNo = lineNo;
    }

    @Override
    public void emit(ParseContext parseCtx, CodegenContext codegenCtx, CodeGenerator<CommandSourceStack> generator) {
        if (!codegenCtx.subroutines().containsKey(name)) {
            parseCtx.reportErr(lineNo, ERR_SUBROUTINE_NOT_DEFINED, name);
        }

        final var targetLabel = codegenCtx.subroutines().get(name);
        generator.emitControlLinkable(List.of(targetLabel), () -> new SubroutineCallInstr<>(targetLabel.offset()));
    }

    @Override
    public void visit(Consumer<ASTNode> visitor) {

    }

    @Override
    public String toString() {
        return "SubroutineCallAST[" + name + "]";
    }
}
