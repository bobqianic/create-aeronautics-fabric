package dev.simulated_team.simulated.content.blocks.directional_gearshift;

import com.zurrtum.create.content.kinetics.RotationPropagator;
import com.zurrtum.create.content.kinetics.base.DirectionalAxisKineticBlock;
import com.zurrtum.create.content.kinetics.base.IRotate;
import com.zurrtum.create.content.kinetics.base.KineticBlockEntity;
import com.zurrtum.create.content.kinetics.base.RotatedPillarKineticBlock;
import com.zurrtum.create.content.kinetics.transmission.SplitShaftBlockEntity;
import com.zurrtum.create.foundation.block.IBE;
import dev.simulated_team.simulated.index.SimBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.ticks.TickPriority;
import org.jetbrains.annotations.Nullable;

public class DirectionalGearshiftBlock extends DirectionalAxisKineticBlock implements IBE<SplitShaftBlockEntity>, IRotate {
    // facing: direction of "forwards" darker powerable side
    // axis along first: which axis the shaft is on
    public static final BooleanProperty LEFT_POWERED = BooleanProperty.create("left_powered");
    public static final BooleanProperty RIGHT_POWERED = BooleanProperty.create("right_powered");

    public DirectionalGearshiftBlock(final Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState().setValue(LEFT_POWERED, false));
        this.registerDefaultState(this.defaultBlockState().setValue(RIGHT_POWERED, false));
    }

    @Override
    public BlockState updateAfterWrenched(final BlockState newState, final UseOnContext context) {
        return super.updateAfterWrenched(this.getPoweredState(context.getLevel(), newState, context.getClickedPos()), context);
    }

    @Override
    protected void createBlockStateDefinition(final StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder.add(LEFT_POWERED).add(RIGHT_POWERED));
    }

    @Override
    public @Nullable BlockState getStateForPlacement(final BlockPlaceContext pContext) {
        final Direction lookingDirection = pContext.getNearestLookingDirection();
        final boolean shiftKeyDown = pContext.getPlayer().isShiftKeyDown();

        final Direction.Axis preferredAxis = RotatedPillarKineticBlock.getPreferredAxis(pContext);

        Direction darkDirection;
        boolean axisAlongFirst = false;

        if (preferredAxis != null && preferredAxis != lookingDirection.getAxis() && !shiftKeyDown) {
            darkDirection = lookingDirection;
            if (preferredAxis == Direction.Axis.X) {
                axisAlongFirst = true;
            } else if (preferredAxis == Direction.Axis.Y && lookingDirection.getAxis() == Direction.Axis.X) {
                axisAlongFirst = true;
            }
        } else {
            if (lookingDirection.getAxis().isHorizontal()) {
                darkDirection = lookingDirection.getCounterClockWise();
                if (lookingDirection.getAxis() == Direction.Axis.X) {
                    axisAlongFirst = true;
                }
            } else {
                darkDirection = pContext.getHorizontalDirection().getCounterClockWise();
                if (pContext.getHorizontalDirection().getAxis() == Direction.Axis.Z) {
                    axisAlongFirst = true;
                }
            }
        }

        if (shiftKeyDown) {
            darkDirection = darkDirection.getOpposite();
        }

        final BlockState state = this.defaultBlockState()
                .setValue(FACING, darkDirection)
                .setValue(AXIS_ALONG_FIRST_COORDINATE, axisAlongFirst);

        return this.getPoweredState(pContext.getLevel(), state, pContext.getClickedPos());
    }

    @Override
    public void neighborChanged(final BlockState state, final Level level, final BlockPos pos, final Block blockIn,
                                final @Nullable Orientation wireOrientation, final boolean isMoving) {
        if (level.isClientSide())
            return;

        final boolean previouslyLeftPowered = state.getValue(LEFT_POWERED);
        final boolean previouslyRightPowered = state.getValue(RIGHT_POWERED);

        final BlockState newState = this.getPoweredState(level, state, pos);

        if (previouslyLeftPowered != newState.getValue(LEFT_POWERED) || previouslyRightPowered != newState.getValue(RIGHT_POWERED)) {
            this.detachKinetics(level, pos, true);
            level.setBlock(pos, newState, 2);
        }
    }

    public BlockState getPoweredState(final Level level, BlockState state, final BlockPos pos) {
        final Direction leftDirection = this.getLeftDirection(state);
        final Direction rightDirection = this.getRightDirection(state);

        final int leftSignal = level.getSignal(pos.offset(leftDirection.getUnitVec3i()), leftDirection);
        final int rightSignal = level.getSignal(pos.offset(rightDirection.getUnitVec3i()), rightDirection);

        final boolean previouslyLeftPowered = state.getValue(LEFT_POWERED);
        if (previouslyLeftPowered != (leftSignal > 0)) {
            state = state.cycle(LEFT_POWERED);
        }

        final boolean previouslyRightPowered = state.getValue(RIGHT_POWERED);
        if (previouslyRightPowered != (rightSignal > 0)) {
            state = state.cycle(RIGHT_POWERED);
        }

        return state;
    }

    public void detachKinetics(final Level worldIn, final BlockPos pos, final boolean reAttachNextTick) {
        final BlockEntity be = worldIn.getBlockEntity(pos);
        if (!(be instanceof KineticBlockEntity))
            return;

        RotationPropagator.handleRemoved(worldIn, pos, (KineticBlockEntity) be);
        if (reAttachNextTick) {
	        worldIn.scheduleTick(pos, this, 1, TickPriority.EXTREMELY_HIGH);
        }
    }

    public Direction getLeftDirection(final BlockState state) {
        return state.getValue(FACING);
    }

    public Direction getRightDirection(final BlockState state) {
        return this.getLeftDirection(state).getOpposite();
    }

    @Override
    public Class<SplitShaftBlockEntity> getBlockEntityClass() {
        return SplitShaftBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends SplitShaftBlockEntity> getBlockEntityType() {
        return SimBlockEntityTypes.DIRECTIONAL_GEARSHIFT.get();
    }

    @Override
    public void tick(final BlockState state, final ServerLevel worldIn, final BlockPos pos, final RandomSource random) {
        final BlockEntity be = worldIn.getBlockEntity(pos);
        if (!(be instanceof final KineticBlockEntity kte))
            return;
        RotationPropagator.handleAdded(worldIn, pos, kte);
    }

	@Override
	public InteractionResult onWrenched(final BlockState state, final UseOnContext context) {
		final InteractionResult interactionResult = super.onWrenched(state, context);
		if (interactionResult.consumesAction() && !context.getLevel().isClientSide()) { //make sure we detach when we rotate no matter what
			this.detachKinetics(context.getLevel(), context.getClickedPos(), true);
		}

		return interactionResult;
	}
}
