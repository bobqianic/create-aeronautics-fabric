package dev.simulated_team.simulated.content.blocks.auger_shaft;

import dev.simulated_team.simulated.content.blocks.auger_shaft.auger_groups.AugerDistributor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;

public interface BlockHarvester {

    AugerDistributor simulated$getAssociatedDistributor();

    void simulated$setDistributor(AugerDistributor distributor);


    /**
     * @param fromPos Position where the item stack is coming from
     * @param stack   Item stack to be deposited
     * @return ItemStack after modification if applicable
     */
    default ItemStack depositItemStack(final BlockPos fromPos, final ItemStack stack) {
        if (stack.isEmpty())
            return stack;

        final AugerDistributor group = this.simulated$getAssociatedDistributor();
        if (group != null) {
            return group.distributeItem(stack, fromPos);
        }

        return stack;
    }
}