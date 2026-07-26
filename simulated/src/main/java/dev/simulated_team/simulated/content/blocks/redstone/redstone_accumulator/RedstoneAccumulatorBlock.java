package dev.simulated_team.simulated.content.blocks.redstone.redstone_accumulator;

import com.mojang.serialization.MapCodec;
import com.zurrtum.create.content.redstone.diodes.AbstractDiodeBlock;
import com.zurrtum.create.foundation.block.IBE;
import dev.simulated_team.simulated.index.SimBlockEntityTypes;
import dev.simulated_team.simulated.index.SimBlockShapes;
import dev.simulated_team.simulated.multiloader.CommonRedstoneBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DiodeBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

public class RedstoneAccumulatorBlock extends AbstractDiodeBlock implements IBE<RedstoneAccumulatorBlockEntity>, CommonRedstoneBlock {
    public static final MapCodec<RedstoneAccumulatorBlock> CODEC = simpleCodec(RedstoneAccumulatorBlock::new);
    public static BooleanProperty POWERING = BooleanProperty.create("powering");
    public static BooleanProperty SIDE_POWERED = BooleanProperty.create("side_powered");
    public static BooleanProperty INVERTED = BlockStateProperties.INVERTED;

    public RedstoneAccumulatorBlock(final Properties builder) {
        super(builder);
        this.registerDefaultState(this.defaultBlockState()
                .setValue(POWERED, false)
                .setValue(SIDE_POWERED, false)
                .setValue(POWERING, false)
                .setValue(INVERTED, false)
        );
    }

    @Override
    protected MapCodec<? extends DiodeBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(final StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, POWERED, SIDE_POWERED, POWERING, INVERTED);
    }

    @Override
    protected void checkTickOnNeighbor(final Level level, final BlockPos pos, final BlockState state) {
        super.checkTickOnNeighbor(level, pos, state);
        level.setBlock(pos, this.getUpdatedBlockstate(pos, state, level), 2);
    }

    @Override
    public void tick(final BlockState state, final ServerLevel level, final BlockPos pos, final RandomSource random) {}

    @Override
    public Class<RedstoneAccumulatorBlockEntity> getBlockEntityClass() {
        return RedstoneAccumulatorBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends RedstoneAccumulatorBlockEntity> getBlockEntityType() {
        return SimBlockEntityTypes.REDSTONE_ACCUMULATOR.get();
    }

    public BlockState getUpdatedBlockstate(final BlockPos pos, final BlockState state, final Level level) {
        final Direction facing = state.getValue(RedstoneAccumulatorBlock.FACING).getOpposite();
        final BlockPos offset = pos.relative(facing.getOpposite());

        final BlockPos leftSide = pos.offset(facing.getCounterClockWise().getUnitVec3i());
        final BlockPos rightSide = pos.offset(facing.getClockWise().getUnitVec3i());

        final boolean leftSignal = level.getSignal(leftSide, facing.getClockWise().getOpposite()) > 0;
        final boolean rightSignal = level.getSignal(rightSide, facing.getCounterClockWise().getOpposite()) > 0;

        final boolean backSignal = level.getSignal(offset, facing.getOpposite()) > 0;
        final boolean sideSignal = leftSignal || rightSignal;
        final boolean frontSignal = this.getOutputSignal(level, pos, state) > 0;

        return state
                .setValue(SIDE_POWERED, sideSignal)
                .setValue(POWERED, backSignal)
                .setValue(POWERING, frontSignal);
    }

    @Override
    public int getSignal(final BlockState state, final BlockGetter level, final BlockPos pos, final Direction dir) {
        return  state.getValue(FACING) == dir ? this.getOutputSignal(level, pos, state) : 0;
    }


    @Override
    protected InteractionResult useItemOn(final ItemStack itemStack, final BlockState blockState, final Level level, final BlockPos blockPos, final Player player, final InteractionHand interactionHand, final BlockHitResult blockHitResult) {
        level.setBlock(blockPos, this.getUpdatedBlockstate(blockPos, blockState.cycle(INVERTED), level), 2);
        level.updateNeighborsAt(blockPos, blockState.getBlock());

        final float f = !blockState.getValue(INVERTED) ? 0.6F : 0.5F;
        level.playSound(null, blockPos, SoundEvents.LEVER_CLICK, SoundSource.BLOCKS, 0.3F, f);

        return InteractionResult.SUCCESS;
    }

    @Override
    public boolean commonConnectRedstone(final BlockState state, final BlockGetter level, final BlockPos pos, @Nullable final Direction direction) {
        return direction != null;
    }

    protected int getOutputSignal(final BlockGetter pLevel, final BlockPos pPos, final BlockState pState) {
        final RedstoneAccumulatorBlockEntity be = (RedstoneAccumulatorBlockEntity) pLevel.getBlockEntity(pPos);
        if(be != null) {
            return pState.getValue(INVERTED) ? 15 - be.outputSignal : be.outputSignal;
        }
        return 0;
    }

    @Override
    protected int getDelay(final BlockState state) {
        return 0;
    }

    @Override
    public void animateTick(final BlockState pState, final Level pLevel, final BlockPos pPos, final RandomSource pRandom) {
        this.withBlockEntityDo(pLevel, pPos, be -> {
            if(pState.getValue(POWERED) || be.outputSignal > 0)
                if(pRandom.nextFloat() < 0.25f)
                    addParticles(pState, pLevel, pPos, 1f);
        } );
    }

    private static void addParticles(final BlockState state, final LevelAccessor level, final BlockPos pos, final float alpha) {
        level.addParticle(new DustParticleOptions(0xFF0000, alpha), pos.getX() + 0.5f, pos.getY() + 0.5f, pos.getZ() + 0.5f, 0.0D, 0.0D,
                0.0D);
    }

    @Override
    public VoxelShape getShape(final BlockState pState, final BlockGetter pLevel, final BlockPos pPos, final CollisionContext pContext) {
        return SimBlockShapes.REDSTONE_ACCUMULATOR;
    }
}
