package com.floweytf.mcfext.codegen;

import com.floweytf.mcfext.execution.instr.BranchInstr;
import com.floweytf.mcfext.execution.instr.CallInstr;
import com.floweytf.mcfext.execution.instr.ControlInstr;
import com.floweytf.mcfext.execution.instr.PushInstrAddrInstr;
import net.minecraft.commands.ExecutionCommandSource;
import net.minecraft.commands.execution.UnboundEntryAction;

import java.util.List;
import java.util.function.Supplier;

public interface Linkable<T> {
    default List<Label> targets() {
        return List.of();
    }

    UnboundEntryAction<T> link();

    static <T extends ExecutionCommandSource<T>> Linkable<T> call(Label target) {
        return wrap(List.of(target), () -> new CallInstr<>(target.offset()));
    }

    static <T extends ExecutionCommandSource<T>> Linkable<T> branch(Label target) {
        return wrap(List.of(target), () -> new BranchInstr<>(target.offset()));
    }

    static <T extends ExecutionCommandSource<T>> Linkable<T> pushInstrAddr(Label target) {
        return wrap(List.of(target), () -> new PushInstrAddrInstr<>(target.offset()));
    }

    static <T extends ExecutionCommandSource<T>> Linkable<T> exit() {
        return wrap(() -> new BranchInstr<>(Integer.MAX_VALUE));
    }

    static <T> Linkable<T> wrap(Supplier<ControlInstr<T>> gen) {
        return gen::get;
    }

    static <T> Linkable<T> wrap(List<Label> targets, Supplier<ControlInstr<T>> gen) {
        return new Linkable<>() {
            @Override
            public List<Label> targets() {
                return targets;
            }

            @Override
            public UnboundEntryAction<T> link() {
                return gen.get();
            }
        };
    }
}
