package dev.simulated_team.simulated.content.blocks.redstone_magnet;

import com.zurrtum.create.foundation.block.IBE;
import com.zurrtum.create.foundation.block.WrenchableDirectionalBlock;
import dev.simulated_team.simulated.data.advancements.SimAdvancements;
import dev.simulated_team.simulated.index.SimBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.redstone.Orientation;

public class RedstoneMagnetBlock extends WrenchableDirectionalBlock implements IBE<RedstoneMagnetBlockEntity> {
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    public RedstoneMagnetBlock(final Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState().setValue(POWERED, false));
    }

    @Override
    protected void createBlockStateDefinition(final StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(POWERED);
        super.createBlockStateDefinition(builder);
    }

    @Override
    public BlockState getStateForPlacement(final BlockPlaceContext context) {
        Direction nearestLookingDirection = context.getNearestLookingDirection();
        if (context.getPlayer().isShiftKeyDown())
            nearestLookingDirection = nearestLookingDirection.getOpposite();

        return super.getStateForPlacement(context).setValue(POWERED,
                context.getLevel().hasNeighborSignal(context.getClickedPos())).setValue(FACING, nearestLookingDirection.getOpposite());
    }

    @Override
    protected void neighborChanged(final BlockState state, final Level level, final BlockPos pos, final Block blockIn, final Orientation orientation,
                                final boolean isMoving) {
        if (level.isClientSide())
            return;
        this.withBlockEntityDo(level, pos, RedstoneMagnetBlockEntity::updateSignal);
        final boolean previouslyPowered = state.getValue(POWERED);
        if (previouslyPowered != level.hasNeighborSignal(pos)) {
            level.setBlock(pos, state.cycle(POWERED), 2);
        }

        if (state.getValue(POWERED)) SimAdvancements.OPPOSITES_ATTRACT.awardToNearby(pos, level);
    }

    @Override
    public Class<RedstoneMagnetBlockEntity> getBlockEntityClass() {
        return RedstoneMagnetBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends RedstoneMagnetBlockEntity> getBlockEntityType() {
        return SimBlockEntityTypes.REDSTONE_MAGNET.get();
    }
}
