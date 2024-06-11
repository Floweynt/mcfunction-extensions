package com.floweytf.mcfext.parse.ast;

import com.floweytf.mcfext.codegen.CodeGenerator;
import com.floweytf.mcfext.execution.instr.BranchInstr;
import com.floweytf.mcfext.parse.ParseContext;
import com.floweytf.mcfext.parse.ast.subroutine.SubroutineDefinitionAST;
import net.minecraft.commands.CommandSourceStack;

import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

public class TopLevelAST extends ASTNode {
    private final BlockAST block;
    private final List<SubroutineDefinitionAST> subroutines;

    public TopLevelAST(BlockAST block, List<SubroutineDefinitionAST> subroutines) {
        this.block = block;
        this.subroutines = subroutines;
    }

    @Override
    public void emit(ParseContext parseCtx, CodegenContext codegenCtx, CodeGenerator<CommandSourceStack> generator) {
        final var map = new HashMap<String, SubroutineDefinitionAST>();

        for (final var subroutine : subroutines) {
            if (map.containsKey(subroutine.name())) {
                parseCtx.reportErr(
                    subroutine.line(),
                    "re-definition of subroutine (previously defined on line %d",
                    map.get(subroutine.name()).line()
                );
                continue;
            }

            map.put(subroutine.name(), subroutine);
        }

        map.forEach((name, ast) -> codegenCtx.subroutines().put(name, generator.defineLabel("subroutine_" + name)));

        block.emit(parseCtx, codegenCtx, generator);
        if (!subroutines.isEmpty()) {
            generator.emitControl(BranchInstr.exit());
        }

        for (final var subroutine : subroutines) {
            subroutine.emit(parseCtx, codegenCtx, generator);
        }
    }

    @Override
    public void visit(Consumer<ASTNode> visitor) {
        subroutines.forEach(visitor);
        visitor.accept(block);
    }

    @Override
    public String toString() {
        return "TopLevelAST";
    }
}
