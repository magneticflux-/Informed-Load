package io.github.giantnuker.fabric.informedload.mixin;

import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Created by Mitchell Skaggs on 3/12/2020.
 */

@Pseudo
@Mixin(targets = "net.optifine.Config")
public class OptifineConfigMixin {

    @Inject(method = "Lnet/optifine/Config;hasResource(Lnet/minecraft/resource/ResourceManager;Lnet/minecraft/util/Identifier;)Z", at = @At("HEAD"), cancellable = true, remap = false)
    private static void cancelIfNull(ResourceManager resourceManager, Identifier location, CallbackInfoReturnable<Boolean> cir) {
        if (resourceManager == null)
            cir.cancel();
    }
}
