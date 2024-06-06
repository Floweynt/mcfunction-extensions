package org.example.mixin.core;

import net.minecraft.SharedConstants;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

@Mixin(SharedConstants.class)
public class SharedConstantsMixin {
    @Shadow
    public static boolean IS_RUNNING_IN_IDE;

    @Inject(
        method = "<clinit>",
        at = @At("TAIL")
    )
    private static void setIsRunningInIde(CallbackInfo ci) {
        IS_RUNNING_IN_IDE = Objects.equals(System.getenv("IS_DEVENV"), "1");
    }
}
