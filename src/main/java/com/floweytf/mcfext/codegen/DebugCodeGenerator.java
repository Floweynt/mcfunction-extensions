package com.floweytf.mcfext.codegen;

import com.floweytf.mcfext.execution.instr.ControlInstr;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.commands.ExecutionCommandSource;
import net.minecraft.commands.execution.UnboundEntryAction;
import net.minecraft.commands.functions.CommandFunction;
import net.minecraft.commands.functions.MacroFunction;
import net.minecraft.commands.functions.StringTemplate;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

public class DebugCodeGenerator<T extends ExecutionCommandSource<T>> extends CodeGenerator<T> {
    private final List<String> disassembly = new ArrayList<>();
    private final IntList linkableDisassemblyIndex = new IntArrayList();

    @Override
    public void emitControl(ControlInstr<T> instr) {
        disassembly.add("  " + instr);
        super.emitControl(instr);
    }

    @Override
    public void emitLabel(Label label) {
        disassembly.add(label + ":");
        super.emitLabel(label);
    }

    @Override
    public void emitLinkable(Linkable<T> linkable) {
        // Logic here is a bit cursed
        linkableDisassemblyIndex.add(disassembly.size());
        disassembly.add(null);
        super.emitLinkable(linkable);
    }

    @Override
    public void emitMacro(String data, int lineNo) {
        disassembly.add("  MACRO " + data);
        super.emitMacro(data, lineNo);
    }

    @Override
    public void emitMacroCustom(String data, BiFunction<StringTemplate, IntList, MacroFunction.Entry<T>> entry) {
        disassembly.add("  MACRO " + data);
        super.emitMacroCustom(data, entry);
    }

    @Override
    public void emitPlain(UnboundEntryAction<T> instr) {
        disassembly.add("  RUN " + instr);
        super.emitPlain(instr);
    }

    @Override
    public CommandFunction<T> define(ResourceLocation id) {
        // linking :3
        for (int i = 0; i < linkables.size(); i++) {
            var linkableInfo = linkables.get(i);
            final var result = linkableInfo.second().link();

            if (builder.plainEntries != null) {
                builder.plainEntries.set(linkableInfo.firstInt(), result);
            } else {
                builder.macroEntries.set(linkableInfo.firstInt(), new MacroFunction.PlainTextEntry<>(result));
            }

            disassembly.set(linkableDisassemblyIndex.getInt(i), "  " + result + " [" + String.join(", ",
                linkableInfo.second().targets().stream().map(Label::toString).toList()) + "]");
        }

        return builder.build(id);
    }

    public String dumpDisassembly() {
        return String.join("\n", disassembly);
    }
}
