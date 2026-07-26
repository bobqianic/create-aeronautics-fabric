package dev.simulated_team.simulated.content.blocks.lasers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import dev.simulated_team.simulated.compat.create.SmartBlockEntityRenderer;
import com.zurrtum.create.client.flywheel.lib.transform.TransformStack;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.api.SubLevelHelper;
import dev.ryanhcode.sable.util.SableDistUtil;
import dev.simulated_team.simulated.compat.create.RenderBridge;
import dev.simulated_team.simulated.content.physics_staff.OptionalShaderMods;
import dev.simulated_team.simulated.index.SimRenderTypes;
import com.zurrtum.create.catnip.data.Couple;
import com.zurrtum.create.client.catnip.render.SuperRenderTypeBuffer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector4f;

public abstract class AbstractLaserRenderer<T extends AbstractLaserBlockEntity> extends SmartBlockEntityRenderer<T> {
    public AbstractLaserRenderer(final BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(final T blockEntity, final float partialTicks, final PoseStack pose, final MultiBufferSource buffer, final int light, final int overlay) {
        super.renderSafe(blockEntity, partialTicks, pose, buffer, light, overlay);

        final LaserBehaviour laser = blockEntity.getAllBehaviours().stream().filter(behaviour -> behaviour instanceof LaserBehaviour).map(behaviour -> (LaserBehaviour) behaviour).findFirst().orElse(null);

        if (laser != null && laser.shouldCast()) {
            final Vector4f colors = this.getColors(blockEntity, partialTicks);

            if (colors.w > 0) { // alpha > 0
                pose.pushPose();

                this.transformPose(blockEntity, laser, pose);
                final float distance = this.getLaserLength(laser);
                this.createLaser(colors, pose, buffer, laser.getRange(), distance);

                pose.popPose();
            }
        }
    }

    public abstract Vector4f getColors(T blockEntity, float partialTicks);

    public float getLaserLength(final LaserBehaviour laser) {
        float laserRange = laser.getRange();

        final HitResult hr = this.getRenderedHitResult(laser);
        final Couple<Vec3> positions = laser.getLaserPositions().get();
        if (hr != null && !hr.getType().equals(HitResult.Type.MISS)) {
            Vec3 hitPos = hr.getLocation();
            if (laser.getVirtualHitPos() != Vec3.ZERO) {
                hitPos = laser.getVirtualHitPos();
            }

            laserRange = (float) Math.sqrt(Sable.HELPER.distanceSquaredWithSubLevels(SableDistUtil.getClientLevel(), positions.getFirst(), hitPos)) - 0.1f;
        } else if (laser.getVirtualHitPos() != Vec3.ZERO) {
            final Vec3 hitPos = laser.getVirtualHitPos();

            laserRange = (float) Math.sqrt(Sable.HELPER.distanceSquaredWithSubLevels(SableDistUtil.getClientLevel(), positions.getFirst(), hitPos)) - 0.1f;
        }

        return laserRange;
    }

    public abstract float getLaserScale(final LaserBehaviour laser);

    public HitResult getRenderedHitResult(final LaserBehaviour laser) {
        return laser.getClosestHitResult();
    }

    protected void transformPose(final T blockEntity, final LaserBehaviour laser, final PoseStack pose) {
        final Direction facing = blockEntity.getDirection();

        pose.translate(0.5, 0.5, 0.5);

        TransformStack.of(pose)
                .rotate(facing.getRotation())
                .rotateXDegrees(-90)
                .translate(0, 0, 0.5 - 0.0625);

        final float scale = this.getLaserScale(laser);
        pose.scale(scale, scale, 1);

        pose.translate(-0.5, -0.5, 0.0);
    }

    protected void createLaser(final Vector4f color, final PoseStack pose, final MultiBufferSource buffer, final float maxLength, final float length) {
        if (length <= 0 || maxLength <= 0) {
            return;
        }

        // These values deliberately match the original renderer. UV.x crossing 1.0
        // tells the native shader where to start fading the final part of the beam.
        final float lengthFrac = length / maxLength;
        final float offset = lengthFrac / 10;
        final float beamEnd = length + 0.5f;
        final float endU = 1 + 1 / length;

        final float red = color.x();
        final float blue = color.y();
        final float green = color.z();
        final float alpha = color.w();
        final float endAlpha = alpha * (1 - lengthFrac);

        final boolean shadersActive = OptionalShaderMods.isShaderPackActive();
        if (!shadersActive && LateLaserRenderQueue.isCollectingWorldFrame()) {
            final RenderType lateRenderType = SimRenderTypes.lateLaser();
            if (RenderBridge.isDiscoveringLayers()) {
                // Register a real bridge layer, but never capture the bridge's
                // identity-pose discovery invocation as world geometry.
                buffer.getBuffer(lateRenderType);
                return;
            }
            if (RenderBridge.isReplayingLayer()) {
                if (RenderBridge.isReplayingLayer(lateRenderType)) {
                    LateLaserRenderQueue.enqueue(pose.last().pose(), beamEnd, offset, endU,
                            red, green, blue, alpha, endAlpha);
                }
                return;
            }
            if (LateLaserRenderQueue.enqueue(pose.last().pose(), beamEnd, offset, endU,
                    red, green, blue, alpha, endAlpha)) {
                return;
            }
        }

        if (shadersActive && IrisLaserRenderQueue.isCollectingWorldFrame()) {
            final RenderType irisRenderType = SimRenderTypes.irisCompositeLaser();
            if (RenderBridge.isDiscoveringLayers()) {
                buffer.getBuffer(irisRenderType);
                return;
            }
            if (RenderBridge.isReplayingLayer()) {
                if (RenderBridge.isReplayingLayer(irisRenderType)) {
                    IrisLaserRenderQueue.enqueue(pose.last().pose(), beamEnd, offset, endU,
                            red, green, blue, alpha, endAlpha);
                }
                return;
            }
            if (IrisLaserRenderQueue.enqueue(pose.last().pose(), beamEnd, offset, endU,
                    red, green, blue, alpha, endAlpha)) {
                return;
            }
        }

        // Shader-pack world rendering must never fall through to an immediate
        // beacon/entity substitute. The normal Iris frame is handled above by
        // its isolated post-composite queue.
        if (shadersActive) {
            return;
        }

        final RenderType renderType = SimRenderTypes.laser();
        final VertexConsumer builder;
        if (buffer instanceof final SuperRenderTypeBuffer superRenderTypeBuffer) {
            builder = superRenderTypeBuffer.getLateBuffer(renderType);
        } else {
            builder = buffer.getBuffer(renderType);
        }

        pose.pushPose();
        final Quaternionf rotationQuat = Axis.ZN.rotationDegrees(90);

        for (int i = 0; i < 4; i++) {
            final Matrix4f matrix = pose.last().pose();
            addExactLaserSide(builder, matrix, beamEnd, offset, endU,
                    red, green, blue, alpha, endAlpha);

            pose.translate(0.5, 0.5, 0.5);
            pose.mulPose(rotationQuat);
            pose.translate(-0.5, -0.5, -0.5);
        }
        pose.popPose();
    }

    private static void addExactLaserSide(final VertexConsumer builder, final Matrix4f matrix,
                                          final float beamEnd, final float offset, final float endU,
                                          final float red, final float green, final float blue,
                                          final float alpha, final float endAlpha) {
        addExactLaserVertex(builder, matrix, 0, 0, 0,
                0, endU, red, green, blue, alpha);
        addExactLaserVertex(builder, matrix, 1, 0, 0,
                0, endU, red, green, blue, alpha);
        addExactLaserVertex(builder, matrix, 1 + offset, -offset, beamEnd,
                endU, endU, red, green, blue, endAlpha);
        addExactLaserVertex(builder, matrix, -offset, -offset, beamEnd,
                 endU, endU, red, green, blue, endAlpha);
    }

    static void addQueuedExactLaser(final VertexConsumer builder, final Matrix4f baseMatrix,
                                    final float beamEnd, final float offset, final float endU,
                                    final float red, final float green, final float blue,
                                    final float alpha, final float endAlpha) {
        final Matrix4f matrix = new Matrix4f(baseMatrix);
        final Quaternionf rotationQuat = Axis.ZN.rotationDegrees(90);
        for (int i = 0; i < 4; i++) {
            addExactLaserSide(builder, matrix, beamEnd, offset, endU,
                    red, green, blue, alpha, endAlpha);
            matrix.translate(0.5f, 0.5f, 0.5f);
            matrix.rotate(rotationQuat);
            matrix.translate(-0.5f, -0.5f, -0.5f);
        }
    }

    private static void addExactLaserVertex(final VertexConsumer builder, final Matrix4f matrix,
                                            final float x, final float y, final float z,
                                            final float u, final float v,
                                            final float red, final float green, final float blue, final float alpha) {
        builder.addVertex(matrix, x, y, z)
                .setColor(red, green, blue, alpha)
                .setUv(u, v)
                .setLight(LightTexture.FULL_BRIGHT)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setNormal(0, 1, 0);
    }

    public boolean shouldRenderOffScreen(final @NotNull T blockEntity) {
        return true;
    }

    public int getViewDistance() {
        return 256;
    }
}
