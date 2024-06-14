package com.floweytf.mcfext.codegen;

import com.floweytf.mcfext.execution.instr.ControlInstr;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntObjectPair;
import net.minecraft.commands.ExecutionCommandSource;
import net.minecraft.commands.execution.UnboundEntryAction;
import net.minecraft.commands.functions.CommandFunction;
import net.minecraft.commands.functions.FunctionBuilder;
import net.minecraft.commands.functions.MacroFunction;
import net.minecraft.commands.functions.StringTemplate;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Supplier;

/**
 * The main code generation class, used to emit "instructions." Supports address labels for advanced control flow
 * constructs.
 */
public class CodeGenerator<T extends ExecutionCommandSource<T>> {
    protected final FunctionBuilder<T> builder = new FunctionBuilder<>() {
        @Override
        public void addMacro(@NotNull String command, int lineNum) {
            if (this.plainEntries != null) {
                this.macroEntries = new ArrayList<>(this.plainEntries.size() + 1);

                for (final var action : this.plainEntries) {
                    if (action == null) {
                        this.macroEntries.add(null);
                    } else {
                        this.macroEntries.add(new MacroFunction.PlainTextEntry<>(action));
                    }
                }

                this.plainEntries = null;
            }

            super.addMacro(command, lineNum);
        }
    };
    protected final List<IntObjectPair<Linkable<T>>> linkables = new ArrayList<>();
    private int currLabelId = 0;

    public Label defineLabel(String name) {
        return new Label(currLabelId++, name);
    }

    public Label defineLabel() {
        return new Label(currLabelId++, null);
    }

    public void emitPlain(UnboundEntryAction<T> instr) {
        builder.addCommand(instr);
    }

    public void emitControl(ControlInstr<T> instr) {
        builder.addCommand(instr);
    }

    public void emitControlNamed(String name, ControlInstr.StateModifier<T> instr) {
        emitControl(ControlInstr.named(name, instr));
    }

    public void emitMacro(String data, int lineNo) {
        builder.addMacro(data, lineNo);
    }

    public void emitMacroCustom(String data, BiFunction<StringTemplate, IntList, MacroFunction.Entry<T>> entry) {
        builder.addMacro(data, 0);
        // hack
        assert builder.macroEntries != null;
        final var lastIndex = builder.macroEntries.size() - 1;
        final var macroEntry = (MacroFunction.MacroEntry<T>) builder.macroEntries.get(lastIndex);
        builder.macroEntries.set(lastIndex, entry.apply(macroEntry.template, macroEntry.parameters));
    }

    public void emitLinkable(Linkable<T> linkable) {
        linkables.add(IntObjectPair.of(nextInstrIndex(), linkable));
        (builder.plainEntries == null ? builder.macroEntries : builder.plainEntries).add(null);
    }

    public void emitControlLinkable(Supplier<ControlInstr<T>> gen) {
        emitLinkable(Linkable.wrap(gen));
    }

    public void emitControlLinkable(List<Label> targets, Supplier<ControlInstr<T>> gen) {
        emitLinkable(Linkable.wrap(targets, gen));
    }

    public void emitLabel(Label label) {
        label.offset = nextInstrIndex();
    }

    public int nextInstrIndex() {
        return builder.plainEntries == null ? builder.macroEntries.size() : builder.plainEntries.size();
    }

    public CommandFunction<T> define(ResourceLocation id) {
        // linking :3
        for (var linkableInfo : linkables) {
            if (builder.plainEntries != null) {
                builder.plainEntries.set(linkableInfo.firstInt(), linkableInfo.second().link());
            } else {
                assert builder.macroEntries != null;
                builder.macroEntries.set(linkableInfo.firstInt(),
                    new MacroFunction.PlainTextEntry<>(linkableInfo.second().link()));
            }
        }

        return builder.build(id);
    }

    public String dumpDisassembly() {
        throw new IllegalStateException();
    }
}
