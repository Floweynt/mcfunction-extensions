package com.floweytf.mcfext.util;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.RedirectModifier;
import com.mojang.brigadier.SingleRedirectModifier;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

import java.util.function.Function;
import java.util.function.Predicate;

public class CommandUtil {
    @SafeVarargs
    public static <A, T> RequiredArgumentBuilder<A, T> arg(
        String key, ArgumentType<T> arg, ArgumentBuilder<A, ?>... callbacks
    ) {
        final var inst = RequiredArgumentBuilder.<A, T>argument(key, arg);
        for (var callback : callbacks) {
            inst.then(callback);
        }
        return inst;
    }

    @SafeVarargs
    public static <A, T> RequiredArgumentBuilder<A, T> arg(
        String key, ArgumentType<T> arg, Command<A> execute, ArgumentBuilder<A, ?>... callbacks
    ) {
        final var inst = RequiredArgumentBuilder.<A, T>argument(key, arg);
        for (var callback : callbacks) {
            inst.then(callback);
        }
        inst.executes(execute);
        return inst;
    }

    @SafeVarargs
    public static <A, T> RequiredArgumentBuilder<A, T> argF(
        String key, ArgumentType<T> arg, CommandNode<A> target, RedirectModifier<A> execute,
        ArgumentBuilder<A, ?>... callbacks
    ) {
        return arg(key, arg, callbacks).fork(target, execute);
    }

    @SafeVarargs
    public static <A, T> RequiredArgumentBuilder<A, T> argR(
        String key, ArgumentType<T> arg, CommandNode<A> target, SingleRedirectModifier<A> execute,
        ArgumentBuilder<A, ?>... callbacks
    ) {
        return arg(key, arg, callbacks).redirect(target, execute);
    }

    @SafeVarargs
    public static <A> LiteralArgumentBuilder<A> lit(
        String key, ArgumentBuilder<A, ?>... callbacks
    ) {
        final var inst = LiteralArgumentBuilder.<A>literal(key);
        for (var callback : callbacks) {
            inst.then(callback);
        }
        return inst;
    }

    @SafeVarargs
    public static <A> LiteralArgumentBuilder<A> lit(
        String key, Command<A> execute, ArgumentBuilder<A, ?>... callbacks
    ) {
        final var inst = LiteralArgumentBuilder.<A>literal(key);
        for (var callback : callbacks) {
            inst.then(callback);
        }
        inst.executes(execute);
        return inst;
    }


    @SafeVarargs
    public static <T> RequiredArgumentBuilder<CommandSourceStack, T> mcArg(
        String key, ArgumentType<T> arg, ArgumentBuilder<CommandSourceStack, ?>... callbacks
    ) {
        return arg(key, arg, callbacks);
    }

    @SafeVarargs
    public static <T> RequiredArgumentBuilder<CommandSourceStack, T> mcArg(
        String key, ArgumentType<T> arg, Command<CommandSourceStack> execute,
        ArgumentBuilder<CommandSourceStack, ?>... callbacks
    ) {
        return arg(key, arg, execute, callbacks);
    }

    @SafeVarargs
    public static LiteralArgumentBuilder<CommandSourceStack> mcLit(
        String key, ArgumentBuilder<CommandSourceStack, ?>... callbacks
    ) {
        return lit(key, callbacks);
    }

    @SafeVarargs
    public static LiteralArgumentBuilder<CommandSourceStack> mcLitPred(
        String key, Predicate<CommandSourceStack> requirement, ArgumentBuilder<CommandSourceStack, ?>... callbacks
    ) {
        final var inst = LiteralArgumentBuilder.<CommandSourceStack>literal(key).requires(requirement);
        for (var callback : callbacks) {
            inst.then(callback);
        }
        return inst;
    }

    @SafeVarargs
    public static LiteralArgumentBuilder<CommandSourceStack> mcLit(
        String key, Command<CommandSourceStack> execute, ArgumentBuilder<CommandSourceStack, ?>... callbacks
    ) {
        return lit(key, execute, callbacks);
    }

    public static DynamicCommandExceptionType exceptionType(Function<Object, Message> function) {
        return new DynamicCommandExceptionType(function);
    }

    public static SimpleCommandExceptionType exceptionType(String message) {
        return new SimpleCommandExceptionType(Component.literal(message));
    }
}