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

public class ControlFlowEvalSource extends CommandSourceStack {
    private final FuncExecState<CommandSourceStack> execState;

    public ControlFlowEvalSource(CommandSourceStack stack, FuncExecState<CommandSourceStack> execState) {
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
    public @NotNull ControlFlowEvalSource withAnchor(@NotNull EntityAnchorArgument.Anchor anchor) {
        return new ControlFlowEvalSource(super.withAnchor(anchor), execState);
    }

    @Override
    public @NotNull ControlFlowEvalSource withCallback(@NotNull CommandResultCallback returnValueConsumer) {
        return new ControlFlowEvalSource(super.withCallback(returnValueConsumer), execState);
    }

    @Override
    public @NotNull ControlFlowEvalSource withCallback(@NotNull CommandResultCallback returnValueConsumer,
                                                       @NotNull BinaryOperator<CommandResultCallback> merger) {
        return new ControlFlowEvalSource(super.withCallback(returnValueConsumer, merger), execState);
    }

    @Override
    public @NotNull ControlFlowEvalSource withEntity(@NotNull Entity entity) {
        return new ControlFlowEvalSource(super.withEntity(entity), execState);
    }

    @Override
    public @NotNull ControlFlowEvalSource withLevel(@NotNull ServerLevel world) {
        return new ControlFlowEvalSource(super.withLevel(world), execState);
    }

    @Override
    public @NotNull ControlFlowEvalSource withMaximumPermission(int level) {
        return new ControlFlowEvalSource(super.withMaximumPermission(level), execState);
    }

    @Override
    public @NotNull ControlFlowEvalSource withPermission(int level) {
        return new ControlFlowEvalSource(super.withPermission(level), execState);
    }

    @Override
    public @NotNull ControlFlowEvalSource withPosition(@NotNull Vec3 position) {
        return new ControlFlowEvalSource(super.withPosition(position), execState);
    }

    @Override
    public @NotNull ControlFlowEvalSource withRotation(@NotNull Vec2 rotation) {
        return new ControlFlowEvalSource(super.withRotation(rotation), execState);
    }

    @Override
    public @NotNull ControlFlowEvalSource withSigningContext(@NotNull CommandSigningContext signedArguments,
                                                             @NotNull TaskChainer messageChainTaskQueue) {
        return new ControlFlowEvalSource(super.withSigningContext(signedArguments, messageChainTaskQueue),
            execState);
    }

    @Override
    public @NotNull ControlFlowEvalSource withSource(@NotNull CommandSource output) {
        return new ControlFlowEvalSource(super.withSource(output), execState);
    }

    @Override
    public @NotNull ControlFlowEvalSource withSuppressedOutput() {
        return new ControlFlowEvalSource(super.withSuppressedOutput(), execState);
    }
}