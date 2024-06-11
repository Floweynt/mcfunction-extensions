package com.floweytf.mcfext.execution;

import net.minecraft.commands.CommandResultCallback;
import net.minecraft.commands.CommandSigningContext;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.TaskChainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.function.BinaryOperator;

public class FunctionExecSource extends CommandSourceStack {
    private final FuncExecState<CommandSourceStack> execState;

    public FunctionExecSource(CommandSourceStack stack, FuncExecState<CommandSourceStack> execState) {
        super(
            stack.source,
            stack.getPosition(),
            stack.getRotation(),
            stack.getLevel(),
            stack.permissionLevel,
            stack.getTextName(),
            stack.getDisplayName(),
            stack.getServer(),
            stack.getEntity(),
            stack.isSilent(),
            stack.callback(),
            stack.getAnchor(),
            stack.getSigningContext(),
            stack.getChatMessageChainer()
        );

        this.execState = execState;
    }

    public FuncExecState<CommandSourceStack> getExecState() {
        return execState;
    }

    @Override
    public @NotNull FunctionExecSource withAnchor(@NotNull EntityAnchorArgument.Anchor anchor) {
        return new FunctionExecSource(super.withAnchor(anchor), execState);
    }

    @Override
    public @NotNull FunctionExecSource withCallback(@NotNull CommandResultCallback returnValueConsumer) {
        return new FunctionExecSource(super.withCallback(returnValueConsumer), execState);
    }

    @Override
    public @NotNull FunctionExecSource withCallback(@NotNull CommandResultCallback returnValueConsumer,
                                                    @NotNull BinaryOperator<CommandResultCallback> merger) {
        return new FunctionExecSource(super.withCallback(returnValueConsumer, merger), execState);
    }

    @Override
    public @NotNull FunctionExecSource withEntity(@NotNull Entity entity) {
        return new FunctionExecSource(super.withEntity(entity), execState);
    }

    @Override
    public @NotNull FunctionExecSource withLevel(@NotNull ServerLevel world) {
        return new FunctionExecSource(super.withLevel(world), execState);
    }

    @Override
    public @NotNull FunctionExecSource withMaximumPermission(int level) {
        return new FunctionExecSource(super.withMaximumPermission(level), execState);
    }

    @Override
    public @NotNull FunctionExecSource withPermission(int level) {
        return new FunctionExecSource(super.withPermission(level), execState);
    }

    @Override
    public @NotNull FunctionExecSource withPosition(@NotNull Vec3 position) {
        return new FunctionExecSource(super.withPosition(position), execState);
    }

    @Override
    public @NotNull FunctionExecSource withRotation(@NotNull Vec2 rotation) {
        return new FunctionExecSource(super.withRotation(rotation), execState);
    }

    @Override
    public @NotNull FunctionExecSource withSigningContext(@NotNull CommandSigningContext signedArguments,
                                                          @NotNull TaskChainer messageChainTaskQueue) {
        return new FunctionExecSource(super.withSigningContext(signedArguments, messageChainTaskQueue),
            execState);
    }

    @Override
    public @NotNull FunctionExecSource withSource(@NotNull CommandSource output) {
        return new FunctionExecSource(super.withSource(output), execState);
    }

    @Override
    public @NotNull FunctionExecSource withSuppressedOutput() {
        return new FunctionExecSource(super.withSuppressedOutput(), execState);
    }
}