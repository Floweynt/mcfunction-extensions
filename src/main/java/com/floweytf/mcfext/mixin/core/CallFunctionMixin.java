package com.floweytf.mcfext.mixin.core;

import com.floweytf.mcfext.execution.FuncExecTask;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.commands.ExecutionCommandSource;
import net.minecraft.commands.execution.ExecutionContext;
import net.minecraft.commands.execution.Frame;
import net.minecraft.commands.execution.UnboundEntryAction;
import net.minecraft.commands.execution.tasks.CallFunction;
import net.minecraft.commands.execution.tasks.ContinuationTask;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;

@Mixin(CallFunction.class)
public class CallFunctionMixin<T extends ExecutionCommandSource<T>> {
    @Redirect(
        method = "execute(Lnet/minecraft/commands/ExecutionCommandSource;" +
            "Lnet/minecraft/commands/execution/ExecutionContext;Lnet/minecraft/commands/execution/Frame;)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/commands/execution/tasks/ContinuationTask;schedule" +
                "(Lnet/minecraft/commands/execution/ExecutionContext;Lnet/minecraft/commands/execution/Frame;" +
                "Ljava/util/List;Lnet/minecraft/commands/execution/tasks/ContinuationTask$TaskProvider;)V"
        )
    )
    private void monumenta$useFuncExecTask(
        ExecutionContext<T> exec,
        Frame frame,
        List<UnboundEntryAction<T>> actions,
        ContinuationTask.TaskProvider<T, UnboundEntryAction<T>> wrapper,
        @Local(argsOnly = true, ordinal = 0) T context
    ) {
        FuncExecTask.schedule(exec, frame, actions, context);
    }
}