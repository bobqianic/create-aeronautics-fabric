package dev.simulated_team.simulated.util;

import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.api.SubLevelHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkSource;

public class SimLevelUtil {
    public static boolean isAreaActuallyLoaded(final Level level, final BlockPos center, final int range) {
        if (Sable.HELPER.getContaining(level, center) != null) return true;

        // TODO: This should be common
        if (!level.hasChunkAt(center)) {
            return false;
        } else {
            if (level.isClientSide()) {
                final int minY = center.getY() - range;
                final int maxY = center.getY() + range;
                if (maxY < level.getMinY() || minY >= level.getMinY() + level.getHeight()) {
                    return false;
                }

                final int minX = center.getX() - range;
                final int minZ = center.getZ() - range;
                final int maxX = center.getX() + range;
                final int maxZ = center.getZ() + range;
                final int minChunkX = SectionPos.blockToSectionCoord(minX);
                final int maxChunkX = SectionPos.blockToSectionCoord(maxX);
                final int minChunkZ = SectionPos.blockToSectionCoord(minZ);
                final int maxChunkZ = SectionPos.blockToSectionCoord(maxZ);
                final ChunkSource chunkSource = level.getChunkSource();

                for(int chunkX = minChunkX; chunkX <= maxChunkX; ++chunkX) {
                    for(int chunkZ = minChunkZ; chunkZ <= maxChunkZ; ++chunkZ) {
                        if (!chunkSource.hasChunk(chunkX, chunkZ)) {
                            return false;
                        }
                    }
                }
            }

            return true;
        }
    }

}
