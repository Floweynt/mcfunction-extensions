package com.floweytf.mcfext.execution.instr;

import com.floweytf.mcfext.execution.FuncExecState;
import net.minecraft.commands.execution.ExecutionContext;
import net.minecraft.commands.execution.Frame;

public record SubroutineCallInstr<T>(int target) implements ControlInstr<T> {
    @Override
    public void modifyState(FuncExecState<T> state, ExecutionContext<T> context, Frame frame) {
        state.stack.pushInstrAddress(state.instr);
        state.pushSource();
        state.basePointerStack.push(state.stack.size());
        state.instr = target;
    }

    @Override
    public String toString() {
        return "subroutine::call[" + target + "]";
    }
}
