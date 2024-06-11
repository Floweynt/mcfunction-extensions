package com.floweytf.mcfext.execution.instr;

import com.floweytf.mcfext.execution.FuncExecState;
import net.minecraft.commands.execution.ExecutionContext;
import net.minecraft.commands.execution.Frame;
import net.minecraft.commands.execution.UnboundEntryAction;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a special control flow instruction within a "compiled" MCFunction.
 *
 * @see FuncExecState
 */
@FunctionalInterface
public interface ControlInstr<T> extends UnboundEntryAction<T> {
    interface StateModifier<T> {
        void modifyState(FuncExecState<T> state, ExecutionContext<T> context, Frame frame);
    }

    @Override
    default void execute(@NotNull T source, @NotNull ExecutionContext<T> context,
                         @NotNull Frame frame) {
    }

    /**
     * Perform special state modification operations.
     *
     * @param state   The current state, with {@link FuncExecState#instr} pointing to the <b>next</b> instr.
     * @param context The current execution context, same as {@link UnboundEntryAction#execute}.
     * @param frame   The current frame, same as {@link UnboundEntryAction#execute}.
     */
    void modifyState(FuncExecState<T> state, ExecutionContext<T> context, Frame frame);

    static <T> ControlInstr<T> named(String name, StateModifier<T> modifier) {
        return new ControlInstr<>() {
            @Override
            public void modifyState(FuncExecState<T> state, ExecutionContext<T> context, Frame frame) {
                modifier.modifyState(state, context, frame);
            }

            @Override
            public String toString() {
                return name;
            }
        };
    }
}