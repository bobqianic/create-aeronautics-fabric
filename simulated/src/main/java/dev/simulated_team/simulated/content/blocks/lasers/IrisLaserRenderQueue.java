package dev.simulated_team.simulated.content.blocks.lasers;

import com.mojang.blaze3d.ProjectionType;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.simulated_team.simulated.content.physics_staff.OptionalShaderMods;
import dev.simulated_team.simulated.index.SimRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Keeps the Iris laser completely separate from shader-off world rendering.
 * The exact native laser geometry is collected during the world frame, then
 * drawn after Iris has finalized and composited its shader-pack targets.
 */
public final class IrisLaserRenderQueue {
    // Outer-to-inner shells. The barely visible outer edge hides the geometric
    // boundary while the inner shell supplies a restrained bloom around the core.
    private static final float[] GLOW_SCALES = {2.2f, 1.65f, 1.25f};
    private static final float[] GLOW_STRENGTHS = {0.004f, 0.012f, 0.03f};

    private static final List<QueuedLaser> QUEUED_LASERS = new ArrayList<>();
    private static boolean collectingWorldFrame;
    private static GpuBufferSlice worldProjection;
    private static ProjectionType worldProjectionType;
    private static GpuBufferSlice worldFog;

    private IrisLaserRenderQueue() {
    }

    public static void beginWorldFrame() {
        QUEUED_LASERS.clear();
        collectingWorldFrame = OptionalShaderMods.isShaderPackActive();
        worldProjection = null;
        worldProjectionType = null;
        worldFog = null;
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

        if (worldProjection == null) {
            worldProjection = RenderSystem.getProjectionMatrixBuffer();
            worldProjectionType = RenderSystem.getProjectionType();
            worldFog = RenderSystem.getShaderFog();
        }

        // The block-entity pose does not contain the global camera model-view.
        // Bake that matrix now because Iris replaces it with an identity/composite
        // transform before this queue is drawn.
        final Matrix4f worldMatrix = new Matrix4f(RenderSystem.getModelViewMatrix()).mul(matrix);
        QUEUED_LASERS.add(new QueuedLaser(worldMatrix, beamEnd, offset, endU,
                red, green, blue, alpha, endAlpha));
        return true;
    }

    public static void finishWorldFrameCollection() {
        collectingWorldFrame = false;
    }

    public static void drawAfterShaderComposite(final MultiBufferSource.BufferSource buffer) {
        final Matrix4fStack modelViewStack = RenderSystem.getModelViewStack();
        final GpuBufferSlice previousProjection = RenderSystem.getProjectionMatrixBuffer();
        final ProjectionType previousProjectionType = RenderSystem.getProjectionType();
        final GpuBufferSlice previousFog = RenderSystem.getShaderFog();
        boolean modelViewPushed = false;

        try {
            if (!OptionalShaderMods.isShaderPackActive() || QUEUED_LASERS.isEmpty()) {
                return;
            }

            modelViewStack.pushMatrix();
            modelViewPushed = true;
            modelViewStack.identity();
            if (worldProjection != null && worldProjectionType != null) {
                RenderSystem.setProjectionMatrix(worldProjection, worldProjectionType);
            }
            if (worldFog != null) {
                RenderSystem.setShaderFog(worldFog);
            }

            final VertexConsumer glowBuilder = buffer.getBuffer(SimRenderTypes.irisCompositeLaserGlow());
            for (final QueuedLaser laser : QUEUED_LASERS) {
                for (int layer = 0; layer < GLOW_SCALES.length; layer++) {
                    addGlowLayer(glowBuilder, laser, GLOW_SCALES[layer], GLOW_STRENGTHS[layer]);
                }
            }
            buffer.endBatch(SimRenderTypes.irisCompositeLaserGlow());

            final VertexConsumer attenuationBuilder =
                    buffer.getBuffer(SimRenderTypes.irisCompositeLaserAttenuation());
            for (final QueuedLaser laser : QUEUED_LASERS) {
                AbstractLaserRenderer.addQueuedExactLaser(attenuationBuilder, laser.matrix,
                        laser.beamEnd, laser.offset, laser.endU,
                        laser.red, laser.green, laser.blue, laser.alpha, laser.endAlpha);
            }
            buffer.endBatch(SimRenderTypes.irisCompositeLaserAttenuation());

            final VertexConsumer builder = buffer.getBuffer(SimRenderTypes.irisCompositeLaser());
            for (final QueuedLaser laser : QUEUED_LASERS) {
                AbstractLaserRenderer.addQueuedExactLaser(builder, laser.matrix,
                        laser.beamEnd, laser.offset, laser.endU,
                        laser.red, laser.green, laser.blue, laser.alpha, laser.endAlpha);
            }
            buffer.endBatch(SimRenderTypes.irisCompositeLaser());
        } finally {
            if (modelViewPushed) {
                modelViewStack.popMatrix();
            }
            if (previousProjection != null && previousProjectionType != null) {
                RenderSystem.setProjectionMatrix(previousProjection, previousProjectionType);
            }
            if (previousFog != null) {
                RenderSystem.setShaderFog(previousFog);
            }
            QUEUED_LASERS.clear();
            collectingWorldFrame = false;
            worldProjection = null;
            worldProjectionType = null;
            worldFog = null;
        }
    }

    private static void addGlowLayer(final VertexConsumer builder, final QueuedLaser laser,
                                     final float scale, final float strength) {
        final Matrix4f glowMatrix = new Matrix4f(laser.matrix)
                .translate(0.5f, 0.5f, 0.0f)
                .scale(scale, scale, 1.0f)
                .translate(-0.5f, -0.5f, 0.0f);
        AbstractLaserRenderer.addQueuedExactLaser(builder, glowMatrix,
                laser.beamEnd, laser.offset, laser.endU,
                laser.red, laser.green, laser.blue,
                laser.alpha * strength, laser.endAlpha * strength);
    }

    private record QueuedLaser(Matrix4f matrix,
                               float beamEnd, float offset, float endU,
                               float red, float green, float blue,
                               float alpha, float endAlpha) {
    }
}
