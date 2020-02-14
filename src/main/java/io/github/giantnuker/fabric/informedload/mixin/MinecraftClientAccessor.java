package io.github.giantnuker.fabric.informedload.mixin;

import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.resource.ClientBuiltinResourcePackProvider;
import net.minecraft.client.resource.ClientResourcePackProfile;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.WindowProvider;
import net.minecraft.resource.ResourcePackManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(MinecraftClient.class)
public interface MinecraftClientAccessor {
    @Invoker("startTimerHackThread")
    void runStartTimerHackThread();

    @Accessor("LOGGER")
    static Logger getLOGGER() {
        return null;
    }

    @Accessor("windowProvider")
    WindowProvider getWindowProvider();

    @Accessor("window")
    void setWindow(Window window);

    @Invoker("getWindowTitle")
    String runGetWindowTitle();

    @Invoker("onWindowFocusChanged")
    void runOnWindowFocusChanged(boolean focused);

    @Invoker("getResourcePackDownloader")
    ClientBuiltinResourcePackProvider runGetResourcePackDownloader();

    @Accessor("mouse")
    Mouse getMouse();

    @Accessor("keyboard")
    Keyboard getKeyboard();

    @Accessor("framebuffer")
    Framebuffer getFramebuffer();

    @Accessor("resourcePackManager")
    ResourcePackManager<ClientResourcePackProfile> getResourcePackManager();

    @Accessor("thread")
    Thread getThread();

}
