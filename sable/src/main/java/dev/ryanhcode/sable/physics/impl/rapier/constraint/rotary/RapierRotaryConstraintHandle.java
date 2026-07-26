package dev.ryanhcode.sable.physics.impl.rapier.constraint.rotary;

import dev.ryanhcode.sable.api.physics.PhysicsPipelineBody;
import dev.ryanhcode.sable.api.physics.constraint.RotaryConstraintConfiguration;
import dev.ryanhcode.sable.api.physics.constraint.RotaryConstraintHandle;
import dev.ryanhcode.sable.physics.impl.rapier.Rapier3D;
import dev.ryanhcode.sable.physics.impl.rapier.constraint.RapierConstraintHandle;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.Nullable;

public class RapierRotaryConstraintHandle extends RapierConstraintHandle implements RotaryConstraintHandle {
    /**
     * Creates a rapier constraint handle
     */
    public static RapierRotaryConstraintHandle create(final ServerLevel serverLevel, @Nullable final PhysicsPipelineBody bodyA, @Nullable final PhysicsPipelineBody bodyB, final RotaryConstraintConfiguration config) {
        final long sceneHandle = Rapier3D.getSceneHandle(serverLevel);

        final long handle = Rapier3D.addRotaryConstraint(
                sceneHandle,
                bodyA == null ? -1 :  Rapier3D.getID(bodyA),
                bodyB == null ? -1 :  Rapier3D.getID(bodyB),
                config.pos1().x(),
                config.pos1().y(),
                config.pos1().z(),
                config.pos2().x(),
                config.pos2().y(),
                config.pos2().z(),
                config.normal1().x(),
                config.normal1().y(),
                config.normal1().z(),
                config.normal2().x(),
                config.normal2().y(),
                config.normal2().z()
        );

        return new RapierRotaryConstraintHandle(sceneHandle, handle);
    }

    /**
     * Creates a new constraint handle
     *
     * @param sceneHandle the scene ID that this constraint is in
     * @param handle the handle from the physics engine
     */
    public RapierRotaryConstraintHandle(final long sceneHandle, final long handle) {
        super(sceneHandle, handle);
    }
}
