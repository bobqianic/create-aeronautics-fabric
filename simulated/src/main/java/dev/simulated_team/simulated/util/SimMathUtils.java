package dev.simulated_team.simulated.util;

import com.mojang.math.Axis;
import dev.ryanhcode.sable.api.math.OrientedBoundingBox3d;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectIterable;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectList;
import com.zurrtum.create.catnip.math.AngleHelper;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.*;

import java.lang.Math;

public class SimMathUtils {

    /**
     * Rotates a vector by a quaternion
     *
     * @param V The vector to be rotated
     * @param Q The quaternion to rotate by
     * @return The rotated vector
     */
    public static Vec3 rotateQuat(final Vec3 V, final Quaterniond Q) {
        final Quaterniond q = new Quaterniond((float) V.x, (float) V.y, (float) V.z, 0.0f);
        final Quaterniond Q2 = new Quaterniond(Q);
        q.mul(Q2);
        Q2.conjugate();
        Q2.mul(q);
        return new Vec3(Q2.x(), Q2.y(), Q2.z());
    }

    /**
     * Rotates a vector by a quaternion
     *
     * @param V The vector to be rotated
     * @param Q The quaternion to rotate by
     * @return The rotated vector
     */
    public static Vec3 rotateQuat(final Vec3 V, final Quaternionf Q) {
        final Quaternionf q = new Quaternionf((float) V.x, (float) V.y, (float) V.z, 0.0f);
        final Quaternionf Q2 = new Quaternionf(Q);
        q.mul(Q2);
        Q2.conjugate();
        Q2.mul(q);
        return new Vec3(Q2.x(), Q2.y(), Q2.z());
    }

    /**
     * Rotates a vector by the inverse of a quaternion
     *
     * @param V The vector to be rotated
     * @param Q The quaternion to rotate by
     * @return The rotated vector
     */
    public static Vec3 rotateQuatReverse(final Vec3 V, final Quaterniond Q) {
        final Quaterniond q = new Quaterniond((float) V.x, (float) V.y, (float) V.z, 0.0f);
        final Quaterniond Q2 = new Quaterniond(Q);
        Q2.conjugate();
        q.mul(Q2);
        Q2.conjugate();
        Q2.mul(q);
        return new Vec3(Q2.x(), Q2.y(), Q2.z());
    }

    /**
     * Rotates a vector by the inverse of a quaternion
     *
     * @param V The vector to be rotated
     * @param Q The quaternion to rotate by
     * @return The rotated vector
     */
    public static Vec3 rotateQuatReverse(final Vec3 V, final Quaternionf Q) {
        final Quaternionf q = new Quaternionf((float) V.x, (float) V.y, (float) V.z, 0.0f);
        final Quaternionf Q2 = new Quaternionf(Q);
        Q2.conjugate();
        q.mul(Q2);
        Q2.conjugate();
        Q2.mul(q);
        return new Vec3(Q2.x(), Q2.y(), Q2.z());
    }

    /**
     * Clamps a normalized vector inside a cone, giving a maximum angle between the returned vector
     * and the axis vector of the cone
     *
     * @param v         Vector to be clamped
     * @param coneAxis  Central axis of the cone
     * @param coneAngle Maximum angle in radians between the axis vector and the output vector
     * @return Clamped vector
     */
    public static Vec3 clampIntoCone(final Vec3 v, final Vec3 coneAxis, final double coneAngle) {
        final double vv = v.dot(v);
        final double vn = v.dot(coneAxis);
        final double nn = coneAxis.dot(coneAxis);
        //the 1.01 is to prevent floating point issues when v=axis,
        //and also have it behave smoother when v is almost the opposite of axis
        final double disc = nn * vv * 1.01 - vn * vn;
        //quadratic formula
        final double offsetDistance = (-vn + Math.sqrt(disc) / Math.tan(coneAngle)) / nn;
        if (offsetDistance < 0 ^ coneAngle < 0) {
            return v;
        }

        return (v.add(coneAxis.scale(offsetDistance))).normalize();
    }

    public static void clampIntoCone(final Vector3d v, final Vector3d coneAxis, final double coneAngle) {
        final double vv = v.dot(v);
        final double vn = v.dot(coneAxis);
        final double nn = coneAxis.dot(coneAxis);
        //the 1.01 is to prevent floating point issues when v=axis,
        //and also have it behave smoother when v is almost the opposite of axis
        final double disc = nn * vv * 1.01 - vn * vn;
        //quadratic formula
        final double offsetDistance = (-vn + Math.sqrt(disc) / Math.tan(coneAngle)) / nn;
        if (offsetDistance < 0 ^ coneAngle < 0) {
            return;
        }
        v.add(new Vector3d(coneAxis).mul(offsetDistance)).normalize();

    }

    /**
     * Tests if a vector is inside a cylinder
     *
     * @param axisVector       Central axis of the cylinder, must be normalized
     * @param relativePosition Vector to be tested, relative to the base of the cylinder
     * @param cylinderLength   Length of the cylinder
     * @param cylinderRadius   Radius of the cylinder
     * @return If the check passed
     */
    public static boolean isInCylinder(final Vector3dc axisVector, Vector3d relativePosition, final double cylinderLength, final double cylinderRadius) {
        final double distance = axisVector.dot(relativePosition);
        if (distance < 0 || distance > cylinderLength) {
            return false;
        }

        final Vector3d scaledAxis = axisVector.mul(distance, new Vector3d());

        relativePosition = relativePosition.sub(scaledAxis, scaledAxis);
        return relativePosition.lengthSquared() <= cylinderRadius * cylinderRadius;
    }

    /**
     * Creates a new quaternion representing the orientation for the specified facing direction.
     *
     * @param facing The facing to get the orientation of
     * @return A new quaternion
     */
    public static Quaternionf getBlockStateOrientation(final Direction facing) {
        final Quaternionf orientation;
        if (facing.getAxis().isHorizontal()) {
            orientation = Axis.YP.rotationDegrees(AngleHelper.horizontalAngle(facing.getOpposite()));
        } else {
            orientation = new Quaternionf();
        }

        orientation.rotateX((-90.0F - AngleHelper.verticalAngle(facing)) * Mth.DEG_TO_RAD);
        return orientation;
    }

    public static Quaternionf getQuaternionfFromVectorRotation(final Vector3dc start, final Vector3dc end) {
        final Vector3d cross = new Vector3d();
        start.cross(end, cross);
        final Quaternionf Q = new Quaternionf((float) cross.x(), (float) cross.y(), (float) cross.z(), 1.0f + (float) start.dot(end));
        Q.normalize();
        return Q;
    }

    public static Quaterniond clampQuaternionToGrid(final Quaterniond q, final Iterable<Quaterniondc> gridQuats) {
        return clampQuaternionToGrid(q, gridQuats, q);
    }

    public static Quaterniond clampQuaternionToGrid(final Quaterniondc q, final Iterable<Quaterniondc> gridQuats, final Quaterniond dest) {

        //negative of sign of each component of q
        final int signX = q.x() < 0 ? -1 : 1;
        final int signY = q.y() < 0 ? -1 : 1;
        final int signZ = q.z() < 0 ? -1 : 1;
        final int signW = q.w() < 0 ? -1 : 1;

        dest.set(q);
        //enforce q to only have non-positive entries, so that adding behaves like subtraction
        dest.x *= -signX;
        dest.y *= -signY;
        dest.z *= -signZ;
        dest.w *= -signW;

        final Quaterniond temp = new Quaterniond();
        final Quaterniond best = new Quaterniond();
        double distance = 10;

        for (final Quaterniondc gq : gridQuats) {
            final double currentDist = dest.add(gq, temp).lengthSquared();
            if (currentDist < distance) {
                distance = currentDist;
                best.set(gq);
            }
        }

        dest.set(best);
        dest.x *= signX;
        dest.y *= signY;
        dest.z *= signZ;
        dest.w *= signW;
        return dest;
    }

    public static float smoothStep(final float t) {
        return t * t * (3 - 2 * t);
    }

    private static final Quaterniondc[] ALL_QUATS = new Quaterniondc[]{
            new Quaterniond(0, 0, 0, 1),
            new Quaterniond(1, 0, 0, 0),
            new Quaterniond(0, 1, 0, 0),
            new Quaterniond(0, 0, 1, 0),
            new Quaterniond(1, 0, 0, 1).normalize(),
            new Quaterniond(0, 1, 0, 1).normalize(),
            new Quaterniond(0, 0, 1, 1).normalize(),
            new Quaterniond(0, 1, 1, 0).normalize(),
            new Quaterniond(1, 0, 1, 0).normalize(),
            new Quaterniond(1, 1, 0, 0).normalize(),
            new Quaterniond(1, 1, 1, 1).normalize()
    };

    /**
     * @param orientation The orientation to get the yaw from
     * @return The closest yaw angle [rad]
     */
    public static double getClosestYaw(final Quaterniond orientation) {
        final double d = OrientedBoundingBox3d.UP.dot(new Vector3d(orientation.x(), orientation.y(), orientation.z()));
        return 2.0 * Math.atan2(-d, orientation.w());
    }

    public enum GridQuats implements ObjectIterable<Quaterniondc> {
        ALL(0b11111111111),
        X_AXIS(0b10011),
        Y_AXIS(0b100101),
        Z_AXIS(0b1001001),
        REAL(0b10001110001);

        private final ObjectList<Quaterniondc> currentQuats = new ObjectArrayList<>(ALL_QUATS.length);
        private final ObjectList<Quaterniondc> oppositeQuats = new ObjectArrayList<>(ALL_QUATS.length);

        GridQuats(int bitPattern) {
            for (final Quaterniondc q : ALL_QUATS) {
                (((bitPattern & 1) > 0) ? this.currentQuats : this.oppositeQuats).add(q);
                bitPattern >>= 1;
            }
        }

        public ObjectIterable<Quaterniondc> opposite() {
            return this.oppositeQuats::iterator;
        }

        @Override
        public @NotNull ObjectIterator<Quaterniondc> iterator() {
            return this.currentQuats.iterator();
        }
    }
}
