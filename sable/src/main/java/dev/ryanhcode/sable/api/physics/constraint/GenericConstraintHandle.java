package dev.ryanhcode.sable.api.physics.constraint;

import org.joml.Quaterniondc;
import org.joml.Vector3dc;

/**
 * A generic constraint between two bodies.
 *
 * @since 1.1.0
 */
public non-sealed interface GenericConstraintHandle extends PhysicsConstraintHandle {

    /**
     * Sets the local frame on the first body.
     *
     * @param localPosition the local anchor position
     * @param localOrientation the local frame orientation
     */
    void setFrame1(Vector3dc localPosition, Quaterniondc localOrientation);

    /**
     * Sets the local frame on the second body.
     *
     * @param localPosition the local anchor position
     * @param localOrientation the local frame orientation
     */
    void setFrame2(Vector3dc localPosition, Quaterniondc localOrientation);

    /**
     * Adds / sets a limit on this joint
     *
     * @param axis The axis on which the limit should be placed
     * @param min The minimum limit on the constraint axis
     * @param max The maximum limit on the constraint axis
     */
    void setLimit(ConstraintJointAxis axis, double min, double max);

    /**
     * Locks the given constraint axes on this joint
     *
     * @param axes The axes to lock
     */
    void lockAxes(ConstraintJointAxis... axes);
}
