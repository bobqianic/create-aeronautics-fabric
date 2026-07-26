package dev.simulated_team.simulated.content.blocks.rope.rope_winch;

import com.zurrtum.create.api.contraption.BlockMovementChecks;
import com.zurrtum.create.content.kinetics.base.DirectionalAxisKineticBlock;
import com.zurrtum.create.foundation.block.IBE;
import com.zurrtum.create.impl.contraption.BlockMovementChecksImpl;
import dev.ryanhcode.sable.api.block.BlockSubLevelAssemblyListener;
import dev.ryanhcode.sable.api.block.BlockSubLevelCollisionShape;
import dev.simulated_team.simulated.content.blocks.rope.RopeHolderBlock;
import dev.simulated_team.simulated.index.SimBlockEntityTypes;
import dev.simulated_team.simulated.index.SimBlockShapes;
import dev.simulated_team.simulated.index.SimTags;
import dev.simulated_team.simulated.util.DirectionalAxisShaper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class RopeWinchBlock extends DirectionalAxisKineticBlock implements IBE<RopeWinchBlockEntity>, RopeHolderBlock<RopeWinchBlockEntity>, BlockSubLevelAssemblyListener, BlockSubLevelCollisionShape {
    private static final DirectionalAxisShaper ROPE_WINCH = DirectionalAxisShaper.make(SimBlockShapes.ROPE_WINCH);
    private static final DirectionalAxisShaper PHYSICS_COLLIDER = DirectionalAxisShaper.make(SimBlockShapes.ROPE_CONNECTOR_COLLIDER);

    static {
        BlockMovementChecksImpl.registerAttachedCheck(
                (state, world, pos, direction) -> {
                    final BlockState relativeState = world.getBlockState(pos.relative(direction));
                    if (state.getBlock() instanceof RopeWinchBlock && state.getValue(RopeWinchBlock.FACING) == direction.getOpposite()) {
                        return BlockMovementChecks.CheckResult.SUCCESS;
                    }
                    if (relativeState.getBlock() instanceof RopeWinchBlock && relativeState.getValue(RopeWinchBlock.FACING) == direction) {
                        return BlockMovementChecks.CheckResult.SUCCESS;
                    }

                    return BlockMovementChecks.CheckResult.PASS;
                }
        );
    }

    public RopeWinchBlock(final Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntityType<? extends RopeWinchBlockEntity> getBlockEntityType() {
        return SimBlockEntityTypes.ROPE_WINCH.get();
    }

    @Override
    public Class<RopeWinchBlockEntity> getBlockEntityClass() {
        return RopeWinchBlockEntity.class;
    }

    @Override
    protected Direction getFacingForPlacement(final BlockPlaceContext context) {
        return context.getClickedFace();
    }

    @Override
    protected boolean getAxisAlignmentForPlacement(final BlockPlaceContext context) {
        return context.getHorizontalDirection()
                .getAxis() != Direction.Axis.X;
    }

    @Override
    public VoxelShape getShape(final BlockState state, final BlockGetter worldIn, final BlockPos pos, final CollisionContext context) {
        return ROPE_WINCH.get(state.getValue(FACING), state.getValue(AXIS_ALONG_FIRST_COORDINATE));
    }

    @Override
    protected InteractionResult useItemOn(final ItemStack stack, final BlockState state, final Level level, final BlockPos pos, final Player player, final InteractionHand hand, final BlockHitResult hitResult) {
        if (!level.isClientSide() && stack.is(SimTags.Items.DESTROYS_ROPE)) {
            return RopeHolderBlock.shearRope(this, level, pos, (ServerPlayer) player);
        }

        return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
    }

    @Override
    public VoxelShape getSubLevelCollisionShape(final BlockGetter blockGetter, final BlockState state) {
        return PHYSICS_COLLIDER.get(state.getValue(FACING), state.getValue(AXIS_ALONG_FIRST_COORDINATE));
    }
}
