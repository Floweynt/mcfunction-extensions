package org.example.mixin.core;

import net.minecraft.SharedConstants;
import net.minecraft.server.dedicated.DedicatedServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DedicatedServer.class)
public class DedicatedServerMixin {
    @Inject(
        method = "initServer",
        at = @At(
            value = "INVOKE",
            target = "Lorg/slf4j/Logger;info(Ljava/lang/String;)V",
            ordinal = 0
        )
    )
    public void printInfo(CallbackInfoReturnable<Boolean> cir) {
        System.out.println("Hello, world! (from mixin)");
        SharedConstants.IS_RUNNING_IN_IDE = true;
    }
}
