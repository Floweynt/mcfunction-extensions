package com.floweytf.mcfext.parse;

import com.floweytf.mcfext.codegen.CodeGenerator;
import com.floweytf.mcfext.execution.instr.ControlInstr;
import com.mojang.brigadier.CommandDispatcher;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntObjectPair;
import net.minecraft.commands.ExecutionCommandSource;
import net.minecraft.commands.FunctionInstantiationException;
import net.minecraft.commands.execution.UnboundEntryAction;
import net.minecraft.commands.functions.MacroFunction;
import net.minecraft.commands.functions.StringTemplate;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Function;

public class ControlFlowStatement<T extends ExecutionCommandSource<T>> {
    private ControlFlowStatement(Object data, @Nullable CommandDispatcher<T> dispatcher) {
        this.data = data;
        this.dispatcher = dispatcher;
    }

    private class MyMacroEntry extends MacroFunction.MacroEntry<T> {
        private final Function<UnboundEntryAction<T>, ControlInstr<T>> mapper;

        public MyMacroEntry(StringTemplate t, IntList l, Function<UnboundEntryAction<T>, ControlInstr<T>> mapper) {
            super(t, l);
            this.mapper = mapper;
        }

        @Override
        @NotNull
        public UnboundEntryAction<T> instantiate(
            @NotNull List<String> args, @NotNull CommandDispatcher<T> dispatcher, @NotNull T source,
            @NotNull ResourceLocation id
        ) throws FunctionInstantiationException {
            assert ControlFlowStatement.this.dispatcher != null;
            return mapper.apply(super.instantiate(args, ControlFlowStatement.this.dispatcher, source, id));
        }
    }

    private final Object data;
    @Nullable
    private final CommandDispatcher<T> dispatcher;

    @SuppressWarnings("unchecked")
    public void emit(CodeGenerator<T> generator, Function<UnboundEntryAction<T>, ControlInstr<T>> mapper) {
        if (data instanceof IntObjectPair<?> info) {
            generator.emitMacroCustom((String) info.second(), (s, l) -> new MyMacroEntry(s, l, mapper));
        } else {
            generator.emitControl(mapper.apply((UnboundEntryAction<T>) data));
        }
    }

    public static <T extends ExecutionCommandSource<T>> ControlFlowStatement<T> plain(UnboundEntryAction<T> action) {
        return new ControlFlowStatement<>(action, null);
    }

    public static <T extends ExecutionCommandSource<T>> ControlFlowStatement<T> macro(CommandDispatcher<T> dispatch,
                                                                                      String text, int line) {
        return new ControlFlowStatement<>(IntObjectPair.of(line, text), dispatch);
    }

    @Override
    public String toString() {
        if (data instanceof IntObjectPair<?> info) {
            return "ControlFlowStatement[macro, " + info.second() + "]";
        } else {
            return "ControlFlowStatement[" + data + "]";
        }
    }
}
