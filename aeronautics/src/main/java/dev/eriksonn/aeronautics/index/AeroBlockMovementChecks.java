package dev.eriksonn.aeronautics.index;

import com.zurrtum.create.api.contraption.BlockMovementChecks;
import dev.simulated_team.simulated.index.SimBlockMovementChecks;
import dev.eriksonn.aeronautics.content.blocks.hot_air.envelope.Envelope;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.ApiStatus;

public class AeroBlockMovementChecks {
	private static BlockMovementChecks.CheckResult isBlockAttachedTowards(final BlockState state, final Level world, final BlockPos pos, final Direction direction) {
		return isBlockAttachedTowards(state, world, pos, BlockPos.ZERO.relative(direction));
	}

	private static BlockMovementChecks.CheckResult isBlockAttachedTowards(final BlockState state, final Level world, final BlockPos pos, final BlockPos direction) {
		if (state.getBlock() instanceof Envelope && world.getBlockState(pos.offset(direction)).getBlock() instanceof Envelope)
			return BlockMovementChecks.CheckResult.SUCCESS;

		return BlockMovementChecks.CheckResult.PASS;
	}

	@ApiStatus.Internal
	public static void init() {
		BlockMovementChecks.registerAttachedCheck(AeroBlockMovementChecks::isBlockAttachedTowards);
		SimBlockMovementChecks.registerAttachedCheck(AeroBlockMovementChecks::isBlockAttachedTowards);
	}
}
