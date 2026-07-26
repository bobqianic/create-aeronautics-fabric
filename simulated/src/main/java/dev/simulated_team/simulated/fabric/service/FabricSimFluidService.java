package dev.simulated_team.simulated.fabric.service;

import dev.simulated_team.simulated.service.SimFluidService;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;

public final class FabricSimFluidService implements SimFluidService {
    @Override
    public long mbToLoaderUnits(final long mb) {
        return mb * FluidConstants.BUCKET / 1000L;
    }

    @Override
    public Fluid getFluidInItem(final ItemStack stack) {
        final Storage<FluidVariant> storage = ContainerItemContext.withConstant(stack).find(FluidStorage.ITEM);
        if (storage == null) {
            return null;
        }
        for (final StorageView<FluidVariant> view : storage) {
            if (!view.isResourceBlank() && view.getAmount() > 0) {
                return view.getResource().getFluid();
            }
        }
        return null;
    }
}
