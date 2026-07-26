package dev.ryanhcode.sable.compatibility;

import dev.ryanhcode.sable.compatibility.scalablelux.ScalableLuxCompat;
import dev.ryanhcode.sable.platform.SableLoaderPlatform;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.lighting.LevelLightEngine;

/**
 * Keeps Sable's plot-lighting code independent of the implementation backing
 * {@link LevelLightEngine}.
 */
public final class LightEngineCompat {
    private static final boolean SCALABLE_LUX_LOADED =
            SableLoaderPlatform.INSTANCE.isModLoaded("scalablelux");

    private LightEngineCompat() {
    }

    public static boolean usesScalableLux() {
        return SCALABLE_LUX_LOADED;
    }

    public static boolean hasBlockLight(final LevelLightEngine engine) {
        if (SCALABLE_LUX_LOADED) {
            return ScalableLuxCompat.hasBlockLight(engine);
        }
        return engine.blockEngine != null;
    }

    public static boolean hasSkyLight(final LevelLightEngine engine) {
        if (SCALABLE_LUX_LOADED) {
            return ScalableLuxCompat.hasSkyLight(engine);
        }
        return engine.skyEngine != null;
    }

    public static void lightChunk(final LevelLightEngine engine, final LevelChunk chunk) {
        if (!SCALABLE_LUX_LOADED) {
            throw new IllegalStateException("ScalableLux chunk lighting requested without ScalableLux");
        }
        ScalableLuxCompat.lightChunk(engine, chunk);
    }

    public static void removeChunk(final LevelLightEngine engine, final ChunkPos pos) {
        if (!SCALABLE_LUX_LOADED) {
            throw new IllegalStateException("ScalableLux chunk removal requested without ScalableLux");
        }
        ScalableLuxCompat.removeChunk(engine, pos);
    }
}
