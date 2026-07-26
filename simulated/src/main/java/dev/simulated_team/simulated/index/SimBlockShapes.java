package dev.simulated_team.simulated.index;

import com.zurrtum.create.catnip.math.VoxelShaper;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.function.BiFunction;

import static net.minecraft.core.Direction.*;

public class SimBlockShapes {

    public static final VoxelShaper
            PLUNGER_BLOCK =
            shape(4, 12, 4, 12, 16, 12)
                    .add(7, -6, 7, 9, 10, 9)
                    .add(6, 10, 6, 10, 12, 10)
                    .forDirectional(),

    SYMMETRIC_SAIL =
            shape(0, 6, 0, 16, 10, 16)
                    .forDirectional(),

    OPTICAL_SENSOR =
            shape(0, 0, 0, 16, 16, 6)
                    .add(0, 0, 10, 16, 16, 16)
                    .add(1, 1, 6, 15, 15, 10)
                    .forDirectional(Direction.NORTH),

    REDSTONE_INDUCTOR =
            shape(0, 0, 0, 16, 2, 16)
                    .add(4, 2, 5, 12, 6, 11)
                    .forHorizontal(NORTH),

    MODULATING_DIRECTIONAL_LINK =
            shape(1, 0, 1, 15, 3, 15).forDirectional(),

    LINKED_TYPEWRITER =
            shape(0, 0, 0, 16, 3, 7)
                    .add(0, 0, 7, 16, 7, 16)
                    .forHorizontal(Direction.NORTH),

    SWIVEL_BEARING_ASSEMBLED =
            shape(0, 0, 0, 16, 11.9, 16)
                    .forDirectional(Direction.UP),

    SWIVEL_BEARING_PLATE =
            shape(0, 12.1, 0, 16, 16, 16)
                    .forDirectional(Direction.UP),

    SWIVEL_BEARING_PLATE_COLLISION =
            shape(3, 12, 3, 13, 16, 13)
                    .forDirectional(Direction.UP),

    PORTABLE_ENGINE = shape(0, 0, 0, 16, 4, 16)
            .add(3, 2, 1, 13, 14, 15)
            .forDirectional(Direction.NORTH),

    PHYSICS_ASSEMBLER_COLLISION =
            shape(0, 0, 0, 16, 3, 16)
                    .add(2, 3, 2, 14, 12, 14)
                    .forDirectional(NORTH),

    PHYSICS_ASSEMBLER_CEILING_COLLISION =
            shape(0, 13, 0, 16, 16, 16)
                    .add(2, 4, 2, 14, 13, 14)
                    .forDirectional(SOUTH),

    PHYSICS_ASSEMBLER_WALL_COLLISION =
            shape(0, 0, 0, 16, 3, 16)
                    .add(2, 3, 2, 14, 12, 14)
                    .forDirectional(DOWN),

    PHYSICS_ASSEMBLER_OUTLINE =
            shape(0, 0, 0, 16, 4, 16)
                    .add(2, 3, 2, 5, 13, 14)
                    .add(2, 3, 2, 14, 6, 14)
                    .add(11, 3, 2, 14, 13, 14)
                    .forDirectional(NORTH),

    PHYSICS_ASSEMBLER_CEILING_OUTLINE =
            shape(0, 12, 0, 16, 16, 16)
                    .add(2, 3, 2, 5, 13, 14)
                    .add(2, 10, 2, 14, 13, 14)
                    .add(11, 3, 2, 14, 13, 14)
                    .forDirectional(SOUTH),

    PHYSICS_ASSEMBLER_WALL_OUTLINE =
            shape(0, 0, 0, 16, 4, 16)
                    .add(2, 3, 2, 5, 13, 14)
                    .add(2, 3, 2, 14, 6, 14)
                    .add(11, 3, 2, 14, 13, 14)
                    .forDirectional(DOWN),

    ALTITUDE_SENSOR_FLOOR = shape(1, 2, 6, 15, 10, 10)
            .add(2, 2, 4, 14, 14, 9)
            .add(4, 4, 9, 12, 12, 13)
            .add(0, 0, 0, 16, 2, 16)
            .forHorizontal(NORTH),
            ALTITUDE_SENSOR_CEILING = shape(1, 6, 6, 15, 14, 10)
                    .add(2, 2, 4, 14, 14, 9)
                    .add(4, 4, 9, 12, 12, 13)
                    .add(0, 14, 0, 16, 16, 16)
                    .forHorizontal(NORTH),

    ALTITUDE_SENSOR_WALL = shape(1, 6, 6, 15, 10, 14)
            .add(2, 2, 4, 14, 14, 9)
            .add(4, 4, 9, 12, 12, 14)
            .add(0, 0, 14, 16, 16, 16)
            .forHorizontal(NORTH),

    NAV_TABLE = shape(0, 9, 0, 16, 13, 16)
            .add(0, 0, 0, 16, 2, 16)
            .add(2, 2, 2, 14, 9, 14)
            .forDirectional(),

    STEERING_WHEEL_MOUNT = shape(2, 2, 0, 14, 12, 16)
            .forDirectional(),
            STEERING_WHEEL_FLOOR = shape(-1, 13.5, -6, 17, 15.5, 12)
                    .forDirectional(),
            STEERING_WHEEL_CEILING = shape(-1, 13.5, 4, 17, 15.5, 22)
                    .forDirectional(),
            STEERING_WHEEL_FULL_FLOOR = shape(STEERING_WHEEL_MOUNT.get(UP)).add(STEERING_WHEEL_FLOOR.get(UP))
                    .forDirectional(),
            STEERING_WHEEL_FULL_CEILING = shape(STEERING_WHEEL_MOUNT.get(UP)).add(STEERING_WHEEL_CEILING.get(UP))
                    .forDirectional(),

    FOURTEEN_VOXEL_POLE = shape(1, 0, 1, 15, 16, 15).forAxis(),
            AUGER_END_SHAPE = shape(1, 0, 1, 15, 16, 15).add(0, 0, 0, 16, 2, 16)
                    .forDirectional(),

    THROTTLE_LEVER = shape(4, 0, 3, 12, 3, 13)
            .add(4, 0, 6, 12, 5, 10)
            .forDirectional(UP),

    THROTTLE_LEVER_SWAP = shape(3, 0, 4, 13, 3, 12)
            .add(6, 0, 4, 10, 5, 12)
            .forDirectional(UP),

    THROTTLE_LEVER_HANDLE = shape(7, 3, 7, 9, 15, 9)
            .add(7 - 0.2, 15, 7 - 0.2, 9 + 0.2, 21.4,  9 + 0.2)
            .forDirectional(UP),

    THROTTLE_LEVER_HANDLE_SWAP = shape(7, 3, 7, 9, 15, 9)
            .add(7 - 0.2, 15, 7 - 0.2, 9 + 0.2, 21.4,  9 + 0.2)
            .forDirectional(UP),

    TORSION_SPRING = shape(0, 0, 0, 16, 6, 16)
            .add(2, 0, 2, 14, 16, 14)
            .forDirectional(UP),

    SMALL_SPRING = shape(5, 0, 5, 11, 4, 11)
            .forDirectional(UP),

    SPRING = shape(4, 0, 4, 12, 4, 12)
            .forDirectional(UP),

    LARGE_SPRING = shape(3, 0, 3, 13, 4, 13)
            .forDirectional(UP),

    MERGING_GlUE = shape(0, 0, 0, 16, 1, 16)
            .forDirectional(UP),

    LASER_POINTER = shape(0, 0, 0, 16, 10, 16)
            .add(1, 10, 1, 15, 12, 15).forDirectional(),

    NAMEPLATE = shape(0, 3, 12, 16, 13, 16)
            .forDirectional(NORTH);

    public static final VoxelShape

            HANDLE =
            shape(4, 0, 0, 12, 6, 16)
                    .build(),

    VELOCITY_SENSOR =
            shape(0, 0, 0, 16, 2, 16)
                    .add(0, 2, 2, 16, 16, 14)
                    .build(),

    REDSTONE_ACCUMULATOR =
            shape(0, 0, 0, 16, 2, 16)
                    .add(4, 2, 4, 12, 7, 12)
                    .build(),

    ROPE_WINCH =
            shape(4, 0, 0, 12, 2, 16)
                    .add(2, 2, 0, 14, 14, 16)
                    .build(),

    ROPE_CONNECTOR =
            shape(3, 0, 3, 13, 2, 13)
                    .add(6, 2, 3, 10, 6, 13)
                    .build(),

    ROPE_CONNECTOR_COLLIDER =
            shape(1, 0, 1, 15, 0.25, 15)
                    .build(),

    GIMBAL_SENSOR = shape(0, 0, 0, 16, 10, 16)
            .build(),

    EVAPORATOR = shape(0, 0, 0, 16, 8, 16)
            .build();

    // create also has a stray semicolon so i thought i'd add one to be twinsies

    private static SimBlockShapes.Builder shape(final VoxelShape shape) {
        return new SimBlockShapes.Builder(shape);
    }

    private static SimBlockShapes.Builder shape(final double x1, final double y1, final double z1, final double x2, final double y2, final double z2) {
        return shape(cuboid(x1, y1, z1, x2, y2, z2));
    }

    private static VoxelShape cuboid(final double x1, final double y1, final double z1, final double x2, final double y2, final double z2) {
        return Block.box(x1, y1, z1, x2, y2, z2);
    }


    public static class Builder {
        private VoxelShape shape;

        public Builder(final VoxelShape shape) {
            this.shape = shape;
        }

        public SimBlockShapes.Builder add(final VoxelShape shape) {
            this.shape = Shapes.or(this.shape, shape);
            return this;
        }

        public SimBlockShapes.Builder add(final double x1, final double y1, final double z1, final double x2, final double y2, final double z2) {
            return this.add(cuboid(x1, y1, z1, x2, y2, z2));
        }

        public SimBlockShapes.Builder erase(final double x1, final double y1, final double z1, final double x2, final double y2, final double z2) {
            this.shape = Shapes.join(this.shape, cuboid(x1, y1, z1, x2, y2, z2), BooleanOp.ONLY_FIRST);
            return this;
        }

        public VoxelShape build() {
            return this.shape;
        }

        public VoxelShaper build(final BiFunction<VoxelShape, Direction, VoxelShaper> factory, final Direction direction) {
            return factory.apply(this.shape, direction);
        }

        public VoxelShaper build(final BiFunction<VoxelShape, Axis, VoxelShaper> factory, final Axis axis) {
            return factory.apply(this.shape, axis);
        }

        public VoxelShaper forAxis() {
            return this.build(VoxelShaper::forAxis, Axis.Y);
        }

        public VoxelShaper forHorizontalAxis() {
            return this.build(VoxelShaper::forHorizontalAxis, Axis.Z);
        }

        public VoxelShaper forHorizontal(final Direction direction) {
            return this.build(VoxelShaper::forHorizontal, direction);
        }

        public VoxelShaper forDirectional(final Direction direction) {
            return this.build(VoxelShaper::forDirectional, direction);
        }

        public VoxelShaper forDirectional() {
            return this.forDirectional(Direction.UP);
        }
    }
}
