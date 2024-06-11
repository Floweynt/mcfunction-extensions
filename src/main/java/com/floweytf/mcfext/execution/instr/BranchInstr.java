package com.floweytf.mcfext.execution.instr;

import com.floweytf.mcfext.execution.FuncExecState;
import net.minecraft.commands.execution.ExecutionContext;
import net.minecraft.commands.execution.Frame;

/**
 * Static jump instruction.
 * <h3>Pseudocode (assembly)</h3>
 * <pre>
 * {@code
 * %ip = target
 * }
 * </pre>
 *
 * @param target The instruction to jump to.
 */
public record BranchInstr<T>(int target) implements ControlInstr<T> {
    private static final BranchInstr<?> EXIT = new BranchInstr<>(Integer.MAX_VALUE);

    @SuppressWarnings("unchecked")
    public static <T> BranchInstr<T> exit() {
        return (BranchInstr<T>) EXIT;
    }

    @Override
    public void modifyState(FuncExecState<T> state, ExecutionContext<T> context, Frame frame) {
        state.instr = target;
    }

    @Override
    public String toString() {
        return "builtin::br[" + target + "]";
    }
}
