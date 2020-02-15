package io.github.giantnuker.fabric.informedload.mixin;

import io.github.giantnuker.fabric.informedload.InformedEntrypointHandler;
import io.github.giantnuker.fabric.informedload.InformedLoadUtils;
import net.minecraft.Bootstrap;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.RunArgs;
import net.minecraft.client.WindowSettings;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.WindowProvider;
import net.minecraft.util.Util;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.LongSupplier;

@Mixin(MinecraftClient.class)
public abstract class MixinMinecraftClient {

    @Shadow @Final private TextureManager textureManager;
    @Shadow @Final private WindowProvider windowProvider;

    @Shadow @Final private net.minecraft.client.util.Window window;

    @Shadow @Final private Framebuffer framebuffer;

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/Bootstrap;initialize()V", ordinal = 0))
    private void grabArgs(RunArgs args) {
        Bootstrap.initialize();
        InformedEntrypointHandler.args = args;
    }
    @Inject(method = "getTextureManager", at = @At("HEAD"), cancellable = true)
    public void changeTextureManager(CallbackInfoReturnable<TextureManager> cir) {
        if (textureManager == null) cir.setReturnValue(InformedLoadUtils.textureManager);
    }
    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;startTimerHackThread()V", ordinal = 0))
    private void ignore1(MinecraftClient minecraftClient) {}
    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;initBackendSystem()Ljava/util/function/LongSupplier;", ordinal = 0))
    private LongSupplier ignore2(RunArgs args) { return Util.nanoTimeSupplier; }
    @Redirect(method = "<init>", at = @At(value = "NEW", target = "net/minecraft/client/util/WindowProvider", ordinal = 0))
    private WindowProvider ignore3(MinecraftClient minecraftClient) { return windowProvider; }
    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/WindowProvider;createWindow(Lnet/minecraft/client/WindowSettings;Ljava/lang/String;Ljava/lang/String;)Lnet/minecraft/client/util/Window;", ordinal = 0))
    private Window ignore4(WindowProvider windowProvider, WindowSettings windowSettings, String string, String string2) { return window; }
    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;onWindowFocusChanged(Z)V", ordinal = 0))
    private void ignore5(MinecraftClient minecraftClient, boolean focused) {}
    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/Window;setFramerateLimit(I)V", ordinal = 0))
    private void ignore6(Window window, int framerateLimit) {}
    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;initRenderer(IZ)V", ordinal = 0))
    private void ignore7(int debugVerbosity, boolean debugSync) {}
    @Redirect(method = "<init>", at = @At(value = "NEW", target = "net/minecraft/client/gl/Framebuffer", ordinal = 0))
    private Framebuffer ignore8(int width, int height, boolean useDepth, boolean getError) { return framebuffer; }
}
