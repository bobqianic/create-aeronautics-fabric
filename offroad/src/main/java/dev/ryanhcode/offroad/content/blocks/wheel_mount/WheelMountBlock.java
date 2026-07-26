package dev.ryanhcode.offroad.content.blocks.wheel_mount;

import com.zurrtum.create.api.schematic.requirement.SpecialBlockItemRequirement;
import com.zurrtum.create.content.kinetics.base.HorizontalKineticBlock;
import com.zurrtum.create.content.schematics.requirement.ItemRequirement;
import com.zurrtum.create.foundation.block.IBE;
import dev.ryanhcode.offroad.content.components.TireLike;
import dev.ryanhcode.offroad.index.OffroadBlockEntityTypes;
import dev.ryanhcode.offroad.index.OffroadBlocks;
import dev.ryanhcode.offroad.index.OffroadDataComponents;
import dev.simulated_team.simulated.multiloader.inventory.ContainerSlot;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class WheelMountBlock extends HorizontalKineticBlock implements IBE<WheelMountBlockEntity>, SpecialBlockItemRequirement {

    public WheelMountBlock(final Properties properties) {
        super(properties);
    }

    @Override
    protected void createBlockStateDefinition(final StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
    }

    @Override
    protected void affectNeighborsAfterRemoval(final BlockState state, final net.minecraft.server.level.ServerLevel level, final BlockPos pos, final boolean isMoving) {
        if (state.hasBlockEntity()) {
            final WheelMountBlockEntity be = (WheelMountBlockEntity) level.getBlockEntity(pos);

            if (be != null && !be.getHeldItem().isEmpty()) {
                final Direction facing = state.getValue(WheelMountBlock.HORIZONTAL_FACING);

                BlockPos dropPos = pos;

                if (facing != null) {
                    dropPos = dropPos.relative(facing);
                }

                Containers.dropItemStack(level, dropPos.getX(), dropPos.getY(), dropPos.getZ(), be.getHeldItem());
            }

        }
        super.affectNeighborsAfterRemoval(state, level, pos, isMoving);
    }

    @Override
    public boolean hasShaftTowards(final LevelReader world, final BlockPos pos, final BlockState state, final Direction face) {
        return face == state.getValue(HORIZONTAL_FACING).getOpposite();
    }

    @Override
    public Direction.Axis getRotationAxis(final BlockState state) {
        return state.getValue(HORIZONTAL_FACING).getAxis();
    }

    @Override
    public BlockState getStateForPlacement(final BlockPlaceContext context) {
        final Direction preferred = this.getPreferredHorizontalFacing(context);
        final boolean crouching = context.getPlayer() != null && context.getPlayer()
                .isShiftKeyDown();
        if (preferred == null || crouching) {
            final Direction horizontalDirection = context.getHorizontalDirection();
            return this.defaultBlockState().setValue(HORIZONTAL_FACING, crouching ? horizontalDirection : horizontalDirection.getOpposite());
        }
        return this.defaultBlockState().setValue(HORIZONTAL_FACING, preferred.getOpposite());

    }

    @Override
    protected InteractionResult useItemOn(final ItemStack heldItem, final BlockState blockState, final Level level, final BlockPos blockPos, final Player player, final InteractionHand interactionHand, final BlockHitResult blockHitResult) {
        final Direction hitDirection = blockHitResult.getDirection();

        if (!hitDirection.equals(blockState.getValue(HORIZONTAL_FACING)) && hitDirection != Direction.DOWN) {
            return super.useItemOn(heldItem, blockState, level, blockPos, player, interactionHand, blockHitResult);
        }

        if (level.isClientSide()) {
            return this.onBlockEntityUseItemOn(level, blockPos, mount -> {
                final ItemStack potentialTire = mount.getHeldItem();
                if ((heldItem.isEmpty() && potentialTire.has(OffroadDataComponents.TIRE))
                        || (heldItem.has(OffroadDataComponents.TIRE) && potentialTire.has(OffroadDataComponents.TIRE))
                        || (heldItem.has(OffroadDataComponents.TIRE) && potentialTire.isEmpty())
                ) {
                    return InteractionResult.SUCCESS;
                }

                return super.useItemOn(heldItem, blockState, level, blockPos, player, interactionHand, blockHitResult);
            });
        }

        if (this.switchStacks(level, blockPos, player, interactionHand)) {
            return InteractionResult.CONSUME;
        }

        return super.useItemOn(heldItem, blockState, level, blockPos, player, interactionHand, blockHitResult);
    }

    private boolean switchStacks(final Level level, final BlockPos pos, final Player player, final InteractionHand hand) {
        final boolean[] passed = { false };

        final ItemStack heldItem = player.getItemInHand(hand);
        final TireLike tireLike = heldItem.get(OffroadDataComponents.TIRE);

        if (heldItem.isEmpty() || tireLike != null) {
            this.withBlockEntityDo(level, pos, mount -> {
                final ContainerSlot slot = mount.getInventory().slot;
                final ItemStack save = slot.getStack().copy();
                final ItemStack oldSlotItem = save.copy();

                slot.setStack(heldItem.copyWithCount(1));
                if (!player.hasInfiniteMaterials()) {
                    heldItem.shrink(1);
                }
                player.getInventory().placeItemBackInInventory(save);

                final ItemStack newSlotItem = slot.getStack();
                mount.setChanged();
                mount.sendData();
                passed[0] = true;

                final float pitch = 0.8f + level.random.nextFloat() * 0.4f;
                final float volume = .75f;

                if (oldSlotItem.isEmpty() && !newSlotItem.isEmpty()) {
                    // tire inserted
                    level.playSound(null, pos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.PLAYERS, volume, pitch);
                } else if (!oldSlotItem.isEmpty() && newSlotItem.isEmpty()) {
                    // tire picked up
                    level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, volume, pitch);
                } else if (!oldSlotItem.isEmpty()) {
                    // tire changed
                    level.playSound(null, pos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.PLAYERS, volume, pitch);
                }
            });

        }

        return passed[0];
    }

    @Override
    public VoxelShape getShape(final BlockState pState, final BlockGetter pLevel, final BlockPos pPos, final CollisionContext ctx) {
        return Shapes.block();
    }

    @Override
    public Class<WheelMountBlockEntity> getBlockEntityClass() {
        return WheelMountBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends WheelMountBlockEntity> getBlockEntityType() {
        return OffroadBlockEntityTypes.WHEEL_MOUNT.get();
    }

    @Override
    public ItemRequirement getRequiredItems(final BlockState state, @Nullable final BlockEntity blockEntity) {
        final ItemStack mountStack = OffroadBlocks.WHEEL_MOUNT.asStack();
        if (blockEntity instanceof final WheelMountBlockEntity wmbe) {
            final ItemStack heldItem = wmbe.getHeldItem();
            if (!heldItem.isEmpty()) {
                return new ItemRequirement(List.of(
                        new ItemRequirement.StackRequirement(mountStack, ItemRequirement.ItemUseType.CONSUME),
                        new ItemRequirement.StrictNbtStackRequirement(heldItem, ItemRequirement.ItemUseType.CONSUME)
                ));
            }
        }
        return new ItemRequirement(ItemRequirement.ItemUseType.CONSUME, mountStack);
    }
}
