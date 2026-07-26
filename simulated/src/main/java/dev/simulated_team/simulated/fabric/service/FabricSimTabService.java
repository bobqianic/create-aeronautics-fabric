package dev.simulated_team.simulated.fabric.service;

import dev.simulated_team.simulated.fabric.SimulatedFabric;
import dev.simulated_team.simulated.service.SimTabService;
import net.minecraft.world.item.CreativeModeTab;

public final class FabricSimTabService implements SimTabService {
    @Override
    public CreativeModeTab getCreativeTab() {
        return SimulatedFabric.TAB;
    }
}
