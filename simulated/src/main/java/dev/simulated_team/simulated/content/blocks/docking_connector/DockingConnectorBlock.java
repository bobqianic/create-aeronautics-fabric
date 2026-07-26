package dev.simulated_team.simulated.content.blocks.docking_connector;

import com.google.common.collect.Maps;
import com.zurrtum.create.foundation.block.IBE;
import com.zurrtum.create.foundation.block.WrenchableDirectionalBlock;
import dev.ryanhcode.sable.api.block.BlockSubLevelAssemblyListener;
import dev.simulated_team.simulated.index.SimBlockEntityTypes;
import dev.simulated_team.simulated.index.SimBlocks;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class DockingConnectorBlock extends WrenchableDirectionalBlock implements IBE<DockingConnectorBlockEntity>, BlockSubLevelAssemblyListener {

    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public static final BooleanProperty EXTENDED = BlockStateProperties.EXTENDED;

    private static final VoxelShape UP_OPEN_AABB = Block.box(0.0, 15.0, 0.0, 16.0, 16.0, 16.0);
    private static final VoxelShape DOWN_OPEN_AABB = Block.box(0.0, 0.0, 0.0, 16.0, 1.0, 16.0);
    private static final VoxelShape WEST_OPEN_AABB = Block.box(0.0, 0.0, 0.0, 1.0, 16.0, 16.0);
    private static final VoxelShape EAST_OPEN_AABB = Block.box(15.0, 0.0, 0.0, 16.0, 16.0, 16.0);
    private static final VoxelShape NORTH_OPEN_AABB = Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 1.0);
    private static final VoxelShape SOUTH_OPEN_AABB = Block.box(0.0, 0.0, 15.0, 16.0, 16.0, 16.0);
    private static final Map<Direction, VoxelShape> OPEN_SHAPE_BY_DIRECTION = Util.make(Maps.newEnumMap(Direction.class), (enumMap) -> {
        enumMap.put(Direction.NORTH, NORTH_OPEN_AABB);
        enumMap.put(Direction.EAST, EAST_OPEN_AABB);
        enumMap.put(Direction.SOUTH, SOUTH_OPEN_AABB);
        enumMap.put(Direction.WEST, WEST_OPEN_AABB);
        enumMap.put(Direction.UP, UP_OPEN_AABB);
        enumMap.put(Direction.DOWN, DOWN_OPEN_AABB);
    });

    public DockingConnectorBlock(final Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState().setValue(POWERED, false).setValue(EXTENDED, false));
    }

    @Override
    protected void createBlockStateDefinition(final StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(POWERED, EXTENDED);
        super.createBlockStateDefinition(builder);
    }

    @Override
    public BlockState getStateForPlacement(final BlockPlaceContext context) {
        Direction nearestLookingDirection = context.getNearestLookingDirection();
        final Player player = context.getPlayer();
        if (player != null && player.isShiftKeyDown()) {
            nearestLookingDirection = nearestLookingDirection.getOpposite();
        }

        return super.getStateForPlacement(context).setValue(POWERED, context.getLevel().hasNeighborSignal(context.getClickedPos())).setValue(FACING, nearestLookingDirection.getOpposite());
    }

    @Override
    public void affectNeighborsAfterRemoval(final @NotNull BlockState state, final @NotNull ServerLevel level,
                                            final @NotNull BlockPos pos, final boolean isMoving) {
        final BlockState newState = level.getBlockState(pos);
        final boolean blockChanged = !state.is(newState.getBlock());

        // Remove the paired connector even if the block doesn't change
        if (state.getValue(POWERED) && (blockChanged || state.getValue(FACING) != newState.getValue(FACING))) {
            final BlockPos pairedConnectorPos = pos.relative(state.getValue(FACING));
            if (level.getBlockState(pairedConnectorPos).is(SimBlocks.PAIRED_DOCKING_CONNECTOR)) {
                level.removeBlock(pairedConnectorPos, isMoving);
            }
        }

        if (blockChanged) {
            level.getBlockEntity(pos, SimBlockEntityTypes.DOCKING_CONNECTOR.get()).ifPresent(connector -> Containers.dropContents(level, pos, connector.inventory));
        }

        super.affectNeighborsAfterRemoval(state, level, pos, isMoving);
    }

    @Override
    public void neighborChanged(final @NotNull BlockState state, final Level level, final @NotNull BlockPos pos,
                                final @NotNull Block block, final @Nullable Orientation wireOrientation,
                                final boolean isMoving) {
        if (level.isClientSide()) {
            return;
        }

        this.withBlockEntityDo(level, pos, DockingConnectorBlockEntity::updateSignal);
        final boolean previouslyPowered = state.getValue(POWERED);
        if (previouslyPowered != level.hasNeighborSignal(pos)) {
            level.setBlock(pos, state.cycle(POWERED), 2);
        }
    }

    @Override
    public @NotNull VoxelShape getShape(final @NotNull BlockState state, final BlockGetter level, final @NotNull BlockPos pos, final @NotNull CollisionContext context) {
        return level.getBlockEntity(pos) instanceof final DockingConnectorBlockEntity be && !be.isRetracted() ? Shapes.create(be.getBoundingBox(state)) : Shapes.block();
    }

    @Override
    public @NotNull VoxelShape getBlockSupportShape(final @NotNull BlockState state, final BlockGetter level, final @NotNull BlockPos pos) {
        return Shapes.block();
    }

    @Override
    public Class<DockingConnectorBlockEntity> getBlockEntityClass() {
        return DockingConnectorBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends DockingConnectorBlockEntity> getBlockEntityType() {
        return SimBlockEntityTypes.DOCKING_CONNECTOR.get();
    }

    @Override
    protected boolean triggerEvent(final @NotNull BlockState state, final @NotNull Level level, final @NotNull BlockPos pos, final int id, final int param) {
        super.triggerEvent(state, level, pos, id, param);
        final BlockEntity be = level.getBlockEntity(pos);
        return be != null && be.triggerEvent(id, param);
    }

    @Override
    public boolean hasAnalogOutputSignal(final BlockState pState) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(final BlockState pState, final Level pLevel, final BlockPos pPos, final Direction side) {
        final DockingConnectorBlockEntity be = this.getBlockEntity(pLevel, pPos);

        if (!be.isExtended())
            return 0;

        if (be.hasOtherConnector())
            return 15;

        return Math.min(14, Math.max(0, 14 - (int) (14 * be.closestPairDistance / 4.0)));
    }

    @Override
    public void afterMove(final ServerLevel originLevel, final ServerLevel resultingLevel, final BlockState newState, final BlockPos oldPos, final BlockPos newPos) {
        if (originLevel.getBlockEntity(oldPos) instanceof final DockingConnectorBlockEntity be &&
                be.hasOtherConnector() &&
                originLevel.getBlockEntity(be.otherConnectorPosition) instanceof final DockingConnectorBlockEntity connected &&
                connected.otherConnectorPosition.equals(oldPos) &&
                resultingLevel.getBlockEntity(newPos) instanceof final DockingConnectorBlockEntity newBe) {
            be.unDock();
            connected.unDock();
            newBe.unDock();
            newBe.pairTo(connected);

            // Don't update sound state on the client
            resultingLevel.blockEvent(newPos, this, 1, 0);
        }
    }
}
