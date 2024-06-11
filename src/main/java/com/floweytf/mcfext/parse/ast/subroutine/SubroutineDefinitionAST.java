package com.floweytf.mcfext.parse.ast.subroutine;

import com.floweytf.mcfext.codegen.CodeGenerator;
import com.floweytf.mcfext.execution.instr.SubroutineRetInstr;
import com.floweytf.mcfext.parse.ParseContext;
import com.floweytf.mcfext.parse.ast.ASTNode;
import com.floweytf.mcfext.parse.ast.BlockAST;
import com.floweytf.mcfext.parse.ast.CodegenContext;
import net.minecraft.commands.CommandSourceStack;

import java.util.function.Consumer;

public class SubroutineDefinitionAST extends ASTNode {
    private final BlockAST body;
    private final String name;
    private final int line;

    public SubroutineDefinitionAST(BlockAST body, String name, int line) {
        this.body = body;
        this.name = name;
        this.line = line;
    }

    public String name() {
        return name;
    }

    public int line() {
        return line;
    }

    @Override
    public void emit(ParseContext parseCtx, CodegenContext codegenCtx, CodeGenerator<CommandSourceStack> generator) {
        generator.emitLabel(codegenCtx.subroutines().get(name));
        body.emit(parseCtx, codegenCtx, generator);
        generator.emitControl(SubroutineRetInstr.get());
    }

    @Override
    public void visit(Consumer<ASTNode> visitor) {
        visitor.accept(body);
    }

    @Override
    public String toString() {
        return "SubroutineDefinitionAST[" + name + "]";
    }
}
