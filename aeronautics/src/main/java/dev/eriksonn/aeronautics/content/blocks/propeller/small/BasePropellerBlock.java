package dev.eriksonn.aeronautics.content.blocks.propeller.small;

import com.zurrtum.create.content.equipment.wrench.IWrenchable;
import com.zurrtum.create.content.kinetics.base.DirectionalKineticBlock;
import com.zurrtum.create.foundation.block.IBE;
import dev.eriksonn.aeronautics.index.AeroBlockShapes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public abstract class BasePropellerBlock extends DirectionalKineticBlock implements IBE<BasePropellerBlockEntity> {

    public static final BooleanProperty REVERSED = BooleanProperty.create("reversed");

    public BasePropellerBlock(final Properties properties) {
        super(properties);
        this.registerDefaultState(this.getStateDefinition().any().setValue(REVERSED, false));
    }

    @Override
    protected void createBlockStateDefinition(final StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(REVERSED);
        super.createBlockStateDefinition(builder);
    }

    @Override
    public BlockState getStateForPlacement(final BlockPlaceContext context) {
        Direction preferredFacing = this.getPreferredFacing(context);
        if (preferredFacing == null)
            preferredFacing = context.getClickedFace().getOpposite();
        return this.defaultBlockState().setValue(FACING, context.getPlayer() != null && context.getPlayer()
                .isShiftKeyDown() ? preferredFacing : preferredFacing.getOpposite());
    }

    @Override
    public VoxelShape getShape(final BlockState pState, final BlockGetter pLevel, final BlockPos pPos, final CollisionContext pContext) {
        return AeroBlockShapes.PROPELLER.get(pState.getValue(FACING));
    }

    @Override
    public Direction.Axis getRotationAxis(final BlockState state) {
        return state.getValue(FACING).getAxis();
    }

    @Override
    public boolean hasShaftTowards(final LevelReader world, final BlockPos pos, final BlockState state, final Direction face) {
        return face == state.getValue(FACING)
                .getOpposite();
    }

    @Override
    public Class<BasePropellerBlockEntity> getBlockEntityClass() {
        return BasePropellerBlockEntity.class;
    }

    @Override
    public InteractionResult onWrenched(BlockState state, final UseOnContext context) {
        final Vec3 diff = context.getClickLocation().subtract(context.getClickedPos().getCenter());

        final Direction facing = state.getValue(FACING);
        final Vec3i normal = facing.getUnitVec3i();

        // dot product to make wrenching the propeller bit more forgiving
        if (context.getClickedFace() == facing || diff.dot(new Vec3(normal.getX(), normal.getY(), normal.getZ())) > 0.0) {
            state = state.cycle(REVERSED);
            context.getLevel().setBlock(context.getClickedPos(), state, 3);
            IWrenchable.playRotateSound(context.getLevel(), context.getClickedPos());
            return InteractionResult.SUCCESS;
        }

        return super.onWrenched(state, context);
    }
}
