package dev.simulated_team.simulated.service;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;

public interface SimFluidService {

    SimFluidService INSTANCE = ServiceUtil.load(SimFluidService.class);

    /**
     * Forge: mb -> mb (*1)
     * Fabric: mb -> FabricFluidUnits(TM) (*81)
     */
    long mbToLoaderUnits(final long mb);

    Fluid getFluidInItem(ItemStack stack);
}
