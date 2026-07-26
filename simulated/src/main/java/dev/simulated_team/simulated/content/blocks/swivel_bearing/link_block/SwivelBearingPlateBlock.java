package dev.simulated_team.simulated.content.blocks.swivel_bearing.link_block;

import com.zurrtum.create.content.kinetics.base.DirectionalKineticBlock;
import com.zurrtum.create.foundation.block.IBE;
import dev.ryanhcode.sable.api.block.BlockSubLevelAssemblyListener;
import dev.simulated_team.simulated.index.SimBlockEntityTypes;
import dev.simulated_team.simulated.index.SimBlockShapes;
import dev.simulated_team.simulated.index.SimBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SwivelBearingPlateBlock extends DirectionalKineticBlock implements IBE<SwivelBearingPlateBlockEntity>, BlockSubLevelAssemblyListener {

    public SwivelBearingPlateBlock(final Properties properties) {
        super(properties);
    }

    @Override
    public boolean hasShaftTowards(final LevelReader world, final BlockPos pos, final BlockState state, final Direction face) {
        return face == state.getValue(FACING);
    }

    @Override
    public void beforeMove(final ServerLevel originLevel, final ServerLevel resultingLevel, final BlockState newState, final BlockPos oldPos, final BlockPos newPos) {
        this.withBlockEntityDo(originLevel, oldPos, SwivelBearingPlateBlockEntity::beforeAssembly);
    }

    @Override
    public Direction.Axis getRotationAxis(final BlockState state) {
        return state.getValue(FACING).getAxis();
    }

    @Override
    public Class<SwivelBearingPlateBlockEntity> getBlockEntityClass() {
        return SwivelBearingPlateBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends SwivelBearingPlateBlockEntity> getBlockEntityType() {
        return SimBlockEntityTypes.SWIVEL_BEARING_LINK_BLOCK.get();
    }

    @Override
    protected VoxelShape getCollisionShape(final BlockState blockState, final BlockGetter blockGetter, final BlockPos blockPos, final CollisionContext collisionContext) {
        return SimBlockShapes.SWIVEL_BEARING_PLATE_COLLISION.get(blockState.getValue(FACING));
    }

    @Override
    protected VoxelShape getShape(final BlockState blockState, final BlockGetter blockGetter, final BlockPos blockPos, final CollisionContext collisionContext) {
        return SimBlockShapes.SWIVEL_BEARING_PLATE.get(blockState.getValue(FACING));
    }

    @Override
    protected VoxelShape getBlockSupportShape(final BlockState state, final BlockGetter level, final BlockPos pos) {
        return SimBlockShapes.SWIVEL_BEARING_PLATE.get(state.getValue(FACING));
    }

    @Override
    protected InteractionResult useItemOn(final ItemStack stack, final BlockState state, final Level level, final BlockPos pos, final Player player, final InteractionHand hand, final BlockHitResult hitResult) {
        if (!player.mayBuild()) {
            return InteractionResult.FAIL;
        }

        if (player.isShiftKeyDown()) {
            return InteractionResult.FAIL;
        }

        if (player.getItemInHand(hand).isEmpty()) {
            if (level.isClientSide()) {
                return InteractionResult.SUCCESS;
            }

            this.withBlockEntityDo(level, pos, SwivelBearingPlateBlockEntity::setParentAssembleNextTick);
        }

        return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
    }

    @Override
    public InteractionResult onWrenched(final BlockState state, final UseOnContext context) {
        return InteractionResult.PASS;
    }
    @Override
    protected ItemStack getCloneItemStack(final LevelReader level, final BlockPos pos, final BlockState state, final boolean includeData) {
        return SimBlocks.SWIVEL_BEARING.asStack();
    }

    @Override
    public void afterMove(final ServerLevel originLevel, final ServerLevel resultingLevel, final BlockState newState, final BlockPos oldPos, final BlockPos newPos) {
        this.withBlockEntityDo(resultingLevel, newPos, SwivelBearingPlateBlockEntity::fixParentLinkingWhenMoved);
    }
}
