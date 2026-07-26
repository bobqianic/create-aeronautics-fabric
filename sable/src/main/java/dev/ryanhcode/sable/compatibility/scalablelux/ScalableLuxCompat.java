package dev.ryanhcode.sable.compatibility.scalablelux;

import ca.spottedleaf.starlight.common.light.StarLightEngine;
import ca.spottedleaf.starlight.common.light.StarLightInterface;
import ca.spottedleaf.starlight.common.light.StarLightLightingProvider;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.lighting.LevelLightEngine;

/**
 * Direct bridge to ScalableLux 0.1.6's lighting implementation.
 *
 * <p>This class is only linked when Fabric Loader reports ScalableLux as
 * loaded. Keeping the optional types here allows the rest of Sable to load
 * normally without ScalableLux on the class path.</p>
 */
public final class ScalableLuxCompat {
    private ScalableLuxCompat() {
    }

    public static boolean hasBlockLight(final LevelLightEngine engine) {
        return getLightEngine(engine).hasBlockLight();
    }

    public static boolean hasSkyLight(final LevelLightEngine engine) {
        return getLightEngine(engine).hasSkyLight();
    }

    public static void lightChunk(final LevelLightEngine engine, final LevelChunk chunk) {
        final StarLightInterface scalableLux = getLightEngine(engine);
        chunk.setLightCorrect(false);
        scalableLux.lightChunk(chunk, StarLightEngine.getEmptySectionsForChunk(chunk));
        chunk.setLightCorrect(true);
        engine.setLightEnabled(chunk.getPos(), true);
    }

    public static void removeChunk(final LevelLightEngine engine, final ChunkPos pos) {
        getLightEngine(engine).removeChunkTasks(pos);
        engine.setLightEnabled(pos, false);
    }

    private static StarLightInterface getLightEngine(final LevelLightEngine engine) {
        if (!(engine instanceof final StarLightLightingProvider provider)) {
            throw new IllegalStateException(
                    "ScalableLux is loaded but LevelLightEngine has no StarLightLightingProvider"
            );
        }
        return provider.getLightEngine();
    }
}
