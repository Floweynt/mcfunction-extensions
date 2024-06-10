package com.floweytf.mcfext.execution.instr;

import com.floweytf.mcfext.execution.FuncExecState;
import net.minecraft.commands.execution.ExecutionContext;
import net.minecraft.commands.execution.Frame;

/**
 * Subroutine call instruction.
 * <h3>Pseudocode (assembly)</h3>
 * <pre>
 * {@code
 * PUSH[InstrAddress](%ip)
 * %ip = target
 * }
 * </pre>
 *
 * @param target The instruction to jump to.
 * @param <T>    The command source.
 */
public record CallInstr<T>(int target) implements ControlInstr<T> {
    @Override
    public void modifyState(FuncExecState<T> state, ExecutionContext<T> context, Frame frame) {
        state.stack.pushInstrAddress(state.instr);
        state.instr = target;
    }

    @Override
    public String toString() {
        return "builtin::call[" + target + "]";
    }
}
