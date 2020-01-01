package io.github.giantnuker.fabric.informedload.mixin;

import io.github.giantnuker.fabric.informedload.InformedLoadUtils;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.WorldGenerationProgressListener;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {
    @Shadow public abstract ServerWorld getWorld(DimensionType dimensionType);

    @Redirect(method = "prepareStartRegion", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;getWorld(Lnet/minecraft/world/dimension/DimensionType;)Lnet/minecraft/server/world/ServerWorld;"))
    private ServerWorld logTheWorld(MinecraftServer minecraftServer, DimensionType dimensionType) {
        ServerWorld serverWorld = getWorld(dimensionType);
        if (serverWorld.getClass() == ServerWorld.class) InformedLoadUtils.loadingWorld = serverWorld;
        return serverWorld;
    }
    @Inject(method = "prepareStartRegion", at = @At("RETURN"))
    private void clearILWorld(WorldGenerationProgressListener worldGenerationProgressListener, CallbackInfo ci) {
        InformedLoadUtils.loadingWorld = null;
    }
}
