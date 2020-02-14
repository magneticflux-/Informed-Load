package io.github.giantnuker.fabric.informedload;

import com.google.gson.JsonParser;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.giantnuker.fabric.informedload.mixin.MinecraftClientAccessor;
import io.github.giantnuker.fabric.informedload.mixin.MixinMinecraftClient;
import io.github.giantnuker.fabric.loadcatcher.EntrypointHandler;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.RunArgs;
import net.minecraft.client.WindowSettings;
import net.minecraft.client.font.FontManager;
import net.minecraft.client.font.FontStorage;
import net.minecraft.client.font.FontType;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.options.GameOptions;
import net.minecraft.client.resource.language.LanguageManager;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.util.WindowProvider;
import net.minecraft.resource.ReloadableResourceManagerImpl;
import net.minecraft.resource.ResourcePack;
import net.minecraft.resource.ResourcePackProfile;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static net.minecraft.client.MinecraftClient.IS_SYSTEM_MAC;

public class InformedEntrypointHandler implements EntrypointHandler {
    public static RunArgs args;
    @Override
    public void beforeModsLoaded() {
        /*if (InformedLoadUtils.config.entrypointDisplay) {
            InformedLoadUtils.isDoingEarlyLoad = true;
            RenderSystem.setupDefaultState(0, 0, this.window.getFramebufferWidth(), this.window.getFramebufferHeight());

            ReloadableResourceManagerImpl resourceManager = (ReloadableResourceManagerImpl) this.resourceManager;
            resourcePackManager.scanPacks();
            List<ResourcePack> list = resourcePackManager.getEnabledProfiles().stream().map(ResourcePackProfile::createResourcePack).collect(Collectors.toList());
            for (ResourcePack resourcePack_1 : list) {
                resourceManager.addPack(resourcePack_1);
            }

            LanguageManager languageManager = new LanguageManager(options.language);
            resourceManager.registerListener(languageManager);
            languageManager.reloadResources(list);
            InformedLoadUtils.textureManager = new TextureManager(resourceManager);

            int i = this.window.calculateScaleFactor(options.guiScale, this.forcesUnicodeFont());
            this.window.setScaleFactor((double)i);

            Framebuffer framebuffer = this.getFramebuffer();
            framebuffer.resize(this.window.getFramebufferWidth(), this.window.getFramebufferHeight(), IS_SYSTEM_MAC);

            FontManager fontManager = new FontManager(InformedLoadUtils.textureManager, forcesUnicodeFont());
            resourceManager.registerListener(fontManager.getResourceReloadListener());

            final FontStorage fontStorage_1 = new FontStorage(InformedLoadUtils.textureManager, new Identifier("loading"));
            fontStorage_1.setFonts(Collections.singletonList(FontType.BITMAP.createLoader(new JsonParser().parse(InformedLoadUtils.FONT_JSON).getAsJsonObject()).load(resourceManager)));
            InformedLoadUtils.textRenderer = new TextRenderer(InformedLoadUtils.textureManager, fontStorage_1);

            Modloader.getInstance(runDirectory).loadMods(InformedLoadUtils.textureManager, window);
        }*/
        try {
            MinecraftClientAccessor mc = ((MinecraftClientAccessor) (Object) MinecraftClient.getInstance());
            Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            mc.runStartTimerHackThread();
            MinecraftClientAccessor.getLOGGER().info("Backend library: {}", RenderSystem.getBackendDescription());
            WindowSettings windowSettings2;
            System.out.println(args);
            GameOptions options = new GameOptions(MinecraftClient.getInstance(), args.directories.runDir);
            if (options.overrideHeight > 0 && options.overrideWidth > 0) {
                windowSettings2 = new WindowSettings(options.overrideWidth, options.overrideHeight, args.windowSettings.fullscreenWidth, args.windowSettings.fullscreenHeight, args.windowSettings.fullscreen);
            } else {
                windowSettings2 = args.windowSettings;
            }

            Util.nanoTimeSupplier = RenderSystem.initBackendSystem();
            Field windowProviderField = MinecraftClient.class.getDeclaredField("windowProvider");
            modifiersField.setInt(windowProviderField, windowProviderField.getModifiers() & ~Modifier.FINAL);
            windowProviderField.setAccessible(true);
            windowProviderField.set(MinecraftClient.getInstance(), new WindowProvider(MinecraftClient.getInstance()));
            mc.setWindow(mc.getWindowProvider().createWindow(windowSettings2, options.fullscreenResolution, mc.runGetWindowTitle()));
            mc.runOnWindowFocusChanged(true);

            try {
                InputStream inputStream = mc.runGetResourcePackDownloader().getPack().open(ResourceType.CLIENT_RESOURCES, new Identifier("icons/icon_16x16.png"));
                InputStream inputStream2 = mc.runGetResourcePackDownloader().getPack().open(ResourceType.CLIENT_RESOURCES, new Identifier("icons/icon_32x32.png"));
                MinecraftClient.getInstance().getWindow().setIcon(inputStream, inputStream2);
            } catch (IOException var8) {
                MinecraftClientAccessor.getLOGGER().error("Couldn't set icon", var8);
            }

            MinecraftClient.getInstance().getWindow().setFramerateLimit(options.maxFps);
            //this.mouse = new Mouse(this);
            //this.mouse.setup(this.window.getHandle());
            //this.keyboard = new Keyboard(this);
            //this.keyboard.setup(this.window.getHandle());
            RenderSystem.initRenderer(options.glDebugVerbosity, false);
            Field framebufferField = MinecraftClient.class.getDeclaredField("framebuffer");
            modifiersField.setInt(framebufferField, framebufferField.getModifiers() & ~Modifier.FINAL);
            framebufferField.setAccessible(true);
            framebufferField.set(MinecraftClient.getInstance(), new Framebuffer(MinecraftClient.getInstance().getWindow().getFramebufferWidth(), MinecraftClient.getInstance().getWindow().getFramebufferHeight(), true, IS_SYSTEM_MAC));
            mc.getFramebuffer().setClearColor(0.0F, 0.0F, 0.0F, 0.0F);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }
}
