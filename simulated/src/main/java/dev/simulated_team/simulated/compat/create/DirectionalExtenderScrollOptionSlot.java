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

import java.util.function.BiPredicate;

public class DirectionalExtenderScrollOptionSlot extends ValueBoxTransform.Sided {
    private final BiPredicate<BlockState, Direction> allowedDirections;

    public DirectionalExtenderScrollOptionSlot(final BiPredicate<BlockState, Direction> allowedDirections) {
        this.allowedDirections = allowedDirections;
    }

    @Override
    protected Vec3 getSouthLocation() {
        return VecHelper.voxelSpace(8, 8, 15.5);
    }

    @Override
    protected boolean isSideActive(final BlockState state, final Direction direction) {
        return allowedDirections.test(state, direction);
    }

    @Override
    public Vec3 getLocalOffset(final LevelAccessor level, final BlockPos pos, final BlockState state) {
        return super.getLocalOffset(level, pos, state)
                .add(Vec3.atLowerCornerOf(state.getValue(BlockStateProperties.FACING).getUnitVec3i()).scale(-2 / 16f));
    }

    @Override
    public void rotate(final LevelAccessor level, final BlockPos pos, final BlockState state, final PoseStack poseStack) {
        if (!getSide().getAxis().isHorizontal()) {
            TransformStack.of(poseStack)
                    .rotateYDegrees(AngleHelper.horizontalAngle(state.getValue(BlockStateProperties.FACING)) + 180);
        }
        super.rotate(level, pos, state, poseStack);
    }
}
