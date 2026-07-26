package dev.ryanhcode.sable.physics.impl.rapier;

import org.jetbrains.annotations.ApiStatus;

/**
 * A physics scene, stored natively in {@link Rapier3D}
 */
@ApiStatus.Internal
public final class RapierPhysicsScene {
    private final long handle;

    RapierPhysicsScene(final long handle) {
        if (handle == 0L) {
            throw new IllegalArgumentException("invalid scene handle");
        }
        this.handle = handle;
    }

    long handle() {
        return this.handle;
    }
}
