package com.floweytf.mcfext.parse.ast;

import com.floweytf.mcfext.codegen.CodeGenerator;
import com.floweytf.mcfext.parse.Diagnostics;
import net.minecraft.commands.CommandSourceStack;

import java.util.function.Consumer;

/**
 * The base Abstract Syntax Tree node for command functions.
 */
public abstract class ASTNode {
    public abstract void emit(Diagnostics diagnostics, CodegenContext cgCtx, CodeGenerator<CommandSourceStack> gen);

    public abstract void visit(Consumer<ASTNode> visitor);

    public String dump() {
        final var builder = new StringBuilder();

        (new Consumer<ASTNode>() {
            private int indent = 0;

            @Override
            public void accept(ASTNode astNode) {
                builder.append("  ".repeat(indent)).append(astNode.toString()).append("\n");
                indent++;
                astNode.visit(this);
                indent--;
            }
        }).accept(this);

        return builder.toString();
    }
}
