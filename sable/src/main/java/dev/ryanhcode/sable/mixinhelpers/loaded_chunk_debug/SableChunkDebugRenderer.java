package dev.ryanhcode.sable.mixinhelpers.loaded_chunk_debug;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.ryanhcode.sable.mixinterface.loaded_chunk_debug.DebugChunkProviderAttachments;
import dev.ryanhcode.sable.mixinterface.loaded_chunk_debug.DebugLevelChunkExtension;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.ApiStatus;

// PORT-NOTE(mc26.1): the VertexConsumer/RenderType.debugLineStrip path is gone — debug renderers emit
// world-space lines through the Gizmos API now (see vanilla ChunkBorderRenderer.emitGizmos). The line
// strips with zero-alpha "jump" vertices became discrete line segments; geometry is unchanged.
@ApiStatus.Internal
public class SableChunkDebugRenderer {

    /**
     * Legacy entry point kept for the ChunkBorderRenderer mixin; the pose stack, buffer source and
     * camera offsets are unused because gizmos are emitted in world space.
     */
    public static void render(final PoseStack poseStack, final MultiBufferSource bufferSource, final double camX, final double camY, final double camZ) {
        emitGizmos();
    }

    /**
     * Emits loaded-chunk debug gizmos. Must be called while a gizmo collector is active
     * (i.e. from within the debug-renderer gizmo pass).
     */
    public static void emitGizmos() {
        final long time = System.currentTimeMillis();

        final Minecraft minecraft = Minecraft.getInstance();
        final Entity entity = minecraft.gameRenderer.getMainCamera().getEntity();

        final ClientLevel level = minecraft.level;
        final int minBuildHeight = level.getMinY();
        final int maxBuildHeight = (level.getMaxY() + 1);

        final double camY = entity != null ? entity.getY() : 0.0;

        final DebugChunkProviderAttachments attachments = (DebugChunkProviderAttachments) level.getChunkSource();
        for (final LevelChunk chunk : attachments.sable$loadedChunks()) {
            final ChunkPos pos = chunk.getPos();
            final float diff = Mth.clamp(time - ((DebugLevelChunkExtension) chunk).sable$getLastUpdate(), 0, 1000) / 1000.0F;
            final float red = 1.0F - diff;
            final int color = ARGB.colorFromFloat(1.0F, red, diff, 0.0F);

            final double x = pos.getMinBlockX();
            final double z = pos.getMinBlockZ();
            double y = minBuildHeight;
            if (camY > minBuildHeight) {
                y += (10 * ((1 - diff) / 100));
            } else {
                y -= (10 * ((1 - diff) / 100));
            }
            double y1 = maxBuildHeight;
            if (camY < maxBuildHeight) {
                y1 -= (10 * ((1 - diff) / 100));
            } else {
                y1 += (10 * ((1 - diff) / 100));
            }

            sable$square(x, y, z, color);
            sable$square(x, y1, z, color);
        }

        if (entity == null) {
            return;
        }

        final ChunkPos ckPos = entity.chunkPosition();
        final double x = ckPos.x * 16;
        final double y = minBuildHeight;
        final double y1 = maxBuildHeight;
        final double z = ckPos.z * 16;

        final int yellow = ARGB.colorFromFloat(1.0F, 1.0F, 1.0F, 0.0F);
        for (int xO = 0; xO < 2; xO++) {
            for (int zO = 0; zO < 2; zO++) {
                Gizmos.line(new Vec3(x + xO * 16, y, z + zO * 16), new Vec3(x + xO * 16, y1, z + zO * 16), yellow);
            }
        }

        final int blue = ARGB.colorFromFloat(1.0F, 0.0F, 0.0F, 1.0F);
        final int yStart = (((int) minBuildHeight) / 16) * 16;
        for (int yO = yStart; yO <= y1 + 1; yO += 16) {
            sable$square(x, yO, z, blue);
        }
    }

    private static void sable$square(final double x, final double y, final double z, final int color) {
        Gizmos.line(new Vec3(x, y, z), new Vec3(x + 16, y, z), color);
        Gizmos.line(new Vec3(x + 16, y, z), new Vec3(x + 16, y, z + 16), color);
        Gizmos.line(new Vec3(x + 16, y, z + 16), new Vec3(x, y, z + 16), color);
        Gizmos.line(new Vec3(x, y, z + 16), new Vec3(x, y, z), color);
    }
}
