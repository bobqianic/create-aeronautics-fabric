package dev.eriksonn.aeronautics.content.blocks.hot_air.hot_air_burner;

import com.mojang.blaze3d.ProjectionType;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.eriksonn.aeronautics.index.client.AeroRenderTypes;
import dev.simulated_team.simulated.content.physics_staff.OptionalShaderMods;
import net.minecraft.client.renderer.MultiBufferSource;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Draws burner flames after Iris has composited the shader-pack frame.
 *
 * <p>The burner uses a custom procedural core shader. If it is drawn into an
 * active shader pack's world targets, packs such as Bliss can treat its opaque
 * output as translucent during their later composite. Retaining the exact
 * geometry and drawing it afterward keeps the custom shader isolated while
 * preserving depth testing against the world.</p>
 */
public final class IrisBurnerFlameRenderQueue {
    private static final List<QueuedFlame> QUEUED_FLAMES = new ArrayList<>();

    private static boolean collectingWorldFrame;
    private static GpuBufferSlice worldProjection;
    private static ProjectionType worldProjectionType;
    private static GpuBufferSlice worldFog;

    private IrisBurnerFlameRenderQueue() {
    }

    public static void beginWorldFrame() {
        QUEUED_FLAMES.clear();
        collectingWorldFrame = OptionalShaderMods.isShaderPackActive();
        worldProjection = null;
        worldProjectionType = null;
        worldFog = null;
    }

    public static boolean isCollectingWorldFrame() {
        return collectingWorldFrame;
    }

    public static boolean enqueue(final Matrix4f matrix, final float animationPhase,
                                  final float intensity, final float palette) {
        if (!collectingWorldFrame) {
            return false;
        }

        if (worldProjection == null) {
            worldProjection = RenderSystem.getProjectionMatrixBuffer();
            worldProjectionType = RenderSystem.getProjectionType();
            worldFog = RenderSystem.getShaderFog();
        }

        // Block-entity poses do not contain the camera model-view transform.
        // Bake it in because Iris replaces the global transform before this
        // queue is drawn.
        final Matrix4f worldMatrix =
                new Matrix4f(RenderSystem.getModelViewMatrix()).mul(matrix);
        QUEUED_FLAMES.add(new QueuedFlame(
                worldMatrix,
                animationPhase,
                intensity,
                palette
        ));
        return true;
    }

    public static void finishWorldFrameCollection() {
        collectingWorldFrame = false;
    }

    public static void drawAfterShaderComposite(
            final MultiBufferSource.BufferSource buffer
    ) {
        final Matrix4fStack modelViewStack = RenderSystem.getModelViewStack();
        final GpuBufferSlice previousProjection =
                RenderSystem.getProjectionMatrixBuffer();
        final ProjectionType previousProjectionType =
                RenderSystem.getProjectionType();
        final GpuBufferSlice previousFog = RenderSystem.getShaderFog();
        boolean modelViewPushed = false;

        try {
            if (!OptionalShaderMods.isShaderPackActive()
                    || QUEUED_FLAMES.isEmpty()) {
                return;
            }

            modelViewStack.pushMatrix();
            modelViewPushed = true;
            modelViewStack.identity();
            if (worldProjection != null && worldProjectionType != null) {
                RenderSystem.setProjectionMatrix(
                        worldProjection,
                        worldProjectionType
                );
            }
            if (worldFog != null) {
                RenderSystem.setShaderFog(worldFog);
            }

            final VertexConsumer consumer = buffer.getBuffer(
                    AeroRenderTypes.irisCompositeBurnerFlame()
            );
            for (final QueuedFlame flame : QUEUED_FLAMES) {
                HotAirBurnerRenderer.renderFlame(
                        flame.matrix,
                        consumer,
                        flame.animationPhase,
                        flame.intensity,
                        flame.palette
                );
            }
            buffer.endBatch(AeroRenderTypes.irisCompositeBurnerFlame());
        } finally {
            if (modelViewPushed) {
                modelViewStack.popMatrix();
            }
            if (previousProjection != null && previousProjectionType != null) {
                RenderSystem.setProjectionMatrix(
                        previousProjection,
                        previousProjectionType
                );
            }
            if (previousFog != null) {
                RenderSystem.setShaderFog(previousFog);
            }
            QUEUED_FLAMES.clear();
            collectingWorldFrame = false;
            worldProjection = null;
            worldProjectionType = null;
            worldFog = null;
        }
    }

    private record QueuedFlame(Matrix4f matrix, float animationPhase,
                               float intensity, float palette) {
    }
}
