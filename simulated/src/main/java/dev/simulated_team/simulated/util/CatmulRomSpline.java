package dev.simulated_team.simulated.util;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class CatmulRomSpline {
    public static List<Vec3> generateSpline(final List<Vec3> controlPoints, final int numSegments) {
        final List<Vec3> splinePoints = new ObjectArrayList<>();

        for (int i = 1; i < controlPoints.size() - 2; i++) {
            for (int j = 0; j < numSegments; j++) {
                final double t = j / (double) numSegments;
                final Vec3 point = interpolate(controlPoints.get(i - 1), controlPoints.get(i),
                        controlPoints.get(i + 1), controlPoints.get(i + 2), t);
                splinePoints.add(point);
            }
        }

        return splinePoints;
    }

    private static Vec3 interpolate(final Vec3 p0, final Vec3 p1, final Vec3 p2, final Vec3 p3, final double t) {
        final double t2 = t * t;
        final double t3 = t2 * t;

        final double a = -0.5 * t3 + t2 - 0.5 * t;
        final double b = 1.5 * t3 - 2.5 * t2 + 1;
        final double c = -1.5 * t3 + 2 * t2 + 0.5 * t;
        final double d = 0.5 * t3 - 0.5 * t2;

        return p0.scale(a).add(p1.scale(b)).add(p2.scale(c)).add(p3.scale(d));
    }
}
