package dev.simulated_team.simulated.content.blocks.handle;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.AllTags;
import com.zurrtum.create.api.contraption.BlockMovementChecks;
import com.zurrtum.create.content.equipment.wrench.IWrenchable;
import com.zurrtum.create.foundation.block.IBE;
import com.zurrtum.create.impl.contraption.BlockMovementChecksImpl;
import dev.simulated_team.simulated.content.blocks.util.AbstractDirectionalAxisBlock;
import dev.simulated_team.simulated.index.SimBlockEntityTypes;
import dev.simulated_team.simulated.index.SimClickInteractions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

import static net.minecraft.core.Direction.Axis.Y;

public class HandleBlock extends AbstractDirectionalAxisBlock implements IBE<HandleBlockEntity>, IWrenchable {

    public static final MapCodec<HandleBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            propertiesCodec(),
            DyeColor.CODEC.fieldOf("color").forGetter(HandleBlock::getColor),
            StringRepresentable.fromValues(Variant::values).fieldOf("variant").forGetter(HandleBlock::getVariant)
    ).apply(instance, HandleBlock::new));

    private static final HandleShaper SHAPER = HandleShaper.make();

    static {
        BlockMovementChecksImpl.registerAttachedCheck(
                (state, world, pos, direction) -> {
                    final BlockState relativeState = world.getBlockState(pos.relative(direction));
                    if (state.getBlock() instanceof HandleBlock && state.getValue(HandleBlock.FACING) == direction.getOpposite()) {
                        return BlockMovementChecks.CheckResult.SUCCESS;
                    }
                    if (relativeState.getBlock() instanceof HandleBlock && relativeState.getValue(HandleBlock.FACING) == direction) {
                        return BlockMovementChecks.CheckResult.SUCCESS;
                    }

                    return BlockMovementChecks.CheckResult.PASS;
                }
        );
    }

    private final @Nullable DyeColor color;
    private final Variant variant;

    public HandleBlock(final Properties properties, @Nullable final DyeColor dyeColor, final Variant variant) {
        super(properties);
        this.color = dyeColor;
        this.variant = variant;
    }

    public static boolean canInteractWithHandle(final Player player) {
        final ItemStack mainHandItem = player.getMainHandItem();
        return mainHandItem.isEmpty() || mainHandItem.is(AllItems.EXTENDO_GRIP);
    }

    @Override
    protected InteractionResult useItemOn(final ItemStack itemStack, final BlockState blockState, final Level level, final BlockPos blockPos, final Player player, final InteractionHand interactionHand, final BlockHitResult blockHitResult) {
        if (itemStack.is(AllItems.WRENCH.asItem()))
            return InteractionResult.PASS;

        if (canInteractWithHandle(player)) {
            if (level.isClientSide() && player.isLocalPlayer()) {
                SimClickInteractions.HANDLE_HANDLER.startHold(level, player, blockPos);
            }
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    @Override
    public boolean canSurvive(final BlockState state, final LevelReader worldIn, final BlockPos pos) {
        final Direction facing = state.getValue(FACING)
                .getOpposite();
        final BlockPos neighbourPos = pos.relative(facing);
        final BlockState neighbour = worldIn.getBlockState(neighbourPos);
        return !neighbour.getCollisionShape(worldIn, neighbourPos)
                .isEmpty();
    }

    @Override
    public void neighborChanged(final BlockState state, final Level worldIn, final BlockPos pos, final Block blockIn,
                                final @Nullable Orientation wireOrientation, final boolean isMoving) {
        if (worldIn.isClientSide())
            return;

        if (!this.canSurvive(state, worldIn, pos)) {
            worldIn.destroyBlock(pos, true);
        }
    }

    @Override
    public VoxelShape getShape(final BlockState state, final BlockGetter pLevel, final BlockPos pPos, final CollisionContext pContext) {
        return SHAPER.get(state.getValue(FACING),  state.getValue(AXIS_ALONG_FIRST_COORDINATE));
    }

    @Override
    protected boolean hasAnalogOutputSignal(final BlockState state) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(final BlockState state, final Level level, final BlockPos pos, final Direction side) {
        if (level.getBlockEntity(pos) instanceof final HandleBlockEntity be) {
            return be.hasPlayer() ? 15 : 0;
        }
        return 0;
    }

    @Override
    public BlockEntityType<? extends HandleBlockEntity> getBlockEntityType() {
        return SimBlockEntityTypes.HANDLE.get();
    }
    @Override
    public Class<HandleBlockEntity> getBlockEntityClass() {
        return HandleBlockEntity.class;
    }

    @Override
    protected MapCodec<? extends DirectionalBlock> codec() {
        return CODEC;
    }

    public @Nullable DyeColor getColor() {
        return this.color;
    }

    public Variant getVariant() {
        return this.variant;
    }

    public static boolean isHorizontal(final BlockState state) {
        final Direction.Axis axis = state.getValue(FACING).getAxis();
        return axis != Y && (state.getValue(AXIS_ALONG_FIRST_COORDINATE) ^ axis == Direction.Axis.X);
    }


    public enum Variant implements StringRepresentable {
        IRON(Ingredient.of(BuiltInRegistries.acquireBootstrapRegistrationLookup(BuiltInRegistries.ITEM)
                .getOrThrow(AllTags.commonItemTag("nuggets/iron")))),
        COPPER(Ingredient.of(BuiltInRegistries.acquireBootstrapRegistrationLookup(BuiltInRegistries.ITEM)
                .getOrThrow(AllTags.commonItemTag("nuggets/copper")))),
        DYED(null);

        @Nullable final Ingredient ingredient;

        Variant(@Nullable final Ingredient ingredient) {
            this.ingredient = ingredient;
        }

        @Override
        public String getSerializedName() {
            return this.name().toLowerCase(Locale.ROOT);
        }

        public @Nullable Ingredient getIngredient() {
            return this.ingredient;
        }
    }
}
