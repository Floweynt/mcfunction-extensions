package com.floweytf.mcfext.execution;

import com.floweytf.mcfext.execution.instr.ControlInstr;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntStack;
import org.jetbrains.annotations.NotNull;

/**
 * Represents the function-local "register" and "stack" state during function execution.
 *
 * <p>
 * Because minecraft's function execution system resembles a "bytecode" level machine (stream of instructions),
 * it's useful to model complex execution behavior as a stack machine. However, it's important to note that stack
 * machine does not have a specific instruction set, since arbitrary behavior can be achieved by simply implementing the
 * {@link ControlInstr} interface. "MCFunction assembly code" should be treated as pseudocode, and not actual assembly.
 * </p>
 *
 * <h3>The Stack</h3>
 * Every stack machine has a stack, which is used to store temporary variables. The stack holds data of several types:
 * <ul>
 *     <li>{@code Source}:  A command source. Java type: {@code T}</li>
 *     <li>{@code List<Source>}: A command source. Java type: {@code List<T>}</li>
 *     <li>{@code InstrAddress}: A command source. Java type: {@code int}</li>
 * </ul>
 *
 * <h3>Registers</h3>
 * There are two "control registers" which represent the execution state:
 * <ul>
 *     <li>{@code %source (Source)}: The command source to use, set to modify the source of future instructions.</li>
 *     <li>{@code %ip (InstrAddress)}: The current/next instruction index, can be set to branch.</li>
 *     <li>{@code %\d+ (any)}: A temporary pseudo-register</li>
 * </ul>
 *
 * <h3>Instruction Syntax</h3>
 * In documentation for various features, "mcfunction assembly pseudocode" is used. The syntax is described in the
 * following block.
 * <pre>
 * {@code
 * PUSH[type](...) // Push to the stack
 * POP[type](...) // Pop from the stack
 * PEEK[type](...) // Peek from the stack
 * %reg = ... // Store to register
 * target: // Declare target
 * %reg = &target // Address of target
 *
 * // Additionally, the following "macros" are defined:
 * BR(addr) ::= %ip = addr
 * BR_COND(flag, addr) ::= %ip = flag ? addr : (%ip + 1)
 *
 * CALL(addr) ::= {
 *     PUSH[InstrAddress](%ip)
 *     %ip = addr
 * }
 *
 * RET ::= %ip = POP{InstrAddress}
 * </pre>
 *
 * <h3>Base Pointer Stack</h3>
 * The base pointer stack is only really used for subroutine calls. It stores where to pop the stack to when
 * returning from a subroutine. This is "similar" to the %rbp/%ebp register on x86.
 *
 * @param <T> The command source type.
 */
public class FuncExecState<T> {
    public final FuncExecStack<T> stack = new FuncExecStack<>();
    public final IntStack basePointerStack = new IntArrayList();

    @NotNull
    public T source;
    public int instr = 0;

    public FuncExecState(@NotNull T source) {
        this.source = source;
    }

    public void pushSource() {
        stack.pushSource(source);
    }

    public void popSource() {
        source = stack.popSource();
    }

    @Override
    public String toString() {
        return String.format("[instr = %d, stack = %s]", instr, stack);
    }
}
