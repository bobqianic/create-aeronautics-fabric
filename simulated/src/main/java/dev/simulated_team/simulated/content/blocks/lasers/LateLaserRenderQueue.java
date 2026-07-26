package dev.simulated_team.simulated.content.blocks.lasers;

import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.simulated_team.simulated.index.SimRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

/**
 * Defers native laser quads until Fabric's post-cloud world render pass.
 * This lets the complete scene exist before the laser is blended over it.
 */
public final class LateLaserRenderQueue {
    private static final List<QueuedLaser> QUEUED_LASERS = new ArrayList<>();
    private static boolean collectingWorldFrame;

    private LateLaserRenderQueue() {
    }

    public static void beginWorldFrame() {
        QUEUED_LASERS.clear();
        collectingWorldFrame = true;
    }

    public static boolean isCollectingWorldFrame() {
        return collectingWorldFrame;
    }

    public static boolean enqueue(final Matrix4f matrix,
                                  final float beamEnd, final float offset, final float endU,
                                  final float red, final float green, final float blue,
                                  final float alpha, final float endAlpha) {
        if (!collectingWorldFrame) {
            return false;
        }

        QUEUED_LASERS.add(new QueuedLaser(new Matrix4f(matrix), beamEnd, offset, endU,
                red, green, blue, alpha, endAlpha));
        return true;
    }

    public static void finishWorldFrameCollection() {
        collectingWorldFrame = false;
    }

    public static void drawAfterClouds(final MultiBufferSource.BufferSource buffer) {
        try {
            if (QUEUED_LASERS.isEmpty()) {
                return;
            }

            // Opacity multiplication and color addition are each commutative.
            // Keeping them in separate passes removes camera-order color shifts
            // without giving up the original beam's translucent overcast.
            final VertexConsumer attenuationBuilder =
                    buffer.getBuffer(SimRenderTypes.lateLaserAttenuation());
            for (final QueuedLaser laser : QUEUED_LASERS) {
                AbstractLaserRenderer.addQueuedExactLaser(attenuationBuilder, laser.matrix,
                        laser.beamEnd, laser.offset, laser.endU,
                        laser.red, laser.green, laser.blue, laser.alpha, laser.endAlpha);
            }
            buffer.endBatch(SimRenderTypes.lateLaserAttenuation());

            final VertexConsumer builder = buffer.getBuffer(SimRenderTypes.lateLaser());
            for (final QueuedLaser laser : QUEUED_LASERS) {
                AbstractLaserRenderer.addQueuedExactLaser(builder, laser.matrix,
                        laser.beamEnd, laser.offset, laser.endU,
                        laser.red, laser.green, laser.blue, laser.alpha, laser.endAlpha);
            }
            buffer.endBatch(SimRenderTypes.lateLaser());
        } finally {
            QUEUED_LASERS.clear();
            collectingWorldFrame = false;
        }
    }

    private record QueuedLaser(Matrix4f matrix,
                               float beamEnd, float offset, float endU,
                               float red, float green, float blue,
                               float alpha, float endAlpha) {
    }
}
