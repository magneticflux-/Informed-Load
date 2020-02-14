package io.github.giantnuker.fabric.informedload.mixin;

import io.github.giantnuker.fabric.informedload.InformedEntrypointHandler;
import io.github.giantnuker.fabric.informedload.InformedLoadUtils;
import net.minecraft.Bootstrap;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.RunArgs;
import net.minecraft.client.texture.TextureManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftClient.class)
public abstract class MixinMinecraftClient {

    @Shadow @Final private TextureManager textureManager;
    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/Bootstrap;initialize()V", ordinal = 0))
    private void grabArgs(RunArgs args) {
        Bootstrap.initialize();
        InformedEntrypointHandler.args = args;
    }
    @Inject(method = "getTextureManager", at = @At("HEAD"), cancellable = true)
    public void changeTextureManager(CallbackInfoReturnable<TextureManager> cir) {
        if (textureManager == null) cir.setReturnValue(InformedLoadUtils.textureManager);
    }
}
