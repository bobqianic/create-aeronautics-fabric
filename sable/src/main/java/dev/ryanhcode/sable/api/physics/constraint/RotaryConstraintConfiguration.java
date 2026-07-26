package dev.ryanhcode.sable.api.physics.constraint;

import dev.ryanhcode.sable.api.physics.PhysicsPipelineBody;
import dev.ryanhcode.sable.api.sublevel.ServerSubLevelContainer;
import org.joml.Vector3dc;

/**
 * A configuration for a rotary joint constraint, with a single angular DOF.
 * @param pos1 the position in world space assumed to be inside the plot of the first sub-level (ex. a block position).
 * @param pos2 the position in world space assumed to be inside the plot of the second sub-level (ex. a block positino).
 * @param normal1 the local normal of the joint on the first sub-level.
 * @param normal2 the local normal of the joint on the second sub-level.
 */
public record RotaryConstraintConfiguration(Vector3dc pos1, Vector3dc pos2, Vector3dc normal1, Vector3dc normal2) implements PhysicsConstraintConfiguration<RotaryConstraintHandle> {

    private static final double NORMAL_LENGTH_SANITY_CHECK = 1.1 * 1.1;

    @Override
    public void validate(final ServerSubLevelContainer container, final PhysicsPipelineBody bodyA, final PhysicsPipelineBody bodyB) {
        PhysicsConstraintConfiguration.validateAnchors(container, bodyA, bodyB, this.pos1, this.pos2);

        if (this.normal1.lengthSquared() > NORMAL_LENGTH_SANITY_CHECK) {
            throw new IllegalArgumentException("The first normal in this constraint should be normalized: " + this.normal1);
        }

        if (this.normal2.lengthSquared() > NORMAL_LENGTH_SANITY_CHECK) {
            throw new IllegalArgumentException("The second normal in this constraint should be normalized: " + this.normal2);
        }
    }

}