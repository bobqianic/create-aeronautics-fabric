package dev.ryanhcode.offroad.handlers.server;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

public interface MultiMiningSupplier {

    /**
     * @return The breaking speed of this supplier.
     */
    float getBreakingSpeed(Level level, BlockPos pos, BlockState currentState);

    /**
     * @return Whether this supplier is active. If this supplier is inactive, and there are no other suppliers destroying a position, the position's progress will be stalled and then reset.
     */
    boolean isActive();

    /**
     * @return The current location of this supplier. Primarily sed for server client syncing.
     */
    @Nullable
    BlockPos getLocation();

    /**
     * An item callback invoked when a block being broken has been destroyed. Given item stack is encouraged to be modified.
     *
     * @param stack The given stack dropped from the broken block
     */
    void itemCallback(ItemStack stack);

}
