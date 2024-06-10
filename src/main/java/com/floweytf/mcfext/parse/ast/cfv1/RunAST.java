package com.floweytf.mcfext.parse.ast.cfv1;

import com.floweytf.mcfext.codegen.CodeGenerator;
import com.floweytf.mcfext.codegen.Linkable;
import com.floweytf.mcfext.execution.ControlFlowEvalSource;
import com.floweytf.mcfext.execution.instr.ControlInstr;
import com.floweytf.mcfext.parse.ast.ASTNode;
import com.floweytf.mcfext.parse.ast.BlockAST;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.execution.UnboundEntryAction;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * <p>
 * <h3>Pseudocode</h3>
 * <pre>
 * {@code
 * var commandSources = runSelector();
 * for(var source : commandSources) {
 *     runBody();
 * }
 * }
 * </pre>
 * <h3>Pseudocode (assembly)</h3>
 * <pre>
 * {@code
 *   PUSH[Source](%source)
 *   PUSH[SourceList](runSelectors())
 * loop_begin:
 *   %0 = PEEK[Source](%source)
 *   BR_COND(%0.isEmpty(), &loop_exit)
 *   %source = %0.popFront()
 *
 *   // ... 'run' statement body
 *
 *   BR(&loop_begin)
 * loop_exit:
 *   POP[SourceList]()
 *   %source = POP[Source]()
 * }
 * </pre>
 */
public class RunAST extends ASTNode {
    private final BlockAST body;
    private final UnboundEntryAction<CommandSourceStack> action;

    public RunAST(BlockAST body, UnboundEntryAction<CommandSourceStack> action) {
        this.body = body;
        this.action = action;
    }

    @Override
    public void emit(CodeGenerator<CommandSourceStack> generator) {
        final var loopBegin = generator.defineLabel("cfv1$run$loop_begin");
        final var loopExit = generator.defineLabel("cfv1$run$loop_exit");

        // PUSH[Source](%source)
        // PUSH[SourceList](runSelectors())
        generator.emitControlNamed("cfv1::run::push_source_and_match", (state, context, frame) -> {
            state.stack.pushSource(state.source);
            state.stack.pushSourceList(new ArrayList<>());
            action.execute(new ControlFlowEvalSource(state.source, state), context, frame);
        });

        // loop_begin:
        generator.emitLabel(loopBegin);

        // %0 = PEEK[Source](%source)
        // BR_COND(%0.isEmpty(), &loop_exit)
        // %source = %0.popFront()
        generator.emitControlLinkable(List.of(loopExit), () -> {
            final var loopExitTarget = loopExit.offset();
            return ControlInstr.named("cfv1::run::pop_source_or_branch", (state, context, frame) -> {
                if (state.stack.peekSourceList().isEmpty()) {
                    state.instr = loopExitTarget;
                    return;
                }

                final List<CommandSourceStack> list = state.stack.popSourceList();
                state.stack.pushSourceList(list.subList(1, list.size()));
                state.source = list.get(0);
            });
        });

        // body
        body.emit(generator);

        // BR(&loop_begin)
        generator.emitLinkable(Linkable.branch(loopBegin));

        // loop_exit:
        generator.emitLabel(loopExit);

        // POP[SourceList]()
        // %source = POP[Source]()
        generator.emitControlNamed("cfv1::run::cleanup", (state, context, frame) -> {
            state.stack.popSourceList();
            state.source = state.stack.popSource();
        });
    }

    @Override
    public void visit(Consumer<ASTNode> visitor) {
        visitor.accept(body);
    }

    @Override
    public String toString() {
        return "RunAST[" + action + "]";
    }
}
