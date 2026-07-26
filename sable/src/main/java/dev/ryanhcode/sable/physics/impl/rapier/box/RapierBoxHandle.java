package dev.ryanhcode.sable.physics.impl.rapier.box;

import dev.ryanhcode.sable.api.physics.object.box.BoxHandle;
import dev.ryanhcode.sable.companion.math.Pose3d;
import dev.ryanhcode.sable.companion.math.Pose3dc;
import dev.ryanhcode.sable.physics.impl.rapier.Rapier3D;
import org.jetbrains.annotations.ApiStatus;
import org.joml.Quaterniondc;
import org.joml.Vector3dc;

@ApiStatus.Internal
public record RapierBoxHandle(long sceneHandle, int id, double[] poseCache) implements BoxHandle {

    public static RapierBoxHandle create(final long sceneHandle, final Pose3dc pose, final Vector3dc halfExtents, final double mass) {
        final Vector3dc pos = pose.position();
        final Quaterniondc rot = pose.orientation();

        final int id = Rapier3D.nextBodyID();
        Rapier3D.createBox(sceneHandle, id, mass, halfExtents.x(), halfExtents.y(), halfExtents.z(), new double[]{pos.x(), pos.y(), pos.z(), rot.x(), rot.y(), rot.z(), rot.w()});
        return new RapierBoxHandle(sceneHandle, id, new double[7]);
    }

    /**
     * Queries the pose of the box from the physics engine
     */
    @Override
    public void readPose(final Pose3d dest) {
        Rapier3D.getPose(this.sceneHandle, this.id, this.poseCache);

        dest.position().set(this.poseCache[0], this.poseCache[1], this.poseCache[2]);
        dest.orientation().set(this.poseCache[3], this.poseCache[4], this.poseCache[5], this.poseCache[6]);
    }

    /**
     * Removes the rope from the physics pipeline
     */
    @Override
    public void remove() {
        Rapier3D.removeBox(this.sceneHandle, this.id);
    }

    /**
     * Wakes up the rope
     */
    @Override
    public void wakeUp() {
        Rapier3D.wakeUpObject(this.sceneHandle, this.id);
    }

    /**
     * @return the runtime ID of the box
     */
    @Override
    public int getRuntimeId() {
        return this.id;
    }
}
