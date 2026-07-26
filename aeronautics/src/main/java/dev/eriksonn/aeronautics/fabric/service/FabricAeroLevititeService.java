package dev.eriksonn.aeronautics.fabric.service;

import dev.eriksonn.aeronautics.fabric.FabricAeroFluids;
import dev.eriksonn.aeronautics.service.AeroLevititeService;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.Fluid;

public final class FabricAeroLevititeService implements AeroLevititeService {
    @Override
    public Item getBucket() {
        return FabricAeroFluids.LEVITITE_BLEND.getBucket().orElseThrow();
    }

    @Override
    public Fluid getFluid() {
        return FabricAeroFluids.LEVITITE_BLEND.getSource();
    }
}
