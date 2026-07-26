package dev.eriksonn.aeronautics.api.levitite_blend_crystallization;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FluidState;

import java.util.Set;

public interface LevititeBlendDummyInterface {
    default void levititeBlendTick(final Level level, final BlockPos pos, final FluidState state) {
        final Set<BlockPos> tickedPositions = LevititeCrystallizerManager.getTickedPositions(level);
        if (tickedPositions.contains(pos))
            return;

        //temp
        LevititeBlendHelper.checkSurroundingSources(level, pos, state);
    }
}
