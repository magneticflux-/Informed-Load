package io.github.giantnuker.fabric.informedload;

import com.google.gson.JsonParser;
import com.mojang.blaze3d.platform.GLX;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.giantnuker.fabric.informedload.api.ProgressBar;
import io.github.giantnuker.fabric.informedload.mixin.MinecraftClientAccessor;
import io.github.giantnuker.fabric.loadcatcher.EntrypointCatcher;
import io.github.giantnuker.fabric.loadcatcher.EntrypointHandler;
import io.github.giantnuker.fabric.loadcatcher.EntrypointKind;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.RunArgs;
import net.minecraft.client.WindowSettings;
import net.minecraft.client.font.FontManager;
import net.minecraft.client.font.FontStorage;
import net.minecraft.client.font.FontType;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.options.GameOptions;
import net.minecraft.client.resource.language.LanguageManager;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.WindowProvider;
import net.minecraft.resource.ReloadableResourceManagerImpl;
import net.minecraft.resource.ResourcePack;
import net.minecraft.resource.ResourcePackProfile;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static net.minecraft.client.MinecraftClient.IS_SYSTEM_MAC;
import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.glfw.GLFW.glfwSwapBuffers;

public class InformedEntrypointHandler implements EntrypointHandler {
    public static boolean STARTED = false;
    private static volatile InformedEntrypointHandler INSTANCE;
    public static RunArgs args;
    public static void runThreadingBypassHandler(File newRunDir, Object gameInstance) {
        try {
            // Window creation and init
            MinecraftClientAccessor mc = ((MinecraftClientAccessor) MinecraftClient.getInstance());
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
            Field windowProviderField = MinecraftClient.class.getDeclaredField(FabricLoader.INSTANCE.getMappingResolver().mapFieldName("intermediary", "net.minecraft.class_310", "field_1686", "Lnet/minecraft/class_3682;"));
            //                                                                                                                                        net.minecraft.client.MinecraftClient windowProvider net.minecraft.client.util.WindowProvider
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
            //this.mouse.setup(MinecraftClient.getInstance().getWindow().getHandle());
            //this.keyboard = new Keyboard(this);
            //this.keyboard.setup(MinecraftClient.getInstance().getWindow().getHandle());
            RenderSystem.initRenderer(options.glDebugVerbosity, false);
            Field framebufferField = MinecraftClient.class.getDeclaredField(FabricLoader.INSTANCE.getMappingResolver().mapFieldName("intermediary", "net.minecraft.class_310", "field_1689", "Lnet/minecraft/class_276;"));
            //                                                                                                                                      net.minecraft.client.MinecraftClient framebuffer net.minecraft.client.gl.Framebuffer
            modifiersField.setInt(framebufferField, framebufferField.getModifiers() & ~Modifier.FINAL);
            framebufferField.setAccessible(true);
            framebufferField.set(MinecraftClient.getInstance(), new Framebuffer(MinecraftClient.getInstance().getWindow().getFramebufferWidth(), MinecraftClient.getInstance().getWindow().getFramebufferHeight(), true, IS_SYSTEM_MAC));
            mc.getFramebuffer().setClearColor(0.0F, 0.0F, 0.0F, 0.0F);

            InformedLoadUtils.isDoingEarlyLoad = true;
            RenderSystem.setupDefaultState(0, 0, MinecraftClient.getInstance().getWindow().getFramebufferWidth(), MinecraftClient.getInstance().getWindow().getFramebufferHeight());


            // Setup Informed Load hooks
            ReloadableResourceManagerImpl resourceManager = new ReloadableResourceManagerImpl(ResourceType.CLIENT_RESOURCES, mc.getThread());
            options.addResourcePackProfilesToManager(mc.getResourcePackManager());
            mc.getResourcePackManager().scanPacks();

            List<ResourcePack> list = mc.getResourcePackManager().getEnabledProfiles().stream().map(ResourcePackProfile::createResourcePack).collect(Collectors.toList());
            for (ResourcePack resourcePack_1 : list) {
                resourceManager.addPack(resourcePack_1);
            }

            LanguageManager languageManager = new LanguageManager(options.language);
            resourceManager.registerListener(languageManager);
            languageManager.reloadResources(list);
            InformedLoadUtils.textureManager = new TextureManager(resourceManager);

            int i = MinecraftClient.getInstance().getWindow().calculateScaleFactor(options.guiScale, options.forceUnicodeFont);
            MinecraftClient.getInstance().getWindow().setScaleFactor(i);

            Framebuffer framebuffer = mc.getFramebuffer();
            framebuffer.resize(MinecraftClient.getInstance().getWindow().getFramebufferWidth(), MinecraftClient.getInstance().getWindow().getFramebufferHeight(), IS_SYSTEM_MAC);

            FontManager fontManager = new FontManager(InformedLoadUtils.textureManager, options.forceUnicodeFont);
            resourceManager.registerListener(fontManager.getResourceReloadListener());

            final FontStorage fontStorage_1 = new FontStorage(InformedLoadUtils.textureManager, new Identifier("loading"));
            fontStorage_1.setFonts(Collections.singletonList(FontType.BITMAP.createLoader(new JsonParser().parse(InformedLoadUtils.FONT_JSON).getAsJsonObject()).load(resourceManager)));
            InformedLoadUtils.textRenderer = new TextRenderer(InformedLoadUtils.textureManager, fontStorage_1);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
        STARTED = true;
        Thread loaderThread = new Thread(() -> EntrypointCatcher.runEntrypointRedirection(newRunDir, gameInstance)); // Doing this allows someone else to redirect it still
        loaderThread.start();
        while (INSTANCE == null) {}
        while (loaderThread.isAlive()) {
            INSTANCE.render();
        }
    }

    List<ProgressBar> progressBars = new ArrayList<>();
    String subText1 = "", subText2 = "";
    boolean keepRendering = true;
    private static final Identifier LOGO = new Identifier("textures/gui/title/mojang.png");

    private void render() {
        GlStateManager.pushMatrix();
        GlStateManager.clearColor(1.0f, 1.0f, 1.0f, 1.0f);
        GlStateManager.clear(16640, MinecraftClient.IS_SYSTEM_MAC);
        GlStateManager.loadIdentity();
        GlStateManager.ortho(0.0D, MinecraftClient.getInstance().getWindow().getScaledWidth(), MinecraftClient.getInstance().getWindow().getScaledHeight(), 0.0D, -1000.0D, 1000.0D);
        RenderSystem.matrixMode(5888);
        //GlStateManager.enableBlend();
        InformedLoadUtils.textureManager.bindTexture(LOGO);
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0f);
        DrawableHelper.blit((MinecraftClient.getInstance().getWindow().getScaledWidth() - 256) / 2, (MinecraftClient.getInstance().getWindow().getScaledHeight() - 256) / 2 - 40, 0, 0, 0, 256, 256, 256, 256);
        for (int i = 0; i < progressBars.size(); i++) {
            ProgressBar progressBar = progressBars.get(i);
            progressBar.render(MinecraftClient.getInstance().getWindow());
        }
        renderSubText(subText2, 0);
        renderSubText(subText1, 1);
        GlStateManager.popMatrix();
        glfwSwapBuffers(MinecraftClient.getInstance().getWindow().getHandle());
        try {
            glfwPollEvents();
        } catch (NullPointerException ignoreme) {
            // ok boomer
        }
        if (GLX._shouldClose(MinecraftClient.getInstance().getWindow())) {
            MinecraftClient.getInstance().stop();
        }
    }

    private void renderSubText(String text, int row) {
        InformedLoadUtils.textRenderer.draw(text, MinecraftClient.getInstance().getWindow().getScaledWidth() / 2f - InformedLoadUtils.textRenderer.getStringWidth(text) / 2f, MinecraftClient.getInstance().getWindow().getScaledHeight() - (row + 1) * 20, 0x666666);
    }

    public ProgressBar createProgressBar(int row, ProgressBar.SplitType splitType) {
        return new ProgressBar.SplitProgressBar(splitType) {
            @Override
            protected int getY(Window window) {
                return row * 20 + window.getScaledHeight() / 4 * 3 - 40;
            }
        };
    }

    Map<String, ModContainer> mainToContainer;
    Map<String, ModContainer> clientToContainer;

    AtomicInteger index;
    AtomicInteger total;

    ProgressBar overall;
    ProgressBar commonEntrypoints;
    ProgressBar clientEntrypoints;

    int totalClientEntrypoints;

    @Override
    public void beforeModsLoaded() {
        INSTANCE = this;
        progressBars.clear();
        overall = createProgressBar(0, ProgressBar.SplitType.NONE);
        progressBars.add(overall);
        overall.setText("Locating Entrypoints");
        InformedLoadUtils.LOGGER.info("Locating Entrypoints");
        mainToContainer = new HashMap<>();
        clientToContainer = new HashMap<>();

        InformedLoadUtils.LOGGER.info("Loading Mods");
        int totalMainEntrypoints = FabricLoader.INSTANCE.getEntrypoints("main", ModInitializer.class).size();
        totalClientEntrypoints = FabricLoader.INSTANCE.getEntrypoints("client", ClientModInitializer.class).size();
        commonEntrypoints = createProgressBar(1, ProgressBar.SplitType.LEFT);
        commonEntrypoints.setText(totalMainEntrypoints + " Common");
        clientEntrypoints = createProgressBar(1, ProgressBar.SplitType.RIGHT);
        clientEntrypoints.setText(totalClientEntrypoints + " Client");

        index = new AtomicInteger();
        total = new AtomicInteger(totalMainEntrypoints);

        progressBars.add(commonEntrypoints);
        progressBars.add(clientEntrypoints);
    }
    @Override
    public void beforeModInitEntrypoint(String id, ModContainer mod, EntrypointKind entrypointKind) {
        overall.setText("Running Entrypoints - " + (entrypointKind == EntrypointKind.CLIENT ? "Client" : "Common"));
        if (entrypointKind == EntrypointKind.CLIENT && totalClientEntrypoints != -1) {
            commonEntrypoints.setText("Common Complete");
            total.set(totalClientEntrypoints);
            index.set(0);
            totalClientEntrypoints = -1;
        }
        index.set(index.get() + 1);
        subText1 = "";
        subText2 = "";
        ModMetadata metadata = mod != null ? mod.getMetadata() : null;
        if (metadata != null) {
            subText1 = metadata.getName() + " (" + metadata.getId() + ")";
        } else {
            subText1 = "UNKNOWN MOD";
        }
        subText2 = id;

        InformedLoadUtils.logDebug(metadata == null ? String.format("Loading [UNKNOWN MOD]: %s (%s)", id, entrypointKind == EntrypointKind.CLIENT ? "Client" : "Main") : String.format("Loading %s(%s): %s (%s)", metadata.getName(), metadata.getId(), id, entrypointKind == EntrypointKind.CLIENT ? "Client" : "Main"));
    }

    @Override
    public void afterModInitEntrypoint(String id, ModContainer mod, EntrypointKind entrypointKind) {
        switch (entrypointKind) {
            case CLIENT:
                clientEntrypoints.setText(index.get() + "/" + total.get() + " Client");
                clientEntrypoints.setProgress((float) (index.get()) / total.get());
                overall.setProgress((0.5f + (((float) (index.get()) / total.get()) / 2f)) / 2f);
                break;
            case COMMON:
                commonEntrypoints.setText(index.get() + "/" + total.get() + " Common");
                commonEntrypoints.setProgress((float) (index.get()) / total.get());
                overall.setProgress((((float) (index.get()) / total.get()) / 2f) / 2f);
                break;
        }
    }
}
