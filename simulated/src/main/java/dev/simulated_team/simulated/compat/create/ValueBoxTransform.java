package dev.simulated_team.simulated.compat.create;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.client.flywheel.lib.transform.TransformStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.tuple.Pair;

import java.util.function.Function;

public abstract class ValueBoxTransform {
    protected float scale = getScale();

    public abstract Vec3 getLocalOffset(LevelAccessor level, BlockPos pos, BlockState state);

    public abstract void rotate(LevelAccessor level, BlockPos pos, BlockState state, PoseStack poseStack);

    public final Vec3 getLocalOffset(final BlockState state) {
        return getLocalOffset(null, BlockPos.ZERO, state);
    }

    public final void rotate(final BlockState state, final PoseStack poseStack) {
        rotate(null, BlockPos.ZERO, state, poseStack);
    }

    public void transform(final BlockState state, final PoseStack poseStack) {
        final Vec3 position = getLocalOffset(state);
        if (position == null) {
            return;
        }
        poseStack.translate(position.x, position.y, position.z);
        rotate(state, poseStack);
        poseStack.scale(scale, scale, scale);
    }

    public boolean testHit(final LevelAccessor level, final BlockPos pos, final BlockState state, final Vec3 localHit) {
        final Vec3 offset = getLocalOffset(level, pos, state);
        return offset != null && localHit.distanceTo(offset) < scale / 2;
    }

    public boolean shouldRender(final LevelAccessor level, final BlockPos pos, final BlockState state) {
        return !state.isAir() && getLocalOffset(level, pos, state) != null;
    }

    public boolean shouldRender(final BlockState state) {
        return shouldRender(null, BlockPos.ZERO, state);
    }

    public float getScale() {
        return .5f;
    }

    public float getFontScale() {
        return 1 / 64f;
    }

    public int getOverrideColor() {
        return -1;
    }

    protected Vec3 rotateHorizontally(final BlockState state, final Vec3 vector) {
        float rotation = 0;
        if (state.hasProperty(BlockStateProperties.FACING)) {
            rotation = AngleHelper.horizontalAngle(state.getValue(BlockStateProperties.FACING));
        }
        if (state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
            rotation = AngleHelper.horizontalAngle(state.getValue(BlockStateProperties.HORIZONTAL_FACING));
        }
        return VecHelper.rotateCentered(vector, rotation, Direction.Axis.Y);
    }

    public static abstract class Dual extends ValueBoxTransform {
        protected final boolean first;

        protected Dual(final boolean first) {
            this.first = first;
        }

        public boolean isFirst() {
            return first;
        }

        public static Pair<ValueBoxTransform, ValueBoxTransform> makeSlots(final Function<Boolean, ? extends Dual> factory) {
            return Pair.of(factory.apply(true), factory.apply(false));
        }

        public boolean testHit(final LevelAccessor level, final BlockPos pos, final BlockState state, final Vec3 localHit) {
            final Vec3 offset = getLocalOffset(level, pos, state);
            return offset != null && localHit.distanceTo(offset) < scale / 3.5f;
        }
    }

    public static abstract class Sided extends ValueBoxTransform {
        protected Direction direction = Direction.UP;

        public Sided fromSide(final Direction direction) {
            this.direction = direction;
            return this;
        }

        public Vec3 getLocalOffset(final LevelAccessor level, final BlockPos pos, final BlockState state) {
            Vec3 location = getSouthLocation();
            location = VecHelper.rotateCentered(location, AngleHelper.horizontalAngle(getSide()), Direction.Axis.Y);
            return VecHelper.rotateCentered(location, AngleHelper.verticalAngle(getSide()), Direction.Axis.X);
        }

        protected abstract Vec3 getSouthLocation();

        public void rotate(final LevelAccessor level, final BlockPos pos, final BlockState state, final PoseStack poseStack) {
            final float yRot = AngleHelper.horizontalAngle(getSide()) + 180;
            final float xRot = getSide() == Direction.UP ? 90 : getSide() == Direction.DOWN ? 270 : 0;
            TransformStack.of(poseStack).rotateYDegrees(yRot).rotateXDegrees(xRot);
        }

        public boolean shouldRender(final LevelAccessor level, final BlockPos pos, final BlockState state) {
            return super.shouldRender(level, pos, state) && isSideActive(state, getSide());
        }

        public boolean testHit(final LevelAccessor level, final BlockPos pos, final BlockState state, final Vec3 localHit) {
            return isSideActive(state, getSide()) && super.testHit(level, pos, state, localHit);
        }

        protected boolean isSideActive(final BlockState state, final Direction direction) {
            return true;
        }

        public Direction getSide() {
            return direction;
        }
    }
}
