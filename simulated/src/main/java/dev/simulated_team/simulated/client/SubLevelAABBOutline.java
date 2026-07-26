package dev.simulated_team.simulated.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.client.catnip.outliner.AABBOutline;
import com.zurrtum.create.client.catnip.render.SuperRenderTypeBuffer;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.companion.math.Pose3dc;
import dev.ryanhcode.sable.sublevel.ClientSubLevel;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3dc;

/**
 * An outliner AABB expressed in parent-world or Sable plot coordinates.
 * Plot-space boxes are rendered using their sublevel's interpolated pose.
 */
public class SubLevelAABBOutline extends AABBOutline {
    private final Quaternionf orientation = new Quaternionf();

    public SubLevelAABBOutline(final AABB bounds) {
        super(bounds);
    }

    @Override
    public void render(final Minecraft minecraft, final PoseStack poseStack, final SuperRenderTypeBuffer buffer,
                       final Vec3 camera, final float partialTick) {
        final ClientSubLevel subLevel = Sable.HELPER.getContainingClient(this.bb.getCenter());
        if (subLevel == null) {
            super.render(minecraft, poseStack, buffer, camera, partialTick);
            return;
        }

        final Pose3dc pose = subLevel.renderPose(partialTick);
        final Vec3 localCamera = pose.transformPositionInverse(camera);
        final Vector3dc position = pose.position();
        final Vector3dc rotationPoint = pose.rotationPoint();
        final Vector3dc scale = pose.scale();

        poseStack.pushPose();
        poseStack.translate(position.x() - camera.x, position.y() - camera.y, position.z() - camera.z);
        poseStack.mulPose(this.orientation.set(pose.orientation()));
        poseStack.translate(
                -(rotationPoint.x() - localCamera.x),
                -(rotationPoint.y() - localCamera.y),
                -(rotationPoint.z() - localCamera.z)
        );
        poseStack.scale((float) scale.x(), (float) scale.y(), (float) scale.z());

        super.render(minecraft, poseStack, buffer, localCamera, partialTick);
        poseStack.popPose();
    }
}
