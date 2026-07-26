package dev.simulated_team.simulated.multiloader;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.SignalGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * An interface for {@link net.minecraft.world.level.block.Block Blocks} to implement to implement redstone behaviour on both loaders.
 */
public interface CommonRedstoneBlock {
    /**
     * @param state This blocks state
     * @param level The level
     * @param pos The position of this block
     * @param direction The direction redstone dust is trying to connect towards (opposite for the face this block points towards the redstone), null if attempting to connect up or down on a diagonal
     * @return True to cause redstone dust to point towards this block
     */
    default boolean commonConnectRedstone(final BlockState state, final BlockGetter level, final BlockPos pos, @Nullable final Direction direction) {
        return false;
    }

    default boolean commonCheckWeakPower(final BlockState state, final SignalGetter level, final BlockPos pos, final Direction side) {
        return false;
    }
}
