package dev.simulated_team.simulated.content.physics_staff;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.world.phys.Vec3;

final class PhysicsStaffOverlayRenderer {
    private static final double MIN_NORMAL_LENGTH_SQUARED = 1.0e-8;

    private PhysicsStaffOverlayRenderer() {
    }

    static void bufferRibbon(final PoseStack.Pose pose, final VertexConsumer buffer,
                             final Vec3 start, final Vec3 end, final Vec3 camera,
                             final float width, final int color) {
        final Vec3 direction = end.subtract(start);
        final double directionLengthSquared = direction.lengthSqr();
        if (directionLengthSquared < MIN_NORMAL_LENGTH_SQUARED) {
            return;
        }

        final Vec3 midpoint = start.add(end).scale(0.5);
        Vec3 side = direction.cross(camera.subtract(midpoint));

        if (side.lengthSqr() < MIN_NORMAL_LENGTH_SQUARED) {
            side = direction.cross(new Vec3(0, 1, 0));
        }
        if (side.lengthSqr() < MIN_NORMAL_LENGTH_SQUARED) {
            side = direction.cross(new Vec3(1, 0, 0));
        }
        if (side.lengthSqr() < MIN_NORMAL_LENGTH_SQUARED) {
            return;
        }

        side = side.normalize().scale(width * 0.5f);
        // Adjacent polyline segments use separate quads. Extend their butt caps by
        // half the line width so bends overlap instead of exposing triangular gaps.
        final Vec3 capOverlap = direction.scale(
                width * 0.5 / Math.sqrt(directionLengthSquared));
        final Vec3 relativeStart = start.subtract(capOverlap).subtract(camera);
        final Vec3 relativeEnd = end.add(capOverlap).subtract(camera);

        // Keep the camera-facing side counter-clockwise. The vanilla lightning pipeline
        // culls back faces, so reversing this order makes the entire ribbon disappear.
        addVertex(pose, buffer, relativeStart.add(side), color);
        addVertex(pose, buffer, relativeEnd.add(side), color);
        addVertex(pose, buffer, relativeEnd.subtract(side), color);
        addVertex(pose, buffer, relativeStart.subtract(side), color);
    }

    private static void addVertex(final PoseStack.Pose pose, final VertexConsumer buffer,
                                  final Vec3 position, final int color) {
        buffer.addVertex(pose, (float) position.x, (float) position.y, (float) position.z)
                .setColor(color);
    }
}
