package dev.simulated_team.simulated.content.blocks.merging_glue;

import com.mojang.serialization.MapCodec;
import com.zurrtum.create.foundation.block.IBE;
import dev.simulated_team.simulated.index.SimBlockEntityTypes;
import dev.simulated_team.simulated.index.SimBlockShapes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

public class MergingGlueBlock extends DirectionalBlock implements IBE<MergingGlueBlockEntity> {
    public static final MapCodec<MergingGlueBlock> CODEC = simpleCodec(MergingGlueBlock::new);

    public MergingGlueBlock(final Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected BlockState updateShape(final BlockState state, final LevelReader level,
                                     final ScheduledTickAccess scheduledTicks, final BlockPos currentPos,
                                     final Direction facing, final BlockPos facingPos,
                                     final BlockState facingState, final RandomSource random) {
        return getConnectedDirection(state) == facing && !state.canSurvive(level, currentPos) ? Blocks.AIR.defaultBlockState() : state;
    }

    protected static Direction getConnectedDirection(final BlockState state) {
        return state.getValue(FACING).getOpposite();
    }

    @Override
    protected boolean canSurvive(final BlockState state, final LevelReader level, final BlockPos pos) {
        return canAttach(level, pos, getConnectedDirection(state));
    }

    public static boolean canAttach(final LevelReader reader, final BlockPos pos, final Direction direction) {
        final BlockPos blockpos = pos.relative(direction);
        return reader.getBlockState(blockpos).isFaceSturdy(reader, blockpos, direction.getOpposite());
    }

    @Override
    protected VoxelShape getCollisionShape(final BlockState state, final BlockGetter level, final BlockPos pos, final CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    protected VoxelShape getShape(final BlockState state, final BlockGetter level, final BlockPos pos, final CollisionContext context) {
        return SimBlockShapes.MERGING_GlUE.get(state.getValue(FACING));
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
    protected void createBlockStateDefinition(final StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING);
    }

    @Override
    protected @NotNull MapCodec<? extends DirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    public Class<MergingGlueBlockEntity> getBlockEntityClass() {
        return MergingGlueBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends MergingGlueBlockEntity> getBlockEntityType() {
        return SimBlockEntityTypes.MERGING_GLUE.get();
    }
}
