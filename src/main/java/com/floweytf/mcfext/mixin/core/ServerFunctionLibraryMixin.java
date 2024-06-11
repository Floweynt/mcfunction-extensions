package com.floweytf.mcfext.mixin.core;

import com.floweytf.mcfext.parse.parser.Parser;
import com.google.common.collect.ImmutableMap;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.ExecutionCommandSource;
import net.minecraft.commands.functions.CommandFunction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ServerFunctionLibrary;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(ServerFunctionLibrary.class)
public class ServerFunctionLibraryMixin {
    @Shadow
    @Final
    private static Logger LOGGER;

    @SuppressWarnings("unchecked")
    @Redirect(
        method = "lambda$reload$2",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/commands/functions/CommandFunction;fromLines" +
                "(Lnet/minecraft/resources/ResourceLocation;Lcom/mojang/brigadier/CommandDispatcher;" +
                "Lnet/minecraft/commands/ExecutionCommandSource;Ljava/util/List;)" +
                "Lnet/minecraft/commands/functions/CommandFunction;"
        )
    )
    private <T extends ExecutionCommandSource<T>> CommandFunction<T> monumenta$redirectFunctionParsing(
        ResourceLocation id, CommandDispatcher<T> dispatcher, T source,
        List<String> lines
    ) {
        return (CommandFunction<T>) Parser.compileFunction(
            (CommandDispatcher<CommandSourceStack>) dispatcher,
            (CommandSourceStack) source, lines, id
        );
    }

    @Inject(
        method = "lambda$reload$5",
        at = @At(
            value = "INVOKE",
            target = "Lcom/google/common/collect/ImmutableMap$Builder;put(Ljava/lang/Object;Ljava/lang/Object;)" +
                "Lcom/google/common/collect/ImmutableMap$Builder;"
        )
    )
    private static void monumenta$redirectFunctionParsing(ResourceLocation resourceLocation,
                                                          ImmutableMap.Builder<?, ?> builder,
                                                          CommandFunction<?> function, Throwable ex,
                                                          CallbackInfoReturnable<Object> cir) {
        if (function == null) {
            LOGGER.error("Failed to parsing function '{}'! See logs for details.", resourceLocation);
        }
    }


    @WrapOperation(
        method = "lambda$reload$5",
        at = @At(
            value = "INVOKE",
            target = "Lcom/google/common/collect/ImmutableMap$Builder;put(Ljava/lang/Object;Ljava/lang/Object;)" +
                "Lcom/google/common/collect/ImmutableMap$Builder;"
        )
    )
    private static ImmutableMap.Builder<?, ?> monumenta$redirectFunctionParsing(ImmutableMap.Builder<?, ?> instance,
                                                                                Object key, Object value,
                                                                                Operation<ImmutableMap.Builder<?, ?>> original) {
        if (value != null) {
            original.call(instance, key, value);
        }

        return instance;
    }
}