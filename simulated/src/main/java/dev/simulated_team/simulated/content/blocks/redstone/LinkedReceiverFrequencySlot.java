package dev.simulated_team.simulated.content.blocks.redstone;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.content.redstone.link.RedstoneLinkBlock;
import dev.simulated_team.simulated.compat.create.ValueBoxTransform;
import com.zurrtum.create.client.flywheel.lib.transform.TransformStack;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.catnip.math.VecHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class LinkedReceiverFrequencySlot extends ValueBoxTransform.Dual{
    public LinkedReceiverFrequencySlot(final boolean first) {
        super(first);
    }

    Vec3 horizontal = VecHelper.voxelSpace(5f, 3f, 2.5f);
    Vec3 vertical = VecHelper.voxelSpace(11f, 2.5f, 3f);

    @Override
    protected Vec3 rotateHorizontally(final BlockState state, final Vec3 location) {
        return VecHelper.rotateCentered(location, AngleHelper.horizontalAngle(state.getValue(RedstoneLinkBlock.FACING)), Direction.Axis.Y);
    }

    @Override
    public Vec3 getLocalOffset(final LevelAccessor levelAccessor, final BlockPos blockPos, final BlockState state) {
        final Direction facing = state.getValue(RedstoneLinkBlock.FACING);
        Vec3 location = this.vertical;

        if (facing.getAxis()
                .isHorizontal()) {
            location = this.horizontal;
            if (this.isFirst())
                location = location.add(0, 10 / 16f, 0);
            return this.rotateHorizontally(state, location);
        }

        if (this.isFirst())
            location = location.add(0, 0, 10 / 16f);
        location = VecHelper.rotateCentered(location, facing == Direction.DOWN ? 180 : 0, Direction.Axis.X);
        return location;
    }

    @Override
    public void rotate(final LevelAccessor levelAccessor, final BlockPos blockPos, final BlockState state, final PoseStack poseStack) {
        final Direction facing = state.getValue(RedstoneLinkBlock.FACING);
        final float yRot = facing.getAxis()
                .isVertical() ? 0 : AngleHelper.horizontalAngle(facing) + 180;
        final float xRot = facing == Direction.UP ? 90 : facing == Direction.DOWN ? 270 : 0;
        TransformStack.of(poseStack)
                .rotateYDegrees(yRot)
                .rotateXDegrees(xRot);
    }

    @Override
    public float getScale() {
        return .5f;
    }
}
