package dev.simulated_team.simulated.content.blocks.physics_assembler;

import com.mojang.serialization.MapCodec;
import com.zurrtum.create.content.equipment.wrench.IWrenchable;
import com.zurrtum.create.content.kinetics.deployer.DeployerFakePlayer;
import com.zurrtum.create.foundation.block.IBE;
import dev.ryanhcode.sable.api.block.BlockSubLevelAssemblyListener;
import dev.simulated_team.simulated.index.SimBlockEntityTypes;
import dev.simulated_team.simulated.index.SimBlockShapes;
import dev.simulated_team.simulated.index.SimClickInteractions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FaceAttachedHorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

public class PhysicsAssemblerBlock extends FaceAttachedHorizontalDirectionalBlock implements IBE<PhysicsAssemblerBlockEntity>, IWrenchable, BlockSubLevelAssemblyListener {
    public static final MapCodec<PhysicsAssemblerBlock> CODEC = simpleCodec(PhysicsAssemblerBlock::new);

    public PhysicsAssemblerBlock(final Properties properties) {
        super(properties);
    }

    @Override
    protected boolean canSurvive(final BlockState state, final LevelReader level, final BlockPos pos) {
        return canAttach(level, pos, getConnectedDirection(state).getOpposite());
    }

    public static boolean canAttach(final LevelReader reader, final BlockPos pos, final Direction direction) {
        final BlockPos blockpos = pos.relative(direction);
        return !reader.getBlockState(blockpos).getBlockSupportShape(reader, pos).getFaceShape(direction.getOpposite()).isEmpty();
    }

    @Override
    protected MapCodec<? extends FaceAttachedHorizontalDirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    public Class<PhysicsAssemblerBlockEntity> getBlockEntityClass() {
        return PhysicsAssemblerBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends PhysicsAssemblerBlockEntity> getBlockEntityType() {
        return SimBlockEntityTypes.PHYSICS_ASSEMBLER.get();
    }

    @Override
    public @NotNull VoxelShape getShape(final BlockState state,
                                        final @NotNull BlockGetter pLevel,
                                        final @NotNull BlockPos pPos,
                                        final @NotNull CollisionContext pContext) {
        final Direction facing = state.getValue(FACING);
        return switch (state.getValue(FACE)) {
            case CEILING -> SimBlockShapes.PHYSICS_ASSEMBLER_CEILING_OUTLINE.get(facing);
            case FLOOR -> SimBlockShapes.PHYSICS_ASSEMBLER_OUTLINE.get(facing);
            default -> SimBlockShapes.PHYSICS_ASSEMBLER_WALL_OUTLINE.get(facing.getOpposite());
        };
    }

    @Override
    protected VoxelShape getCollisionShape(final BlockState state, final BlockGetter level, final BlockPos pos, final CollisionContext context) {
        final Direction facing = state.getValue(FACING);
        return switch (state.getValue(FACE)) {
            case CEILING -> SimBlockShapes.PHYSICS_ASSEMBLER_CEILING_COLLISION.get(facing);
            case FLOOR -> SimBlockShapes.PHYSICS_ASSEMBLER_COLLISION.get(facing);
            default -> SimBlockShapes.PHYSICS_ASSEMBLER_WALL_COLLISION.get(facing.getOpposite());
        };
    }

    @Override
    protected InteractionResult useWithoutItem(final BlockState state, final Level level, final BlockPos pos, final Player player, final BlockHitResult hitResult) {
        // Deployer interaction
        if (player instanceof DeployerFakePlayer) {
            if (!level.isClientSide()) {
                this.withBlockEntityDo(level, pos, PhysicsAssemblerBlockEntity::assembleOrDisassemble);
            }

            return InteractionResult.SUCCESS;
        }

        // Start holding
        if (level.isClientSide() && player.isLocalPlayer()) {
            return this.onBlockEntityUse(level, pos, be -> {
                SimClickInteractions.PHYSICS_ASSEMBLER_MANAGER.startHold(level, player, pos);
                return InteractionResult.SUCCESS;
            });
        }

        return InteractionResult.CONSUME;
    }

    public static Direction getStickyFacing(final BlockState state) {
        return switch (state.getValue(FACE)) {
            case FLOOR -> Direction.DOWN;
            case CEILING -> Direction.UP;
            case WALL -> state.getValue(FACING).getOpposite();
        };
    }

    @Override
    protected void createBlockStateDefinition(final StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder.add(FACING, FACE));
    }

    @Override
    public void afterMove(final ServerLevel serverLevel, final ServerLevel serverLevel1, final BlockState blockState, final BlockPos blockPos, final BlockPos blockPos1) {
        final BlockEntity be = serverLevel1.getBlockEntity(blockPos1);
        if (be instanceof final PhysicsAssemblerBlockEntity pabe) {
            pabe.setParent(serverLevel1);
        }
    }
}
