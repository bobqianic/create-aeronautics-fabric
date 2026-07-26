package dev.simulated_team.simulated.content.blocks.swivel_bearing;

import com.zurrtum.create.content.equipment.wrench.IWrenchable;
import com.zurrtum.create.content.kinetics.base.DirectionalKineticBlock;
import com.zurrtum.create.content.kinetics.base.IRotate;
import com.zurrtum.create.content.kinetics.base.KineticBlockEntity;
import com.zurrtum.create.content.kinetics.simpleRelays.CogwheelBlockItem;
import com.zurrtum.create.foundation.block.IBE;
import dev.ryanhcode.sable.api.block.BlockSubLevelAssemblyListener;
import dev.simulated_team.simulated.index.SimBlockEntityTypes;
import dev.simulated_team.simulated.index.SimBlockShapes;
import dev.simulated_team.simulated.index.SimBlocks;
import dev.simulated_team.simulated.util.extra_kinetics.ExtraKinetics;
import dev.simulated_team.simulated.util.placement_helpers.CogwheelPlacementExtension;
import com.zurrtum.create.catnip.placement.IPlacementHelper;
import com.zurrtum.create.catnip.placement.PlacementHelpers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SwivelBearingBlock extends DirectionalKineticBlock implements IBE<SwivelBearingBlockEntity>, IRotate, ExtraKinetics.ExtraKineticsBlock, BlockSubLevelAssemblyListener {
    public static final BooleanProperty ASSEMBLED = BooleanProperty.create("assembled");
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    private static final int placementHelperId = PlacementHelpers.register(new CogwheelPlacementExtension(
            i -> i.getItem() instanceof CogwheelBlockItem, state -> SimBlocks.SWIVEL_BEARING.has(state)));

    public SwivelBearingBlock(final Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState().setValue(ASSEMBLED, false).setValue(POWERED, false));
    }

    @Override
    protected void createBlockStateDefinition(final StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder.add(ASSEMBLED).add(POWERED));
    }

    @Override
    protected InteractionResult useItemOn(final ItemStack itemStack, final BlockState blockState, final Level level, final BlockPos blockPos, final Player player, final InteractionHand interactionHand, final BlockHitResult blockHitResult) {
        if (!player.mayBuild()) {
            return InteractionResult.FAIL;
        }

        if (player.isShiftKeyDown()) {
            return InteractionResult.FAIL;
        }

        if (player.getItemInHand(interactionHand).isEmpty()) {
            if (level.isClientSide()) {
                return InteractionResult.SUCCESS;
            }

            this.withBlockEntityDo(level, blockPos, be -> be.assembleNextTick = true);
            return InteractionResult.SUCCESS;
        }

        final ItemStack heldItem = player.getItemInHand(interactionHand);
        final IPlacementHelper helper = PlacementHelpers.get(placementHelperId);
        if (helper.matchesItem(heldItem)) {
            return helper
                    .getOffset(player, level, blockState, blockPos, blockHitResult)
                    .placeInWorld(level, (BlockItem) heldItem.getItem(), player, interactionHand);
        }


        return InteractionResult.PASS;
    }

    @Override
    public Direction.Axis getRotationAxis(final BlockState blockState) {
        return blockState.getValue(FACING).getAxis();
    }

    @Override
    public boolean hasShaftTowards(final LevelReader world, final BlockPos pos, final BlockState state, final Direction face) {
        final Direction facing = state.getValue(FACING);
        return state.getValue(ASSEMBLED) ? face == facing.getOpposite() : face.getAxis() == facing.getAxis();
    }

    @Override
    public InteractionResult onWrenched(final BlockState state, final UseOnContext context) {
        final Level level = context.getLevel();
        final BlockPos pos = context.getClickedPos();
        BlockState rotated = this.getRotatedBlockState(state, context.getClickedFace());
        if (!rotated.canSurvive(level, context.getClickedPos()))
            return InteractionResult.PASS;

        if (!level.isClientSide()) {
            this.withBlockEntityDo(level, pos, SwivelBearingBlockEntity::disassemble);
        }

        // blockstate could have changed from disassembly
        rotated = this.getRotatedBlockState(level.getBlockState(pos), context.getClickedFace());
        KineticBlockEntity.switchToBlockState(level, pos, this.updateAfterWrenched(rotated, context));

        if (level.getBlockState(pos) != state)
            IWrenchable.playRotateSound(level, pos);

        return InteractionResult.SUCCESS;
    }

    @Override
    public Class<SwivelBearingBlockEntity> getBlockEntityClass() {
        return SwivelBearingBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends SwivelBearingBlockEntity> getBlockEntityType() {
        return SimBlockEntityTypes.SWIVEL_BEARING.get();
    }

    @Override
    public IRotate getExtraKineticsRotationConfiguration() {
        return SwivelBearingBlockEntity.SwivelBearingCogwheelBlockEntity.EXTRA_COGWHEEL_CONFIG;
    }

    @Override
    protected VoxelShape getShape(final BlockState blockState, final BlockGetter blockGetter, final BlockPos blockPos, final CollisionContext collisionContext) {
        return blockState.getValue(ASSEMBLED) ? SimBlockShapes.SWIVEL_BEARING_ASSEMBLED.get(blockState.getValue(FACING)) : Shapes.block();
    }

    @Override
    public void beforeMove(final ServerLevel originLevel, final ServerLevel resultingLevel, final BlockState newState, final BlockPos oldPos, final BlockPos newPos) {
        this.withBlockEntityDo(originLevel, oldPos, SwivelBearingBlockEntity::beforeAssembly);
    }

    @Override
    public void afterMove(final ServerLevel originLevel, final ServerLevel resultingLevel, final BlockState newState, final BlockPos oldPos, final BlockPos newPos) {
        this.withBlockEntityDo(resultingLevel, newPos, SwivelBearingBlockEntity::associatePlateWithParent);
    }
}
