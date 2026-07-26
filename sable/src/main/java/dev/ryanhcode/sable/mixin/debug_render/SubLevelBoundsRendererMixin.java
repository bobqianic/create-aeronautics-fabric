package dev.ryanhcode.sable.mixin.debug_render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.companion.math.BoundingBox3dc;
import dev.ryanhcode.sable.companion.math.BoundingBox3ic;
import dev.ryanhcode.sable.companion.math.Pose3dc;
import dev.ryanhcode.sable.network.client.ClientSableInterpolationState;
import dev.ryanhcode.sable.network.client.SubLevelSnapshotInterpolator;
import dev.ryanhcode.sable.sublevel.ClientSubLevel;
import dev.ryanhcode.sable.sublevel.SubLevel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.debug.DebugScreenEntries;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShapeRenderer;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.debug.DebugRenderer;
import org.joml.Quaternionf;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DebugRenderer.class)
public class SubLevelBoundsRendererMixin {

    @Inject(method = "render", at = @At("TAIL"))
    private void sable$renderSubLevelBounds(
            final PoseStack poseStack,
            final Frustum frustum,
            final MultiBufferSource.BufferSource bufferSource,
            final double cameraX,
            final double cameraY,
            final double cameraZ,
            final boolean translucent,
            final CallbackInfo ci
    ) {
        final Minecraft minecraft = Minecraft.getInstance();

        if (!translucent
                || minecraft.showOnlyReducedInfo()
                || !minecraft.debugEntries.isCurrentlyEnabled(DebugScreenEntries.ENTITY_HITBOXES)
                || minecraft.level == null) {
            return;
        }

        final SubLevelContainer container = SubLevelContainer.getContainer(minecraft.level);
        if (container == null) {
            return;
        }

        final VertexConsumer consumer = bufferSource.getBuffer(RenderType.lines());

        for (final SubLevel subLevel : container.getAllSubLevels()) {
            if (!(subLevel instanceof final ClientSubLevel clientSubLevel)) {
                continue;
            }

            final BoundingBox3dc bounds = subLevel.boundingBox();

            ShapeRenderer.renderLineBox(
                    poseStack.last(),
                    consumer,
                    bounds.minX() - cameraX,
                    bounds.minY() - cameraY,
                    bounds.minZ() - cameraZ,
                    bounds.maxX() - cameraX,
                    bounds.maxY() - cameraY,
                    bounds.maxZ() - cameraZ,
                    0.5f, 0.5f, 0.5f, 0.7f
            );

            poseStack.pushPose();
            final Pose3dc renderPose = clientSubLevel.renderPose();
            final BoundingBox3ic plotBounds = subLevel.getPlot().getBoundingBox();

            final Vector3dc globalCenter = renderPose.position();
            final Vector3dc localCenter = renderPose.rotationPoint();

            poseStack.translate(
                    globalCenter.x() - cameraX,
                    globalCenter.y() - cameraY,
                    globalCenter.z() - cameraZ
            );
            poseStack.mulPose(new Quaternionf(renderPose.orientation()));

            ShapeRenderer.renderLineBox(
                    poseStack.last(),
                    consumer,
                    -2.0f / 16.0f,
                    -2.0f / 16.0f,
                    -2.0f / 16.0f,
                    2.0f / 16.0f,
                    2.0f / 16.0f,
                    2.0f / 16.0f,
                    0.7f, 0.7f, 0.5f, 1.0f
            );

            ShapeRenderer.renderLineBox(
                    poseStack.last(),
                    consumer,
                    plotBounds.minX() - localCenter.x(),
                    plotBounds.minY() - localCenter.y(),
                    plotBounds.minZ() - localCenter.z(),
                    plotBounds.maxX() + 1.0 - localCenter.x(),
                    plotBounds.maxY() + 1.0 - localCenter.y(),
                    plotBounds.maxZ() + 1.0 - localCenter.z(),
                    0.9f, 0.5f, 0.5f, 1.0f
            );
            poseStack.popPose();

            if (ClientSableInterpolationState.RENDER_INTERPOLATION_BOUNDS) {
                final Vector3d boundSize = bounds.size(new Vector3d());
                final SubLevelSnapshotInterpolator interpolator = clientSubLevel.getInterpolator();

                for (final SubLevelSnapshotInterpolator.Snapshot buffer : interpolator.buffer) {
                    final Pose3dc pose = buffer.pose();

                    ShapeRenderer.renderLineBox(
                            poseStack.last(),
                            consumer,
                            pose.position().x() - boundSize.x() / 2.0 - cameraX,
                            pose.position().y() - boundSize.y() / 2.0 - cameraY,
                            pose.position().z() - boundSize.z() / 2.0 - cameraZ,
                            pose.position().x() + boundSize.x() / 2.0 - cameraX,
                            pose.position().y() + boundSize.y() / 2.0 - cameraY,
                            pose.position().z() + boundSize.z() / 2.0 - cameraZ,
                            0.0f, 1.0f, 1.0f, 0.5f
                    );
                }
            }
        }
    }
}
