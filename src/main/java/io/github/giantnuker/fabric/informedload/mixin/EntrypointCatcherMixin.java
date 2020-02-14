package io.github.giantnuker.fabric.informedload.mixin;

import io.github.giantnuker.fabric.informedload.InformedEntrypointHandler;
import io.github.giantnuker.fabric.loadcatcher.EntrypointCatcher;
import io.github.giantnuker.fabric.loadcatcher.EntrypointRunnable;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.io.File;

@Mixin(EntrypointCatcher.class)
public class EntrypointCatcherMixin {
    @Shadow private static EntrypointRunnable modEntrypointReplacement;

    @Shadow @Final private static Logger LOGGER;

    @Shadow private static String replacingMod;

    /**
     * :tiny_potato:
     * @author GiantNuker
     */
    @Overwrite(remap = false)
    public static void runEntrypointRedirection(File newRunDir, Object gameInstance) {
        if (InformedEntrypointHandler.STARTED) {
            if (modEntrypointReplacement != null) {
                LOGGER.warn("Running Mod Entrypoint Redirector from " + replacingMod);
                modEntrypointReplacement.run(newRunDir, gameInstance);
            } else {
                LOGGER.info("Running Mod Entrypoints Normally");
                EntrypointCatcher.LoaderClientReplacement.run(newRunDir, gameInstance);
            }

            LOGGER.info("Mod Initialization complete");
        } else {
            InformedEntrypointHandler.runThreadingBypassHandler(newRunDir, gameInstance);
        }
    }
}
