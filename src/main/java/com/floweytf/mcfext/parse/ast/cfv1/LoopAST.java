package com.floweytf.mcfext.parse.ast.cfv1;

import com.floweytf.mcfext.codegen.CodeGenerator;
import com.floweytf.mcfext.codegen.Linkable;
import com.floweytf.mcfext.execution.FunctionExecSource;
import com.floweytf.mcfext.execution.instr.ControlInstr;
import com.floweytf.mcfext.parse.ControlFlowStatement;
import com.floweytf.mcfext.parse.Diagnostics;
import com.floweytf.mcfext.parse.ast.ASTNode;
import com.floweytf.mcfext.parse.ast.BlockAST;
import com.floweytf.mcfext.parse.ast.CodegenContext;
import net.minecraft.commands.CommandSourceStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * AST for a {@code loop ... { ... }} statement. This is a bit more involved than {@code run},
 * since it's recursive.
 * <p>
 * <h3>Pseudocode</h3>
 * <pre>
 * {@code
 * void anon() {
 *     var commandSources = runSelector();
 *     for(var source : commandSources) {
 *         runBody();
 *         anon();
 *     }
 * }
 * }
 * </pre>
 * <h3>Pseudocode (assembly)</h3>
 * <pre>
 * {@code
 *   PUSH[InstrAddress](&end)
 * wrapper_function:
 *   PUSH[Source](%source)
 *   PUSH[SourceList](runSelectors())
 * loop_begin:
 *   %0 = PEEK[Source](%source)
 *   BR_COND(%0.isEmpty(), &loop_exit)
 *   %source = %0.popFront()
 *
 *   // ... 'run' statement body
 *   CALL(wrapper_function)
 *   BR(&loop_begin)
 * loop_exit:
 *   POP[SourceList]()
 *   %source = POP[Source]()
 *   RET()
 *
 * end:
 * }
 * </pre>
 */
public class LoopAST extends ASTNode {
    private final BlockAST body;
    private final ControlFlowStatement<CommandSourceStack> statement;

    public LoopAST(BlockAST body, ControlFlowStatement<CommandSourceStack> statement) {
        this.body = body;
        this.statement = statement;
    }

    @Override
    public void emit(Diagnostics diagnostics, CodegenContext cgCtx, CodeGenerator<CommandSourceStack> gen) {
        final var wrapperFuncLabel = gen.defineLabel("cfv1$loop$wrapper_func");
        final var loopBeginLabel = gen.defineLabel("cfv1$loop$loop_begin");
        final var loopExitLabel = gen.defineLabel("cfv1$loop$loop_exit");
        final var endLabel = gen.defineLabel("cfv1$loop$end");

        // PUSH[InstrAddress](&end)
        gen.emitLinkable(Linkable.pushInstrAddr(endLabel));

        // wrapper_function:
        gen.emitLabel(wrapperFuncLabel);

        // PUSH[Source](%source)
        // PUSH[SourceList](runSelectors())
        statement.emit(gen, action -> ControlInstr.named(
            "cfv1::loop::push_source_and_match",
            (state, context, frame) -> {
                state.stack.pushSource(state.source);
                state.stack.pushSourceList(new ArrayList<>());
                action.execute(new FunctionExecSource(state.source, state), context, frame);
            }
        ));

        // loop_begin:
        gen.emitLabel(loopBeginLabel);

        // %0 = PEEK[Source](%source)
        // BR_COND(%0.isEmpty(), &loop_exit)
        // %source = %0.popFront()
        gen.emitControlLinkable(List.of(loopExitLabel), () -> {
            final var loopExitTarget = loopExitLabel.offset();
            return ControlInstr.named("cfv1::loop::pop_source_or_branch", (state, context, frame) -> {
                if (state.stack.peekSourceList().isEmpty()) {
                    state.instr = loopExitTarget;
                    return;
                }

                final var list = state.stack.popSourceList();
                state.stack.pushSourceList(list.subList(1, list.size()));
                state.source = list.get(0);
            });
        });

        // ... body
        body.emit(diagnostics, cgCtx, gen);

        // CALL(wrapper_function)
        // BR(&loop_begin)
        gen.emitLinkable(Linkable.call(wrapperFuncLabel));
        gen.emitLinkable(Linkable.branch(loopBeginLabel));

        // loop_exit:
        gen.emitLabel(loopExitLabel);

        // POP[SourceList]()
        // %source = POP[Source]()
        // RET()
        gen.emitControlNamed("cfv1::loop::function_exit", (state, context, frame) -> {
            state.stack.popSourceList();
            state.source = state.stack.popSource();
            state.instr = state.stack.popInstrAddress();
        });

        // end:
        gen.emitLabel(endLabel);
    }

    @Override
    public void visit(Consumer<ASTNode> visitor) {
        visitor.accept(body);
    }

    @Override
    public String toString() {
        return "LoopAST[" + statement + "]";
    }
}
