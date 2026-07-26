package dev.eriksonn.aeronautics.api.levitite_blend_crystallization;

import com.zurrtum.create.foundation.utility.BlockHelper;
import dev.eriksonn.aeronautics.config.AeroConfig;
import dev.eriksonn.aeronautics.index.AeroTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

public interface CrystalPropagationContext {
    /**
     * Called when this blend begins the crystallization process, after its dormant phase is over
     *
     * @param level The level that this interaction is taking place
     * @param pos   The position of the blend
     */
    void onCrystallizationInitialize(Level level, BlockPos pos, boolean isDormant);

    /**
     * Called when this blend successfully crystallizes
     *
     * @param level The level that this interaction is taking place
     * @param pos   The position of the blend
     */
    void onCrystallize(Level level, BlockPos pos);

    default void onDefaultCrystallize(final Level level, final BlockPos pos) {
        if (!level.isClientSide()) {
            level.setBlockAndUpdate(pos, this.getCrystalBlockState(level, pos));

            if (AeroConfig.server().blocks.breakBlocksOnCrystallize.get()) {
                for (final Direction dir : Direction.values()) {
                    if (level.getBlockState(pos.relative(dir)).is(AeroTags.BlockTags.LEVITITE_BREAKABLE)) {
                        boolean shouldBreak = true;
                        for (final Direction dir2 : Direction.values()) {
                            if (level.getFluidState(pos.relative(dir).relative(dir2)).is(LevititeBlendHelper.getFluid())) {
                                shouldBreak = false;
                                break;
                            }
                        }

                        if (shouldBreak) {
                            BlockHelper.destroyBlock(level, pos.relative(dir), 1.0F);
                        }
                    }
                }
            }
        }
    }

    /**
     * Called if this blend fails to crystalize
     *
     * @param level The level that this interaction is taking place
     * @param pos   The position of the blend
     */
    void onCrystallizationFail(Level level, BlockPos pos, int attempts, boolean isDormant);

    /**
     * @return The resulting crystalized BlockState
     */
    BlockState getCrystalBlockState(Level level, BlockPos pos);

    default int getNewAge(final Level level, final int attempts, final boolean isDormant) {
        return level.random.nextInt(10, 40);
    }

    default boolean shouldCrystallize(final Level level, final int attempts, final boolean isDormant) {
        final float maxAttempts = isDormant ? 10 : 5;
        return level.random.nextFloat() < attempts / maxAttempts;
    }

    boolean canSpreadTo(FluidState state);

    CrystalPropagationContext getContextForSpread(final Level level, final BlockPos pos);

    TagKey<Block> getCatalyzerTag();
}
