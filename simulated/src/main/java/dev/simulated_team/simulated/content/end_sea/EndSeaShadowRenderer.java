package dev.simulated_team.simulated.content.end_sea;

import dev.simulated_team.simulated.content.blocks.void_anchor.VoidAnchorBlockEntity;
import foundry.veil.api.client.render.MatrixStack;
import foundry.veil.api.event.VeilRenderLevelStageEvent;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import org.joml.Matrix4fc;
import org.joml.Vector3d;
import org.joml.Vector3dc;

/** Compatibility no-op for the unavailable end-sea shadow pipeline. */
public final class EndSeaShadowRenderer {
    public static final float SHADOW_VOLUME_RADIUS = 128f;
    private static final Vector3dc ORIGIN = new Vector3d();

    private EndSeaShadowRenderer() {
    }

    public static boolean isEnabled() {
        return false;
    }

    public static boolean renderingShadowMap() {
        return false;
    }

    public static Vector3dc getLastRenderOrigin() {
        return ORIGIN;
    }

    public static Object getShadowsFramebuffer() {
        return null;
    }

    public static void addVoidAnchor(VoidAnchorBlockEntity anchor) {
    }

    public static void renderShadowMap(VeilRenderLevelStageEvent.Stage stage, LevelRenderer levelRenderer,
                                       MultiBufferSource.BufferSource bufferSource, MatrixStack poseStack,
                                       Matrix4fc frustumMatrix, Matrix4fc projectionMatrix, int renderTick,
                                       DeltaTracker deltaTracker, Camera camera, Frustum frustum) {
    }
}
