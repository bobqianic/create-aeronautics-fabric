package dev.simulated_team.simulated.service;

import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;

public interface SimItemService {

    SimItemService INSTANCE = ServiceUtil.load(SimItemService.class);

    static DyeColor getDyeColor(final ItemStack itemStack) {
        return itemStack.getItem() instanceof final DyeItem dyeItem ? dyeItem.getDyeColor() : null;
    }

    int getBurnTime(final ItemStack stack);

    int getSuperheatedBurnTime(final ItemStack stack);
}
