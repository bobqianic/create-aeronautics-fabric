package com.tterrag.registrate.providers;

import org.joml.Vector3f;

import java.util.Objects;

/**
 * Environment-neutral representation of an item model display transform.
 *
 * <p>Minecraft's old {@code ItemTransform} data holder became client-only, but
 * model data generation still runs from the dedicated-server entry point. The
 * vendored model generator only needs these four vectors to write JSON.</p>
 */
public final class RegistrateItemTransform {
    public static final RegistrateItemTransform NO_TRANSFORM = new RegistrateItemTransform(
            Deserializer.DEFAULT_ROTATION,
            Deserializer.DEFAULT_TRANSLATION,
            Deserializer.DEFAULT_SCALE
    );

    public final Vector3f field_4287;
    public final Vector3f field_4286;
    public final Vector3f field_4285;
    private Vector3f rightRotation = new Vector3f(Deserializer.DEFAULT_ROTATION);

    public RegistrateItemTransform(Vector3f rotation, Vector3f translation, Vector3f scale) {
        this.field_4287 = new Vector3f(rotation);
        this.field_4286 = new Vector3f(translation);
        this.field_4285 = new Vector3f(scale);
    }

    public Vector3f getRightRotation() {
        return rightRotation;
    }

    public void setRightRotation(Vector3f rightRotation) {
        this.rightRotation = new Vector3f(rightRotation);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof RegistrateItemTransform other)) {
            return false;
        }
        return field_4287.equals(other.field_4287)
                && field_4286.equals(other.field_4286)
                && field_4285.equals(other.field_4285)
                && rightRotation.equals(other.rightRotation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(field_4287, field_4286, field_4285, rightRotation);
    }

    public static final class Deserializer {
        public static final Vector3f DEFAULT_ROTATION = new Vector3f();
        public static final Vector3f DEFAULT_TRANSLATION = new Vector3f();
        public static final Vector3f DEFAULT_SCALE = new Vector3f(1.0F, 1.0F, 1.0F);

        private Deserializer() {
        }
    }
}
