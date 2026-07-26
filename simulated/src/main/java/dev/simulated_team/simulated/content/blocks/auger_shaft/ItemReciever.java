package dev.simulated_team.simulated.content.blocks.auger_shaft;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;

public interface ItemReciever {

    /**
     * @param item Item stack to be collected
     * @param fromPos Location where the item came from
     * @return If collection was successful
     */
    ItemStack onRecieveItem(ItemStack item, BlockPos fromPos);

    boolean removed();

    boolean isActive();

}
