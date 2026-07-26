package dev.ryanhcode.sable.api.physics.callback;

import dev.ryanhcode.sable.companion.math.JOMLConversion;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;
import org.joml.Vector3dc;

public interface BlockSubLevelCollisionCallback {

    /**
     * Called when a collision occurs between two blocks, from JNI / pipeline implementations
     *
     * @return tangent motion
     */
    @ApiStatus.Internal
    @SuppressWarnings("unused")
    default double[] onCollision(final int x,
                                 final int y,
                                 final int z,
                                 final int otherX,
                                 final int otherY,
                                 final int otherZ,
                                 final double x1,
                                 final double y1,
                                 final double z1,
                                 final double impactVelocity,
                                 final boolean hasOtherBlock) {
        final CollisionResult result = this.sable$onCollision(new BlockPos(x, y, z), hasOtherBlock ? new BlockPos(otherX, otherY, otherZ) : null, new Vector3d(x1, y1, z1), impactVelocity);
        final Vector3dc motion = result.tangentMotion;

        // TODO: this is stupid and moronic to pass through the removal as a double lmao, let's not do that in the future
        return new double[]{motion.x(), motion.y(), motion.z(), result.removeCollision ? 1.0 : 0.0};
    }

    /**
     * Called when a collision occurs between two blocks
     * @param hitBlockPos the block that was hit (the block this callback is on)
     * @param otherHitBlockPos the other block that was hit (if the other body is a sub-level)
     * @param impactPosition the impact position, in the plot of the sub-level that was hit (or in global coordinates if a sub-level does not contain the block)
     * @param impactVelocity the velocity of the impact along the normal
     * @return the result of the collision callback
     */
    CollisionResult sable$onCollision(BlockPos hitBlockPos, @Nullable BlockPos otherHitBlockPos, Vector3d impactPosition, double impactVelocity);

    record CollisionResult(Vector3dc tangentMotion, boolean removeCollision) {
        public static final CollisionResult NONE = new CollisionResult(JOMLConversion.ZERO, false);
    }

}
