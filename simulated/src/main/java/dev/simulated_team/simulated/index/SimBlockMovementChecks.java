package dev.simulated_team.simulated.index;

import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.api.contraption.BlockMovementChecks;
import com.zurrtum.create.content.contraptions.bearing.BearingBlock;
import com.zurrtum.create.content.contraptions.bearing.SailBlock;
import com.zurrtum.create.content.contraptions.bearing.WindmillBearingBlock;
import com.zurrtum.create.content.contraptions.bearing.WindmillBearingBlockEntity;
import com.zurrtum.create.content.contraptions.chassis.StickerBlock;
import com.zurrtum.create.content.contraptions.gantry.GantryCarriageBlock;
import com.zurrtum.create.content.contraptions.piston.MechanicalPistonBlock;
import com.zurrtum.create.content.contraptions.piston.MechanicalPistonHeadBlock;
import com.zurrtum.create.content.contraptions.piston.PistonExtensionPoleBlock;
import com.zurrtum.create.content.contraptions.pulley.PulleyBlock;
import com.zurrtum.create.content.kinetics.base.IRotate;
import com.zurrtum.create.content.kinetics.belt.BeltBlock;
import com.zurrtum.create.content.kinetics.gantry.GantryShaftBlock;
import com.zurrtum.create.infrastructure.config.AllConfigs;
import dev.simulated_team.simulated.content.blocks.spring.SpringBlock;
import dev.simulated_team.simulated.content.blocks.symmetric_sail.SymmetricSailBlock;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import com.zurrtum.create.catnip.data.Iterate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.PistonType;
import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import static com.zurrtum.create.content.contraptions.piston.MechanicalPistonBlock.isExtensionPole;
import static com.zurrtum.create.content.contraptions.piston.MechanicalPistonBlock.isPistonHead;

/**
 * Provides several interfaces that can define the behavior of blocks when mounting onto simulated contraptions:
 * <ul>
 *     <li>{@link SimBlockMovementChecks.AttachedCheck}</li>
 * </ul>
 * See each one for details.
 * <p>
 * For each interface, checks can be registered and queried.
 * Registration is thread-safe and can be done in parallel mod init.
 * Each query will iterate all registered checks of that type in reverse-registration order. If a check returns
 * a non-{@link BlockMovementChecks.CheckResult#PASS PASS} result, that is the result of the query. If no check catches a query, then
 * a best-effort fallback is used.
 */
public class SimBlockMovementChecks {
    private static final List<BlockPos> TEMP_DEFAULT_POSITIONS = new ArrayList<>();

    private static final ObjectList<AdditionalBlocks> ADDITIONAL_BLOCK_REGISTRATIONS = new ObjectArrayList<>();

    private static final ObjectList<AttachedCheck> ATTACHED_CHECKS = new ObjectArrayList<>();

    private static BlockMovementChecks.CheckResult registerDefaultBlockAttachedTowards(final BlockState state, final Level world, final BlockPos pos, final Direction direction) {
        final Block block = state.getBlock();
        final BlockState relativeState = world.getBlockState(pos.relative(direction));
        final Block relativeBlock = relativeState.getBlock();

        //cool new java switch statements
        return switch (block) {
            case final SymmetricSailBlock ignored when relativeBlock instanceof SailBlock ->
                    BlockMovementChecks.CheckResult.FAIL;

            case final SailBlock ignored when relativeBlock instanceof SymmetricSailBlock ->
                    BlockMovementChecks.CheckResult.FAIL;

            case final SymmetricSailBlock ignored ->
                    direction.getAxis() == state.getValue(SymmetricSailBlock.AXIS) ? BlockMovementChecks.CheckResult.FAIL : BlockMovementChecks.CheckResult.SUCCESS;

            case final SpringBlock ignored ->
                    direction.getOpposite() == state.getValue(SpringBlock.FACING) ? BlockMovementChecks.CheckResult.SUCCESS : BlockMovementChecks.CheckResult.FAIL;

            default -> BlockMovementChecks.CheckResult.PASS;
        };
    }

    private synchronized static Iterable<BlockPos> registerDefaultAdditionalBlocks(final BlockState state, final Level level, final BlockPos pos, final Set<BlockPos> visited) {
        TEMP_DEFAULT_POSITIONS.clear();

        final Block block = state.getBlock();
        switch (block) {
            case final BeltBlock ignored -> {
                final BlockPos nextPos = BeltBlock.nextSegmentPosition(state, pos, true);
                if (nextPos != null && !visited.contains(nextPos)) {
                    TEMP_DEFAULT_POSITIONS.add(nextPos);
                }

                final BlockPos prevPos = BeltBlock.nextSegmentPosition(state, pos, false);
                if (prevPos != null && !visited.contains(prevPos)) {
                    TEMP_DEFAULT_POSITIONS.add(prevPos);
                }
            }

            case final PulleyBlock ignored -> {
                int limit = AllConfigs.server().kinetics.maxRopeLength.get();
                BlockPos ropePos = pos;
                while (limit-- >= 0) {
                    ropePos = ropePos.below();
                    if (!level.isLoaded(ropePos)) {
                        break;
                    }

                    final BlockState ropeState = level.getBlockState(ropePos);
                    final Block ropeBlock = ropeState.getBlock();
                    if (!(ropeBlock instanceof PulleyBlock.RopeBlock) && !(ropeBlock instanceof PulleyBlock.MagnetBlock)) {
                        if (!visited.contains(ropePos)) {
                            TEMP_DEFAULT_POSITIONS.add(ropePos);
                        }

                        break;
                    }

                    if (!visited.contains(ropePos)) {
                        TEMP_DEFAULT_POSITIONS.add(ropePos);
                    }
                }
            }

            //make sure windmills specifically are disassembled before moving
            case final WindmillBearingBlock ignored -> {
                if (level.getBlockEntity(pos) instanceof final WindmillBearingBlockEntity wwbe) {
                    wwbe.disassembleForMovement();
                }

                final BlockPos relative = pos.relative(state.getValue(BearingBlock.FACING));
                if (!visited.contains(relative)) {
                    TEMP_DEFAULT_POSITIONS.add(relative);
                }
            }

            case final BearingBlock ignored -> {
                final BlockPos relative = pos.relative(state.getValue(BearingBlock.FACING));
                if (!visited.contains(relative)) {
                    TEMP_DEFAULT_POSITIONS.add(relative);
                }
            }

            case final MechanicalPistonBlock ignored -> {
                final MechanicalPistonBlock.PistonState s = state.getValue(MechanicalPistonBlock.STATE);

                if (s != MechanicalPistonBlock.PistonState.MOVING) {
                    final Direction dir = state.getValue(MechanicalPistonBlock.FACING);
                    BlockPos reverseOffset = pos.relative(dir.getOpposite());

                    if (!visited.contains(reverseOffset)) {
                        final BlockState poleState = level.getBlockState(reverseOffset);
                        if (poleState.getBlock() instanceof PistonExtensionPoleBlock && poleState.getValue(PistonExtensionPoleBlock.FACING).getAxis() == dir.getAxis()) {
                            TEMP_DEFAULT_POSITIONS.add(reverseOffset);
                        }
                    }

                    if (s == MechanicalPistonBlock.PistonState.EXTENDED || MechanicalPistonBlock.isStickyPiston(state)) {
                        reverseOffset = pos.relative(dir);
                        if (!visited.contains(reverseOffset)) {
                            TEMP_DEFAULT_POSITIONS.add(reverseOffset);
                        }
                    }
                }
            }

            case final PistonExtensionPoleBlock ignored -> {
                for (final Direction d : Iterate.directionsInAxis(state.getValue(PistonExtensionPoleBlock.FACING)
                        .getAxis())) {
                    final BlockPos offset = pos.relative(d);
                    if (!visited.contains(offset)) {
                        final BlockState blockState = level.getBlockState(offset);
                        if (isExtensionPole(blockState) && blockState.getValue(PistonExtensionPoleBlock.FACING)
                                .getAxis() == d.getAxis()) {
                            TEMP_DEFAULT_POSITIONS.add(offset);
                        }

                        if (isPistonHead(blockState) && blockState.getValue(MechanicalPistonHeadBlock.FACING)
                                .getAxis() == d.getAxis()) {
                            TEMP_DEFAULT_POSITIONS.add(offset);
                        }

                        if (blockState.getBlock() instanceof MechanicalPistonBlock) {
                            final Direction pistonFacing = blockState.getValue(MechanicalPistonBlock.FACING);
                            if (pistonFacing == d || pistonFacing == d.getOpposite()
                                    && blockState.getValue(MechanicalPistonBlock.STATE) == MechanicalPistonBlock.PistonState.EXTENDED) {
                                TEMP_DEFAULT_POSITIONS.add(offset);
                            }
                        }
                    }
                }
            }

            case final MechanicalPistonHeadBlock ignore -> {
                final Direction direction = state.getValue(MechanicalPistonHeadBlock.FACING);
                final BlockPos offset = pos.relative(direction.getOpposite());
                if (!visited.contains(offset)) {
                    final BlockState blockState = level.getBlockState(offset);
                    if (isExtensionPole(blockState) && blockState.getValue(PistonExtensionPoleBlock.FACING)
                            .getAxis() == direction.getAxis()) {
                        TEMP_DEFAULT_POSITIONS.add(offset);
                    }

                    if (blockState.getBlock() instanceof MechanicalPistonBlock) {
                        final Direction pistonFacing = blockState.getValue(MechanicalPistonBlock.FACING);
                        if (pistonFacing == direction
                                && blockState.getValue(MechanicalPistonBlock.STATE) == MechanicalPistonBlock.PistonState.EXTENDED) {
                            TEMP_DEFAULT_POSITIONS.add(offset);
                        }
                    }
                }

                if (state.getValue(MechanicalPistonHeadBlock.TYPE) == PistonType.STICKY) {
                    final BlockPos attached = pos.relative(direction);
                    if (!visited.contains(attached)) {
                        TEMP_DEFAULT_POSITIONS.add(attached);
                    }
                }
            }

            case final GantryCarriageBlock ignored -> {
                BlockPos offset = pos.relative(state.getValue(GantryCarriageBlock.FACING));
                if (!visited.contains(offset)) {
                    TEMP_DEFAULT_POSITIONS.add(offset);
                }

                final Direction.Axis rotationAxis = ((IRotate) state.getBlock()).getRotationAxis(state);
                for (final Direction d : Iterate.directionsInAxis(rotationAxis)) {
                    offset = pos.relative(d);
                    final BlockState offsetState = level.getBlockState(offset);
                    if (offsetState.is(AllBlocks.GANTRY_SHAFT) && offsetState.getValue(GantryShaftBlock.FACING).getAxis() == d.getAxis()) {
                        if (!visited.contains(offset)) {
                            TEMP_DEFAULT_POSITIONS.add(offset);
                        }
                    }
                }
            }

            case final GantryShaftBlock ignored -> {
                for (final Direction d : Iterate.directions) {
                    final BlockPos offset = pos.relative(d);
                    if (!visited.contains(offset)) {
                        final BlockState offsetState = level.getBlockState(offset);
                        final Direction facing = state.getValue(GantryShaftBlock.FACING);
                        if (d.getAxis() == facing.getAxis() && offsetState.is(AllBlocks.GANTRY_SHAFT) && offsetState.getValue(GantryShaftBlock.FACING) == facing) {
                            TEMP_DEFAULT_POSITIONS.add(offset);
                        } else if (offsetState.is(AllBlocks.GANTRY_CARRIAGE) && offsetState.getValue(GantryCarriageBlock.FACING) == d) {
                            TEMP_DEFAULT_POSITIONS.add(offset);
                        }
                    }
                }
            }

            case final StickerBlock ignored -> {
                if (state.getValue(StickerBlock.EXTENDED)) {
                    final Direction offset = state.getValue(StickerBlock.FACING);
                    final BlockPos attached = pos.relative(offset);
                    if (!visited.contains(attached) && !BlockMovementChecks.isNotSupportive(level.getBlockState(attached), offset.getOpposite())) {
                        TEMP_DEFAULT_POSITIONS.add(attached);
                    }
                }
            }

            default -> {
            }
        }

        return TEMP_DEFAULT_POSITIONS;
    }

//    private static BlockMovementChecks.CheckResult isBlockAttachedTowards(final BlockState state, final Level world, final BlockPos pos, final BlockPos direction) {
//        return BlockMovementChecks.CheckResult.PASS;
//    }

    public static void addAdditionalBlocks(final BlockState state, final Level world, final BlockPos pos, final Queue<BlockPos> frontier, final Set<BlockPos> visited) {
        for (final AdditionalBlocks additional : ADDITIONAL_BLOCK_REGISTRATIONS) {
            additional.addAdditionalBlocks(state, world, pos, visited).forEach(frontier::add);
        }
    }

    public static boolean checkIsBlockAttachedTowards(final BlockState state, final Level world, final BlockPos pos, final BlockPos direction) {
        for (final AttachedCheck check : ATTACHED_CHECKS) {
            final BlockMovementChecks.CheckResult result = check.isBlockAttachedTowards(state, world, pos, direction);
            if (result != BlockMovementChecks.CheckResult.PASS) {
                return result.toBoolean();
            }
        }
        return false;
    }

    @ApiStatus.Internal
    public static void register() {
        BlockMovementChecks.registerAttachedCheck(SimBlockMovementChecks::registerDefaultBlockAttachedTowards);
//        SimBlockMovementChecks.registerAttachedCheck(SimBlockMovementChecks::isBlockAttachedTowards);

        SimBlockMovementChecks.registerAdditionalBlocks(SimBlockMovementChecks::registerDefaultAdditionalBlocks);
    }

    /**
     * Registers a check for if blocks are attached.
     * This will be used to determine if a given block is attached to another block in a given direction.
     */
    public static synchronized void registerAttachedCheck(final AttachedCheck check) {
        ATTACHED_CHECKS.addFirst(check);
    }

    /**
     * Registers an entry for adding additional blocks to simulated's assembly process.
     */
    public static synchronized void registerAdditionalBlocks(final AdditionalBlocks additionalBlocks) {
        ADDITIONAL_BLOCK_REGISTRATIONS.addFirst(additionalBlocks);
    }

    @FunctionalInterface
    public interface AttachedCheck {
        /**
         * Determine if the given block is attached to the block in the given direction.
         * As simulated assembly assembled blocks connected by edges, the direction is represented by a block-state.
         */
        BlockMovementChecks.CheckResult isBlockAttachedTowards(BlockState state, Level world, BlockPos pos, BlockPos direction);
    }

    @FunctionalInterface
    public interface AdditionalBlocks {
        /**
         * Add additional block positions to simulated assembly process.
         *
         * @param visited an <B>immutable</B> view of the current assembly's visited positions.
         */
        Iterable<BlockPos> addAdditionalBlocks(BlockState state, Level world, BlockPos pos, Set<BlockPos> visited);
    }
}
