package dev.simulated_team.simulated.content.blocks.directional_gearshift;

import com.zurrtum.create.content.kinetics.transmission.SplitShaftBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class DirectionalGearshiftBlockEntity extends SplitShaftBlockEntity {
    public DirectionalGearshiftBlockEntity(final BlockEntityType<?> type, final BlockPos pos, final BlockState state) {
        super(type, pos, state);
    }

    @Override
    public float getRotationSpeedModifier(final Direction face) {
        if (this.hasSource()) {
            if (face == this.getSourceFacing()) {
                return 1;
            }

            final boolean leftPowered = this.getBlockState().getValue(DirectionalGearshiftBlock.LEFT_POWERED);
            final boolean rightPowered = this.getBlockState().getValue(DirectionalGearshiftBlock.RIGHT_POWERED);
            if (rightPowered && leftPowered) {
                return 0;
            } else if (leftPowered) {
                return 1;
            } else if (rightPowered) {
                return -1;
            }
        }
        return 0;
    }
}
