package dev.ryanhcode.sable.api.physics.constraint;

import dev.ryanhcode.sable.api.physics.PhysicsPipelineBody;
import dev.ryanhcode.sable.api.sublevel.ServerSubLevelContainer;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import org.joml.Vector3dc;

/**
 * A configuration for a physics constraint.
 * @param <T> the type of constraint handle this configuration produces
 */
public sealed interface PhysicsConstraintConfiguration<T extends PhysicsConstraintHandle> permits FixedConstraintConfiguration, FreeConstraintConfiguration, GenericConstraintConfiguration, RotaryConstraintConfiguration {

    static void validateAnchors(final ServerSubLevelContainer container, final PhysicsPipelineBody bodyA, final PhysicsPipelineBody bodyB, final Vector3dc pos1, final Vector3dc pos2) {
        if (bodyA instanceof final ServerSubLevel subLevel) {
            if (!subLevel.getPlot().contains(pos1)) {
                throw new IllegalArgumentException("pos1 does not fall within the plot of the first sub-level in block-coordinates! Double check your coordinate spaces.");
            }
        } else {
            if (container.inBounds(pos1)) {
                throw new IllegalArgumentException("the first body of this constraint is not a sub-level, but the first position is in the plotgrid! Double check your coordinate spaces.");
            }
        }

        if (bodyB instanceof final ServerSubLevel subLevel) {
            if (!subLevel.getPlot().contains(pos2)) {
                throw new IllegalArgumentException("pos2 does not fall within the plot of the second sub-level in block-coordinates! Double check your coordinate spaces.");
            }
        } else {
            if (container.inBounds(pos2)) {
                throw new IllegalArgumentException("the second body of this constraint is not a sub-level, but the second position is in the plotgrid! Double check your coordinate spaces.");
            }
        }
    }

    /**
     * Validates that this constraint can be added
     *
     * @param container the sub-level container the constraint is being applied in
     * @param bodyA     the first body the constraint is being applied to
     * @param bodyB     the first second the constraint is being applied to
     */
    default void validate(final ServerSubLevelContainer container, final PhysicsPipelineBody bodyA, final PhysicsPipelineBody bodyB) {

    }

}
