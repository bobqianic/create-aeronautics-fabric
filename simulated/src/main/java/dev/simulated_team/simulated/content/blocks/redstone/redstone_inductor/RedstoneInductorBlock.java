package dev.simulated_team.simulated.content.blocks.redstone.redstone_inductor;

import com.mojang.serialization.MapCodec;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.content.redstone.diodes.AbstractDiodeBlock;
import com.zurrtum.create.foundation.block.IBE;
import dev.simulated_team.simulated.index.SimBlockEntityTypes;
import dev.simulated_team.simulated.index.SimBlockShapes;
import dev.simulated_team.simulated.multiloader.CommonRedstoneBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DiodeBlock;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Vector3f;

public class RedstoneInductorBlock extends AbstractDiodeBlock implements IBE<RedstoneInductorBlockEntity>, CommonRedstoneBlock {
    public static final MapCodec<RedstoneInductorBlock> CODEC = simpleCodec(RedstoneInductorBlock::new);
    public static final BooleanProperty INVERTED = BooleanProperty.create("inverted");

    public RedstoneInductorBlock(final Properties builder) {
        super(builder);
        this.registerDefaultState(this.defaultBlockState()
                .setValue(INVERTED, false)
                .setValue(POWERED, false));
    }

    @Override
    protected MapCodec<? extends DiodeBlock> codec() {
        return CODEC;
    }

    @Override
    protected InteractionResult useItemOn(final ItemStack itemStack, final BlockState blockState, final Level level, final BlockPos blockPos, final Player player, final InteractionHand interactionHand, final BlockHitResult blockHitResult) {
        return this.toggle(level,blockPos, blockState, player, interactionHand);
    }

    public InteractionResult toggle(final Level pLevel, final BlockPos pPos, final BlockState pState, final Player player,
                                    final InteractionHand pHand) {
        if (!player.mayBuild())
            return InteractionResult.PASS;
        if (player.isShiftKeyDown())
            return InteractionResult.PASS;
        if (player.getItemInHand(pHand).is(AllItems.WRENCH.asItem()))
            return InteractionResult.PASS;

        if (pLevel.isClientSide()) {
            addParticles(pState, pLevel, pPos, 1f);
            return InteractionResult.SUCCESS;
        }

        pLevel.setBlock(pPos, pState.cycle(INVERTED), 3);

        return this.onBlockEntityUseItemOn(pLevel, pPos, be -> {
            final int backSignal = this.getBackSignal(pLevel, pPos, pState);

            be.updateSignal();

            final float f = !pState.getValue(INVERTED) ? 0.6F : 0.5F;
            pLevel.playSound(null, pPos, SoundEvents.LEVER_CLICK, SoundSource.BLOCKS, 0.3F, f);

            return InteractionResult.SUCCESS;
        });
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
    protected void createBlockStateDefinition(final StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(POWERED, INVERTED, FACING);
    }

    @Override
    public boolean commonConnectRedstone(final BlockState state, final BlockGetter world, final BlockPos pos, final Direction side) {
        if (side == null) return false;

        return side.getAxis() == state.getValue(FACING).getAxis();
    }

    @Override
    protected int getDelay(final BlockState pState) {
        return 0;
    }

    public int getBackSignal(final Level level, final BlockPos pos, final BlockState state){
        final Direction direction = state.getValue(FACING);
        final BlockPos blockpos = pos.relative(direction);
            return Math.max(level.getSignal(blockpos, direction), state.is(Blocks.REDSTONE_WIRE) ? state.getValue(RedStoneWireBlock.POWER) : 0);
    }

    @Override
    protected int getOutputSignal(final BlockGetter level, final BlockPos pos, final BlockState state) {
        final RedstoneInductorBlockEntity be = (RedstoneInductorBlockEntity) level.getBlockEntity(pos);
        assert be != null;

        final boolean inverted = state.getValue(INVERTED);

        return inverted ? 15 - be.outputSignal : be.outputSignal;
    }

    @Override
    public int getSignal(final BlockState state, final BlockGetter blockGetter, final BlockPos pos, final Direction side) {
            return state.getValue(FACING) == side ? this.getOutputSignal(blockGetter, pos, state) : 0;
    }

    @Override
    public VoxelShape getShape(final BlockState pState, final BlockGetter pLevel, final BlockPos pPos, final CollisionContext pContext) {
        return SimBlockShapes.REDSTONE_INDUCTOR.get(pState.getValue(FACING));
    }

    @Override
    public Class<RedstoneInductorBlockEntity> getBlockEntityClass() {
        return RedstoneInductorBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends RedstoneInductorBlockEntity> getBlockEntityType() {
        return SimBlockEntityTypes.REDSTONE_INDUCTOR.get();
    }
}
