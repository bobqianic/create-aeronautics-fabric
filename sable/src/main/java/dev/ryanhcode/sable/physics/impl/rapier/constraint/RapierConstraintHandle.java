package dev.ryanhcode.sable.physics.impl.rapier.constraint;

import dev.ryanhcode.sable.api.physics.constraint.ConstraintJointAxis;
import dev.ryanhcode.sable.physics.impl.rapier.Rapier3D;
import org.jetbrains.annotations.ApiStatus;
import org.joml.Vector3d;

@ApiStatus.Internal
public abstract class RapierConstraintHandle {

    protected final long sceneHandle;
    protected final long handle;
    private final double[] impulseCache;

    protected RapierConstraintHandle(final long sceneHandle, final long handle) {
        this.sceneHandle = sceneHandle;
        this.handle = handle;
        this.impulseCache = new double[6];
    }

    public void setContactsEnabled(final boolean enabled) {
        this.assertValid();
        Rapier3D.setConstraintContactsEnabled(this.sceneHandle, this.handle, enabled);
    }

    public void getJointImpulses(final Vector3d linearImpulseDest, final Vector3d angularImpulseDest) {
        this.assertValid();
        Rapier3D.getConstraintImpulses(this.sceneHandle, this.handle, this.impulseCache);
        linearImpulseDest.set(this.impulseCache[0], this.impulseCache[1], this.impulseCache[2]);
        angularImpulseDest.set(this.impulseCache[3], this.impulseCache[4], this.impulseCache[5]);
    }

    public void setMotor(final ConstraintJointAxis axis, final double target, final double stiffness, final double damping, final boolean hasForceLimit, final double maxForce) {
        this.assertValid();
        Rapier3D.setConstraintMotor(this.sceneHandle, this.handle, axis.ordinal(), target, stiffness, damping, hasForceLimit, maxForce);
    }

    public void remove() {
        Rapier3D.removeConstraint(this.sceneHandle, this.handle);
    }

    public boolean isValid() {
        return Rapier3D.isConstraintValid(this.sceneHandle, this.handle);
    }

    protected void assertValid() {
        if (!this.isValid()) {
            throw new RuntimeException("Attempted to mutate an invalid constraint");
        }
    }
}
