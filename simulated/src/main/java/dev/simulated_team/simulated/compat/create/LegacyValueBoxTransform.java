package dev.simulated_team.simulated.compat.create;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public abstract class LegacyValueBoxTransform extends com.zurrtum.create.client.foundation.blockEntity.behaviour.ValueBoxTransform {
    protected final SmartBlockEntity blockEntity;
    protected final ValueBoxTransform delegate;

    protected LegacyValueBoxTransform(final SmartBlockEntity blockEntity, final ValueBoxTransform delegate) {
        this.blockEntity = blockEntity;
        this.delegate = delegate;
        this.scale = delegate.getScale();
    }

    public static com.zurrtum.create.client.foundation.blockEntity.behaviour.ValueBoxTransform of(
            final SmartBlockEntity blockEntity, final ValueBoxTransform delegate) {
        if (delegate instanceof final ValueBoxTransform.Sided sided) {
            return new Sided(blockEntity, sided);
        }
        return new Standard(blockEntity, delegate);
    }

    protected LevelAccessor level() {
        return blockEntity.getLevel();
    }

    protected BlockPos pos() {
        return blockEntity.getBlockPos();
    }

    @Override
    public Vec3 getLocalOffset(final BlockState state) {
        return delegate.getLocalOffset(level(), pos(), state);
    }

    @Override
    public void rotate(final BlockState state, final PoseStack poseStack) {
        delegate.rotate(level(), pos(), state, poseStack);
    }

    @Override
    public boolean testHit(final LevelAccessor level, final BlockPos pos, final BlockState state, final Vec3 localHit) {
        return delegate.testHit(level, pos, state, localHit);
    }

    @Override
    public boolean shouldRender(final BlockState state) {
        return delegate.shouldRender(level(), pos(), state);
    }

    @Override
    public float getScale() {
        return delegate == null ? .5f : delegate.getScale();
    }

    @Override
    public float getFontScale() {
        return delegate == null ? 1 / 64f : delegate.getFontScale();
    }

    @Override
    public int getOverrideColor() {
        return delegate.getOverrideColor();
    }

    private static final class Standard extends LegacyValueBoxTransform {
        private Standard(final SmartBlockEntity blockEntity, final ValueBoxTransform delegate) {
            super(blockEntity, delegate);
        }
    }

    private static final class Sided extends com.zurrtum.create.client.foundation.blockEntity.behaviour.ValueBoxTransform.Sided {
        private final SmartBlockEntity blockEntity;
        private final ValueBoxTransform.Sided delegate;

        private Sided(final SmartBlockEntity blockEntity, final ValueBoxTransform.Sided delegate) {
            this.blockEntity = blockEntity;
            this.delegate = delegate;
            this.scale = delegate.getScale();
        }

        @Override
        public void fromSide(final Direction direction) {
            super.fromSide(direction);
            delegate.fromSide(direction);
        }

        @Override
        public Direction getSide() {
            return delegate == null ? super.getSide() : delegate.getSide();
        }

        @Override
        public Vec3 getLocalOffset(final BlockState state) {
            return delegate.getLocalOffset(blockEntity.getLevel(), blockEntity.getBlockPos(), state);
        }

        @Override
        protected Vec3 getSouthLocation() {
            return Vec3.ZERO;
        }

        @Override
        public void rotate(final BlockState state, final PoseStack poseStack) {
            delegate.rotate(blockEntity.getLevel(), blockEntity.getBlockPos(), state, poseStack);
        }

        @Override
        protected boolean isSideActive(final BlockState state, final Direction direction) {
            return delegate == null || delegate.shouldRender(blockEntity.getLevel(), blockEntity.getBlockPos(), state);
        }

        @Override
        public boolean testHit(final LevelAccessor level, final BlockPos pos, final BlockState state, final Vec3 localHit) {
            return delegate.testHit(level, pos, state, localHit);
        }

        @Override
        public boolean shouldRender(final BlockState state) {
            return delegate.shouldRender(blockEntity.getLevel(), blockEntity.getBlockPos(), state);
        }

        @Override
        public float getScale() {
            return delegate == null ? .5f : delegate.getScale();
        }

        @Override
        public float getFontScale() {
            return delegate == null ? 1 / 64f : delegate.getFontScale();
        }

        @Override
        public int getOverrideColor() {
            return delegate.getOverrideColor();
        }
    }
}
