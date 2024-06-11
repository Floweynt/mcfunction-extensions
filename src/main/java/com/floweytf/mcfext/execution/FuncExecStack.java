package com.floweytf.mcfext.execution;

import it.unimi.dsi.fastutil.Pair;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * The local state stack for function execution.
 * <p>
 * MCFunction uses a stack machine execution model for implementing control flow.
 * See <a href="https://en.wikipedia.org/wiki/Stack_machine">wikipedia</a>.
 *
 * @see FuncExecState
 */
public class FuncExecStack<T> {
    public enum Type {
        CONTEXT_LIST,
        CONTEXT,
        INSTR_ADDRESS
    }

    private final ArrayDeque<Pair<Type, Object>> stack = new ArrayDeque<>();

    public void pushSourceList(List<T> entry) {
        stack.push(Pair.of(Type.CONTEXT_LIST, entry));
    }

    public void pushSource(T entry) {
        stack.push(Pair.of(Type.CONTEXT, entry));
    }

    public void pushInstrAddress(int entry) {
        stack.push(Pair.of(Type.INSTR_ADDRESS, entry));
    }

    @SuppressWarnings("unchecked")
    public List<T> popSourceList() {
        final var ent = stack.pop();
        if (ent.first() != Type.CONTEXT_LIST) {
            throw new IllegalStateException("Stack type mismatch");
        }
        return (List<T>) ent.second();
    }

    @SuppressWarnings("unchecked")
    public T popSource() {
        final var ent = stack.pop();
        if (ent.first() != Type.CONTEXT) {
            throw new IllegalStateException("Stack type mismatch");
        }
        return (T) ent.second();
    }

    public int popInstrAddress() {
        final var ent = stack.pop();
        if (ent.first() != Type.INSTR_ADDRESS) {
            throw new IllegalStateException("Stack type mismatch");
        }
        return (Integer) ent.second();
    }

    @SuppressWarnings("unchecked")
    public List<T> peekSourceList() {
        final var ent = Objects.requireNonNull(stack.peek());
        if (ent.first() != Type.CONTEXT_LIST) {
            throw new IllegalStateException("Stack type mismatch");
        }
        return (List<T>) ent.second();
    }

    @SuppressWarnings("unchecked")
    public T peekSource() {
        final var ent = Objects.requireNonNull(stack.peek());
        if (ent.first() != Type.CONTEXT) {
            throw new IllegalStateException("Stack type mismatch");
        }
        return (T) ent.second();
    }

    public int peekInstrAddress() {
        final var ent = Objects.requireNonNull(stack.peek());
        if (ent.first() != Type.INSTR_ADDRESS) {
            throw new IllegalStateException("Stack type mismatch");
        }
        return (Integer) ent.second();
    }

    @SuppressWarnings("unchecked")
    @Override
    public String toString() {
        return "[" + stack.stream().map(x -> switch (x.first()) {
            case CONTEXT -> "Context";
            case CONTEXT_LIST -> "ContextList[" + ((List<T>) x.second()).size() + "]";
            case INSTR_ADDRESS -> "InstrAddress[" + x.second() + "]";
        }).collect(Collectors.joining(", ")) + "]";
    }

    public void discard() {
        stack.pop();
    }

    public int size() {
        return stack.size();
    }
}