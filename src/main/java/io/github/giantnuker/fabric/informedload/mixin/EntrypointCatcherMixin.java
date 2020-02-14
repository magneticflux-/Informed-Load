package io.github.giantnuker.fabric.informedload.mixin;

import io.github.giantnuker.fabric.informedload.InformedEntrypointHandler;
import io.github.giantnuker.fabric.loadcatcher.EntrypointCatcher;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.io.File;

@Mixin(EntrypointCatcher.class)
public class EntrypointCatcherMixin {
    @Redirect(method = "runEntrypointRedirection", at = @At(value = "INVOKE", target = "Lio/github/giantnuker/fabric/loadcatcher/EntrypointCatcher$LoaderClientReplacement;run(Ljava/io/File;Ljava/lang/Object;)V", remap = false), remap = false)
    private static void runEntrypointsThreaded(File newRunDir, Object gameInstance) {
        InformedEntrypointHandler.runThreadingBypassHandler(newRunDir, gameInstance);
    }
}
