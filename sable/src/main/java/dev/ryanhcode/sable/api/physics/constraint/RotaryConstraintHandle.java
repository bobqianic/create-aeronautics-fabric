package dev.ryanhcode.sable.api.physics.constraint;

/**
 * A constraint handle for a rotary / motor constraint between two bodies
 */
public non-sealed interface RotaryConstraintHandle extends PhysicsConstraintHandle {

    ConstraintJointAxis DEFAULT_AXIS = ConstraintJointAxis.ANGULAR_X;

    /**
     * Sets the servo coefficients for this rotary constraint.
     *
     * @param angle     the target angle [radians]
     * @param stiffness the stiffness of the servo
     * @param damping   the damping of the servo
     * @deprecated use {@link #setMotor(ConstraintJointAxis, double, double, double, boolean, double)} instead with {@link RotaryConstraintHandle#DEFAULT_AXIS}.
     */
    @Deprecated(forRemoval = true)
    default void setServoCoefficients(final double angle, final double stiffness, final double damping) {
        this.setMotor(DEFAULT_AXIS, angle, stiffness, damping, false, 0.0);
    }

}
