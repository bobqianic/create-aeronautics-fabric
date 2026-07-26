package dev.ryanhcode.sable;

import dev.ryanhcode.sable.sublevel.render.SubLevelRenderer;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.jetbrains.annotations.ApiStatus;

import java.util.Arrays;

public final class SableClientConfig {

    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.BooleanValue ATTEMPT_UDP_NETWORKING;
    public static final ModConfigSpec.BooleanValue SUB_LEVEL_DYNAMIC_SHADING;
    public static final ModConfigSpec.BooleanValue SUB_LEVEL_WATER_OCCLUSION;
    public static final ModConfigSpec.BooleanValue SUB_LEVEL_SKYLIGHT_SHADOWS;
    public static final ModConfigSpec.BooleanValue DEBUG_DRAW_LOADED_CHUNKS;
    public static final ModConfigSpec.DoubleValue INTERPOLATION_DELAY;
    public static final ModConfigSpec.EnumValue<SubLevelRenderer.SelectedRenderer> SELECTED_RENDERER;
    public static final ModConfigSpec.DoubleValue ZOOM_SENSITIVITY;

    static {
        final ModConfigSpec.Builder builder = new ModConfigSpec.Builder();


        SUB_LEVEL_DYNAMIC_SHADING = builder
                .comment("Whether sub-levels should apply block shading dynamically")
                .define("sub_level_dynamic_shading", true);
        SUB_LEVEL_WATER_OCCLUSION = builder
                .comment("Whether sub-levels can occlude the water surface")
                .define("sub_level_water_occlusion", true);
        SUB_LEVEL_SKYLIGHT_SHADOWS = builder
                .comment("Whether sub-levels should cast a shadow on the world")
                .define("sub_level_skylight_shadows", false);
        DEBUG_DRAW_LOADED_CHUNKS = builder
                .comment("Whether to draw loaded chunks on the client in the chunk debug renderer")
                .define("debug_draw_loaded_chunks", false);
        INTERPOLATION_DELAY = builder
                .comment("The distance back in game-ticks that the snapshot interpolation should operate")
                .defineInRange("sub_level_snapshot_interpolation_delay_ticks", 1.5, 0.0, 100.0);
        SELECTED_RENDERER = builder
                .comment("The renderer to use for sub-levels")
                .defineEnum("sub_level_renderer", SubLevelRenderer.DEFAULT, Arrays.stream(SubLevelRenderer.SelectedRenderer.values())
                        .filter(SubLevelRenderer.SelectedRenderer::isSupported)
                        .toArray(SubLevelRenderer.SelectedRenderer[]::new));
        ZOOM_SENSITIVITY = builder
                .comment("The zoom sensitivity for sub-level camera types")
                .defineInRange("sub_level_zoom_sensitivity", 0.2, 0.0, 100.0);
        ATTEMPT_UDP_NETWORKING = builder
                .comment("If Sable should attempt to establish a UDP connection with the server, to receive sub-level movement data over a UDP channel")
                .define("attempt_udp_networking", true);

        SPEC = builder.build();
    }

    @ApiStatus.Internal
    public static void onUpdate(final boolean notify) {
        // mc26.1 port branch: the dynamic-shading / sky-light-shadow /
        // water-occlusion toggles no longer drive anything (Veil shader
        // features stripped); the options are kept so existing config files
        // load unchanged. Only the renderer selection remains live.
        final Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null) {
            // mc26.1: config Loading fires before the client instance exists;
            // the default renderer selection applies lazily on first use.
            return;
        }
        minecraft.execute(() -> SubLevelRenderer.setImpl(SableClientConfig.SELECTED_RENDERER.get()));
    }
}
