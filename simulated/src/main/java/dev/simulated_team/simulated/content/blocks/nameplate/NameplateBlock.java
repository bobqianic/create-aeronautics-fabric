package dev.simulated_team.simulated.content.blocks.nameplate;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.zurrtum.create.content.equipment.wrench.IWrenchable;
import com.zurrtum.create.foundation.block.IBE;
import com.tterrag.registrate.util.entry.BlockEntry;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.api.block.BlockSubLevelAssemblyListener;
import dev.ryanhcode.sable.sublevel.SubLevel;
import dev.simulated_team.simulated.index.SimBlockEntityTypes;
import dev.simulated_team.simulated.index.SimBlockShapes;
import dev.simulated_team.simulated.index.SimBlocks;
import com.zurrtum.create.catnip.placement.IPlacementHelper;
import com.zurrtum.create.catnip.placement.PlacementHelpers;
import com.zurrtum.create.catnip.placement.PlacementOffset;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SignApplicator;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;

public class NameplateBlock extends HorizontalDirectionalBlock implements IBE<NameplateBlockEntity>, IWrenchable, BlockSubLevelAssemblyListener {

    public static final EnumProperty<Position> POSITION = EnumProperty.create("position", Position.class);
    public static final MapCodec<NameplateBlock> CODEC = RecordCodecBuilder.mapCodec((instance) -> instance.group(propertiesCodec(), DyeColor.CODEC.fieldOf("DyeColor").forGetter(NameplateBlock::getColor)).apply(instance, NameplateBlock::new));

    private static final int placementHelperId = PlacementHelpers.register(new PlacementHelper());

    protected final DyeColor color;

    public NameplateBlock(final Properties properties, final DyeColor color) {
        super(properties);
        this.color = color;
    }

    @Override
    protected void createBlockStateDefinition(final StateDefinition.Builder<Block, BlockState> pBuilder) {
        super.createBlockStateDefinition(pBuilder);
        pBuilder.add(POSITION);
        pBuilder.add(FACING);
    }

    public static boolean hasBackSupport(final Direction facingDir, final LevelReader level, final BlockPos pos) {
        return !level.getBlockState(pos.relative(facingDir, -1)).isAir();
    }

    // survival check on block update is not the same
    @Override
    public boolean canSurvive(final BlockState pState, final LevelReader pLevel, final BlockPos pPos) {
        final Direction facing = pState.getValue(FACING);

        if (hasBackSupport(facing, pLevel, pPos)) {
            return true;
        }

        final BlockState leftState = pLevel.getBlockState(pPos.relative(facing.getClockWise()));
        if (leftState.getBlock().equals(pState.getBlock()) && leftState.getValue(FACING) == facing) {
            return true;
        }

        final BlockState rightState = pLevel.getBlockState(pPos.relative(facing.getCounterClockWise()));
        if (rightState.getBlock().equals(pState.getBlock()) && rightState.getValue(FACING) == facing) {
            return true;
        }

        return false;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(final BlockPlaceContext pContext) {
        final BlockState state = super.getStateForPlacement(pContext);

        Direction direction = pContext.getClickedFace();

        if (pContext.getClickedFace().getAxis().equals(Direction.Axis.Y)) {
            direction = pContext.getHorizontalDirection().getOpposite();
        }

        final BlockPos pos = pContext.getClickedPos();
        final Level level = pContext.getLevel();
        final Position position = this.getPositionState(level, pos, direction);

        return state.setValue(POSITION, position).setValue(FACING, direction);
    }

    @Override
    public VoxelShape getShape(final BlockState pState, final BlockGetter pLevel, final BlockPos pPos, final CollisionContext pContext) {
        return SimBlockShapes.NAMEPLATE.get(pState.getValue(FACING));
    }

    public Position getPositionState(final LevelReader level, final BlockPos pos, final Direction facing) {
        Position outPos = Position.SINGLE;

        final BlockState leftState = level.getBlockState(pos.offset(facing.getClockWise(Direction.Axis.Y).getUnitVec3i()));
        final BlockState rightState = level.getBlockState(pos.offset(facing.getCounterClockWise(Direction.Axis.Y).getUnitVec3i()));

        final boolean left = leftState.getBlock() instanceof final NameplateBlock npb && npb.getColor() == this.getColor();
        final boolean right = rightState.getBlock() instanceof final NameplateBlock npb && npb.getColor() == this.getColor();
        if (left) {
            final boolean leftBlock = leftState.getValue(FACING).equals(facing);
            if (leftBlock) outPos = Position.RIGHT;
        }
        if (right) {
            final boolean rightBlock = rightState.getValue(FACING).equals(facing);
            if (rightBlock) outPos = Position.LEFT;
        }
        if (left && right) {
            final boolean rightBlock = rightState.getValue(FACING).equals(facing);
            final boolean leftBlock = leftState.getValue(FACING).equals(facing);
            if (leftBlock && rightBlock) outPos = Position.MIDDLE;
        }

        return outPos;
    }

    @Override
    protected InteractionResult useItemOn(final ItemStack itemStack, final BlockState blockState, final Level level, final BlockPos blockPos, final Player player, final InteractionHand interactionHand, final BlockHitResult blockHitResult) {
        if (!player.isShiftKeyDown() && player.mayBuild()) {
            final IPlacementHelper placementHelper = PlacementHelpers.get(placementHelperId);
            if (itemStack.getItem() instanceof final BlockItem bi && blockState.is(bi.getBlock()) && placementHelper.matchesItem(itemStack)) {
                final InteractionResult result = placementHelper.getOffset(player, level, blockState, blockPos, blockHitResult)
                        .placeInWorld(level, (BlockItem) itemStack.getItem(), player, interactionHand);
                if (result == InteractionResult.SUCCESS) {
                    return InteractionResult.SUCCESS;
                }
            }
        }

        if (player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }

        final ItemStack heldItem = player.getItemInHand(interactionHand);

        if(heldItem.getItem() instanceof final SignApplicator signApplicator) {
            final MutableBoolean success = new MutableBoolean(false);
            this.withBlockEntityDo(level, blockPos, nbe -> {
                final NameplateBlockEntity controller = nbe.findController();
                if (controller.allowsEditing()) {

                    final SignBlockEntity dummySign = new SignBlockEntity(blockPos, Blocks.OAK_SIGN.defaultBlockState());
                    dummySign.setLevel(controller.getLevel());
                    SignText text = dummySign.getFrontText()
                            .setMessage(0, Component.literal(controller.getName()))
                            .setColor(controller.getTextColor())
                            .setHasGlowingText(controller.glowing);
                    dummySign.setText(text, true);
                    dummySign.setWaxed(controller.waxed);

                    if (signApplicator.canApplyToSign(text, player) && signApplicator.tryApplyToSign(controller.getLevel(), dummySign, true, player)) {
                        text = dummySign.getFrontText();
                        controller.setTextColor(text.getColor(), true);
                        controller.glowing = text.hasGlowingText();
                        controller.waxed = dummySign.isWaxed();
                        controller.updateNameplates(this.getColor(), blockState.getValue(FACING));
                        success.setTrue();
                    }
                    nbe.sendData();
                }
            });
            return success.booleanValue() ? InteractionResult.SUCCESS : InteractionResult.PASS;
        }

        if (level.isClientSide()) {
            this.withBlockEntityDo(level, blockPos, nbe -> {
                final NameplateBlockEntity controller = nbe.findController();
                if(!controller.waxed) {
                    NameplateScreen.setScreen(nbe);
                } else {
                    level.playSound(player, blockPos, SoundEvents.WAXED_SIGN_INTERACT_FAIL, SoundSource.BLOCKS);
                }
            });
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public void neighborChanged(final BlockState state, final Level level, final BlockPos selfPos,
                                final Block neighborBlock, final @Nullable Orientation wireOrientation,
                                final boolean movedByPiston) {
        if (level.isClientSide()) {
            return;
        }

        if (level.getBlockEntity(selfPos) instanceof NameplateBlockEntity nbe) {
            final Direction facing = state.getValue(FACING);
            final NameplateBlockEntity previousController = nbe.findController();
            nbe.checkAndUpdateController(this.color, facing);
            if (previousController != nbe) {
                previousController.checkAndUpdateController(this.color, facing);
            }

            if (!NameplateBlockEntity.hasSupport(nbe)) {
                level.destroyBlock(selfPos, true);
            }
        }
    }

    @Override
    public BlockState updateShape(final BlockState state, final LevelReader level,
                                  final ScheduledTickAccess scheduledTicks, final BlockPos pos,
                                  final Direction direction, final BlockPos neighborPos,
                                  final BlockState neighborState, final RandomSource random) {
        final Position posState = this.getPositionState(level, pos, state.getValue(FACING));
        return state.setValue(POSITION, posState);
    }

    @Override
    public Class<NameplateBlockEntity> getBlockEntityClass() {
        return NameplateBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends NameplateBlockEntity> getBlockEntityType() {
        return SimBlockEntityTypes.NAMEPLATE.get();
    }

    public DyeColor getColor() {
        return this.color;
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    public void afterMove(final ServerLevel serverLevel, final ServerLevel resultingLevel, final BlockState blockState, final BlockPos oldPos, final BlockPos newPos) {
        final SubLevel subLevel = Sable.HELPER.getContaining(resultingLevel, newPos);
        final NameplateBlockEntity nameplate = this.getBlockEntity(resultingLevel, newPos);

        if (nameplate != null && nameplate.getName() != null && subLevel != null && subLevel.getName() == null) {
            subLevel.setName(nameplate.getName());
        }
    }

    public enum Position implements StringRepresentable {
        SINGLE,
        LEFT,
        RIGHT,
        MIDDLE;

        @Override
        public @NotNull String getSerializedName() {
            return this.name().toLowerCase(Locale.ROOT);
        }
    }

    private static class PlacementHelper implements IPlacementHelper {
        @Override
        public Predicate<ItemStack> getItemPredicate() {
            return stack -> {
                for (final NameplateBlock nameplate : SimBlocks.NAMEPLATES) {
                    if (Block.byItem(stack.getItem()) == nameplate) {
                        return true;
                    }
                }
                return false;
            };
        }

        @Override
        public Predicate<BlockState> getStatePredicate() {
            return state -> {
                for (final NameplateBlock nameplate : SimBlocks.NAMEPLATES) {
                    if (state.is(nameplate)) {
                        return true;
                    }
                }
                return false;
            };
        }

        @Override
        public PlacementOffset getOffset(final Player player, final Level world, final BlockState state, final BlockPos pos, final BlockHitResult ray, final ItemStack heldItem) {
            if (heldItem.getItem() instanceof final BlockItem bi) {
                if (state.is(bi.getBlock())) {
                    return IPlacementHelper.super.getOffset(player, world, state, pos, ray, heldItem);
                }
            }
            return PlacementOffset.fail();
        }

        @Override
        public PlacementOffset getOffset(final Player player, final Level level, final BlockState blockState, final BlockPos blockPos, final BlockHitResult blockHitResult) {
            final List<Direction> directions = IPlacementHelper.orderedByDistance(blockPos, blockHitResult.getLocation(), dir -> {
                if (dir.getAxis() != blockState.getValue(FACING).getClockWise().getAxis()) {
                    return false;
                }
                final BlockPos relPos = blockPos.relative(dir);
                return level.getBlockState(relPos).canBeReplaced() && blockState.canSurvive(level, relPos);
            });
            if (directions.isEmpty())
                return PlacementOffset.fail();
            else {
                return PlacementOffset.success(blockPos.relative(directions.getFirst()),
                        s -> s.setValue(FACING, blockState.getValue(FACING)));
            }
        }
    }
}
