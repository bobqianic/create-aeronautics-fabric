package dev.simulated_team.simulated.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;

import java.util.Arrays;

import static net.minecraft.core.Direction.*;

public class SimDirectionUtil {
    public static final Direction[] VALUES = Direction.values();
    /**
     * All directions except for ones that share the x-axis
     */
    public static Direction[] X_AXIS_PLANE = {NORTH, SOUTH, DOWN, UP};

    /**
     * All directions except for the ones that share the y-axis
     */
    public static Direction[] Y_AXIS_PLANE = {NORTH, SOUTH, EAST, WEST};

    /**
     * All directions except for the ones that share the z-axis
     */
    public static Direction[] Z_AXIS_PLANE = {DOWN, UP, EAST, WEST};

    public static BlockPos[] CUBIC_OFFSET = BlockPos.betweenClosedStream(-1, -1, -1, 1, 1, 1).map(BlockPos::immutable).toArray(BlockPos[]::new);

    public static Direction[] getSurroundingDirections(final Axis axis) {
        return switch (axis) {
            case X -> X_AXIS_PLANE;
            case Y -> Y_AXIS_PLANE;
            case Z -> Z_AXIS_PLANE;
        };
    }

    public static Direction[] getDirectionsExcept(final Direction dirToIgnore) {
        return Arrays.stream(values()).filter((d) -> d != dirToIgnore).toArray(Direction[]::new);
    }

    public static Direction directionFromNormal(final Vec3i normal) {
        for (final Direction dir : Direction.values()) {
            if(dir.getUnitVec3i().equals(normal)) return dir;
        }

        return Direction.UP;
    }
}
