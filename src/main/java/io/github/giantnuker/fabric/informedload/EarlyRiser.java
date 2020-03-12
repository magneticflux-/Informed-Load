package io.github.giantnuker.fabric.informedload;

import org.spongepowered.asm.mixin.Mixins;

/**
 * Created by Mitchell Skaggs on 3/12/2020.
 */

public class EarlyRiser implements Runnable {
    @Override
    public void run() {
        Mixins.addConfiguration("informedload.optifine.mixins.json");
    }
}
