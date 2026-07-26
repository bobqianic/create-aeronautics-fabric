package dev.simulated_team.simulated.content.blocks.lasers.laser_sensor;

import com.zurrtum.create.content.redstone.DirectedDirectionalBlock;
import com.zurrtum.create.foundation.block.IBE;
import dev.simulated_team.simulated.index.SimBlockEntityTypes;
import dev.simulated_team.simulated.multiloader.CommonRedstoneBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.SignalGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import org.jetbrains.annotations.Nullable;

public class LaserSensorBlock extends DirectedDirectionalBlock implements IBE<LaserSensorBlockEntity>, CommonRedstoneBlock {
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    public LaserSensorBlock(final Properties props) {
        super(props);
    }

    @Override
    protected void createBlockStateDefinition(final StateDefinition.Builder<Block, BlockState> pBuilder) {
        super.createBlockStateDefinition(pBuilder.add(POWERED));
    }

    @Override
    public BlockState getStateForPlacement(final BlockPlaceContext context) {
        BlockState state = this.defaultBlockState();
        Direction preferredFacing;

        final Direction facing = context.getNearestLookingDirection();
        preferredFacing = context.getPlayer() != null && context.getPlayer()
                .isSteppingCarefully() ? facing : facing.getOpposite();

        if (preferredFacing.getAxis() == Direction.Axis.Y) {
            state = state.setValue(TARGET, preferredFacing == Direction.UP ? AttachFace.CEILING : AttachFace.FLOOR);
            preferredFacing = context.getHorizontalDirection();
        }

        return state.setValue(FACING, preferredFacing)
                .setValue(POWERED, false);
    }

    @Override
    public int getSignal(final BlockState pState, final BlockGetter level, final BlockPos pos, final Direction pDirection) {
        int power = 0;

        final LaserSensorBlockEntity blockEntity = this.getBlockEntity(level, pos);
        if (blockEntity != null) {
            power = Math.max(0, Math.min(15, blockEntity.currentPower));
        }

        return power;
    }

    @Override
    public boolean isSignalSource(final BlockState state) {
        return true;
    }

    @Override
    public boolean commonCheckWeakPower(final BlockState state, final SignalGetter level, final BlockPos pos, final Direction side) {
        return false;
    }

    @Override
    public boolean commonConnectRedstone(final BlockState state, final BlockGetter level, final BlockPos pos, @Nullable final Direction direction) {
        if (direction == null)
            return false;

        return direction != state.getValue(FACING).getOpposite();
    }

    @Override
    public Class<LaserSensorBlockEntity> getBlockEntityClass() {
        return LaserSensorBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends LaserSensorBlockEntity> getBlockEntityType() {
        return SimBlockEntityTypes.LASER_SENSOR.get();
    }
}
