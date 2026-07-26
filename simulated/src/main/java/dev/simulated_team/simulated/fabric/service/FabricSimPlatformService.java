package dev.simulated_team.simulated.fabric.service;

import dev.simulated_team.simulated.service.SimPlatformService;
import net.fabricmc.loader.api.FabricLoader;

public final class FabricSimPlatformService implements SimPlatformService {
    @Override
    public boolean isLoaded(final String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }
}
