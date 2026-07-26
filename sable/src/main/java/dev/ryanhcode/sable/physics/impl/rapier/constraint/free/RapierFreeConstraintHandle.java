package dev.ryanhcode.sable.physics.impl.rapier.constraint.free;

import dev.ryanhcode.sable.api.physics.PhysicsPipelineBody;
import dev.ryanhcode.sable.api.physics.constraint.FreeConstraintConfiguration;
import dev.ryanhcode.sable.api.physics.constraint.FreeConstraintHandle;
import dev.ryanhcode.sable.physics.impl.rapier.Rapier3D;
import dev.ryanhcode.sable.physics.impl.rapier.constraint.RapierConstraintHandle;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.Nullable;

public class RapierFreeConstraintHandle extends RapierConstraintHandle implements FreeConstraintHandle {
    /**
     * Creates a rapier constraint handle
     */
    public static RapierFreeConstraintHandle create(final ServerLevel serverLevel, @Nullable final PhysicsPipelineBody bodyA, @Nullable final PhysicsPipelineBody bodyB, final FreeConstraintConfiguration config) {
        final long sceneHandle = Rapier3D.getSceneHandle(serverLevel);

        final long handle = Rapier3D.addFreeConstraint(
                sceneHandle,
                bodyA == null ? -1 :  Rapier3D.getID(bodyA),
                bodyB == null ? -1 :  Rapier3D.getID(bodyB),
                config.pos1().x(),
                config.pos1().y(),
                config.pos1().z(),
                config.pos2().x(),
                config.pos2().y(),
                config.pos2().z(),
                config.orientation().x(),
                config.orientation().y(),
                config.orientation().z(),
                config.orientation().w()
        );

        return new RapierFreeConstraintHandle(sceneHandle, handle);
    }

    /**
     * Creates a new constraint handle
     *
     * @param sceneHandle the scene ID that this constraint is in
     * @param handle the handle from the physics engine
     */
    public RapierFreeConstraintHandle(final long sceneHandle, final long handle) {
        super(sceneHandle, handle);
    }

}
