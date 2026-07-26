package dev.simulated_team.simulated.content.blocks.lasers.laser_pointer;

import com.mojang.serialization.MapCodec;
import com.zurrtum.create.content.equipment.wrench.IWrenchable;
import com.zurrtum.create.content.kinetics.base.KineticBlockEntity;
import com.zurrtum.create.foundation.block.IBE;
import dev.simulated_team.simulated.data.advancements.SimAdvancements;
import dev.simulated_team.simulated.index.SimBlockEntityTypes;
import dev.simulated_team.simulated.index.SimBlockShapes;
import dev.simulated_team.simulated.index.SimTags;
import dev.simulated_team.simulated.util.SimColors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LaserPointerBlock extends DirectionalBlock implements IBE<LaserPointerBlockEntity>, IWrenchable {
    public static final MapCodec<LaserPointerBlock> CODEC = simpleCodec(LaserPointerBlock::new);

    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public static final BooleanProperty INVERTED = BlockStateProperties.INVERTED;

    public LaserPointerBlock(final Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState().setValue(POWERED, false).setValue(INVERTED, false));
    }

    @Override
    protected MapCodec<? extends DirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(final StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(POWERED)
                .add(INVERTED)
                .add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(final BlockPlaceContext context) {
        Direction nearestLookingDirection = context.getNearestLookingDirection();
        if (!context.getPlayer().isShiftKeyDown()) {
            nearestLookingDirection = nearestLookingDirection.getOpposite();
        }
        return super.getStateForPlacement(context)
                .setValue(FACING, nearestLookingDirection)
                .setValue(POWERED, context.getLevel().hasNeighborSignal(context.getClickedPos()))
                .setValue(INVERTED, false);
    }

    @Override
    public void neighborChanged(final BlockState state, final Level world, final BlockPos pos, final Block neighborBlock,
                                final @Nullable Orientation wireOrientation, final boolean moving) {
        if (world.isClientSide()) {
            return;
        }

        final boolean powered = world.hasNeighborSignal(pos);
        if (state.getValue(POWERED) != powered) {
            world.setBlock(pos, state.setValue(POWERED, powered), 7);
        }

        // Grant advancement
        if (powered) {
            SimAdvancements.BIG_BEAM.awardToNearby(pos, world);
        }
    }

    @Override
    public VoxelShape getShape(final BlockState pState, final BlockGetter pLevel, final BlockPos pPos, final CollisionContext pContext) {
        return SimBlockShapes.LASER_POINTER.get(pState.getValue(FACING));
    }

    @Override
    public @NotNull BlockState rotate(final BlockState state, final Rotation rot) {
        return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
    }

    @Override
    public @NotNull BlockState mirror(final BlockState state, final Mirror mirrorIn) {
        return state.rotate(mirrorIn.getRotation(state.getValue(FACING)));
    }

    @Override
    public Class<LaserPointerBlockEntity> getBlockEntityClass() {
        return LaserPointerBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends LaserPointerBlockEntity> getBlockEntityType() {
        return SimBlockEntityTypes.LASER_POINTER.get();
    }

    @Override
    protected InteractionResult useItemOn(final @NotNull ItemStack itemStack,
                                              final @NotNull BlockState blockState,
                                              final @NotNull Level level,
                                              final @NotNull BlockPos blockPos,
                                              final @NotNull Player player,
                                              final @NotNull InteractionHand interactionHand,
                                              final @NotNull BlockHitResult blockHitResult) {
        final LaserPointerBlockEntity be = (LaserPointerBlockEntity) level.getBlockEntity(blockPos);
        assert be != null;

        int newColor = -1;
        boolean newRainbow = be.isRainbow();
        if (itemStack.getItem() instanceof final DyeItem dyeItem) {
            final DyeColor gatheredColor = dyeItem.getDyeColor();
            newColor = gatheredColor.getTextColor();
        } else if (itemStack.is(SimTags.Items.LASER_POINTER_LENS)) {
            newColor = SimColors.MEDIA_OURPLE;
            newRainbow = false;
        } else if (itemStack.is(SimTags.Items.LASER_POINTER_RAINBOW)) {
            newRainbow = true;
        }

        final boolean updatedColor = newColor != -1 && be.laserColor != newColor;

        if (updatedColor || newRainbow != be.isRainbow()) {
            if (updatedColor) {
                be.setLaserColor(newColor);
            }
            be.setRainbow(newRainbow);
            level.playLocalSound(blockPos.getX(), blockPos.getY(), blockPos.getZ(), SoundEvents.DYE_USE, SoundSource.PLAYERS, 0.3f, 1.0f, false);
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    @Override
    public InteractionResult onWrenched(final BlockState state, final UseOnContext context) {
        if (context.getClickedFace().getAxis() == state.getValue(FACING).getAxis()) {
            KineticBlockEntity.switchToBlockState(context.getLevel(), context.getClickedPos(), state.cycle(INVERTED));
            return InteractionResult.SUCCESS;
        }
        return IWrenchable.super.onWrenched(state, context);
    }
}
