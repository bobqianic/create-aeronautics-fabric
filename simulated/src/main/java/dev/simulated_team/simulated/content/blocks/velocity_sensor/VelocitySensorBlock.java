package dev.simulated_team.simulated.content.blocks.velocity_sensor;

import com.mojang.serialization.MapCodec;
import com.zurrtum.create.foundation.block.IBE;
import dev.simulated_team.simulated.content.blocks.util.AbstractDirectionalAxisBlock;
import dev.simulated_team.simulated.index.SimBlockEntityTypes;
import dev.simulated_team.simulated.multiloader.CommonRedstoneBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class VelocitySensorBlock extends AbstractDirectionalAxisBlock implements IBE<VelocitySensorBlockEntity>, CommonRedstoneBlock {
    public static final MapCodec<VelocitySensorBlock> CODEC = simpleCodec(VelocitySensorBlock::new);

    /**
     * 0 == unpowered, 1 == powered along positive axis, 2 == powered along negative axis
     */
    public static IntegerProperty POWERED = IntegerProperty.create("powered", 0, 2);

    public VelocitySensorBlock(final Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends DirectionalBlock> codec() {
        return CODEC;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(final BlockPlaceContext context) {
        return super.getStateForPlacement(context).setValue(POWERED, 0);
    }

    @Override
    protected int getSignal(final @NotNull BlockState blockState, final BlockGetter blockGetter, final BlockPos blockPos, final Direction direction) {
        final int powered = blockState.getValue(POWERED);
        if (powered == 0) {
            return 0;
        }

        Direction positiveDir = getDirectionOfAxis(blockState);
        if (powered == 2) {
            positiveDir = positiveDir.getOpposite();
        }

        if (direction != positiveDir) {
            return 0;
        }

        int power = 0;
        if (blockState.hasBlockEntity()) {
            power = ((VelocitySensorBlockEntity) blockGetter.getBlockEntity(blockPos)).getRedstoneStrength();
        }

        return power;
    }

    //needed, otherwise there is no way to get redstone signal out of the top of the velocity sensor on a wall!
    @Override
    protected int getDirectSignal(final BlockState state, final BlockGetter level, final BlockPos pos, final Direction direction) {
        final Direction facing = state.getValue(FACING);
        //if we're not on a wall, don't hardpower nearby blocks
        if (!facing.getAxis().isHorizontal()) {
            return 0;
        }

        //if we're on a wall, and on the east axis, not along the first coordinate, don't hardpower
        if (facing.getAxis() == Direction.EAST.getAxis() && !state.getValue(AXIS_ALONG_FIRST_COORDINATE)) {
            return 0;
        }

        //if we're on a wall, and on the north axis, along the first coordinate, don't hardpower
        if (facing.getAxis() == Direction.NORTH.getAxis() && state.getValue(AXIS_ALONG_FIRST_COORDINATE)) {
            return 0;
        }

        return this.getSignal(state, level, pos, direction);
    }

    @Override
    public boolean commonConnectRedstone(final BlockState state, final BlockGetter level, final BlockPos pos, @Nullable final Direction direction) {
        return direction != null && getAxis(state) == direction.getAxis();
    }

    @Override
    protected boolean isSignalSource(final BlockState blockState) {
        return true;
    }

    @Override
    protected void createBlockStateDefinition(final StateDefinition.Builder<Block, BlockState> pBuilder) {
        super.createBlockStateDefinition(pBuilder.add(POWERED));
    }

    @Override
    public Class<VelocitySensorBlockEntity> getBlockEntityClass() {
        return VelocitySensorBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends VelocitySensorBlockEntity> getBlockEntityType() {
        return SimBlockEntityTypes.VELOCITY_SENSOR.get();
    }
    private static final VelocitySensorShaper VELOCITY_SENSOR = VelocitySensorShaper.make();
    @Override
    public VoxelShape getShape(final BlockState state, final BlockGetter pLevel, final BlockPos pPos, final CollisionContext pContext) {
        return VELOCITY_SENSOR.get(state.getValue(FACING),  state.getValue(AXIS_ALONG_FIRST_COORDINATE));
    }

}
