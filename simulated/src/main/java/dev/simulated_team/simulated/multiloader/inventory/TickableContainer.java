package dev.simulated_team.simulated.multiloader.inventory;

import net.minecraft.world.level.Level;

public interface TickableContainer {
    /**
     * Allows Containers to be ticked. Mst be called externally.
     *
     * @param level The level this ticking is taking place in.
     */
    void tick(Level level);
}
