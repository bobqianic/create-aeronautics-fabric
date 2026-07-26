package dev.eriksonn.aeronautics.fabric;

import dev.eriksonn.aeronautics.Aeronautics;
import dev.eriksonn.aeronautics.data.AeroAdvancementTriggers;
import dev.eriksonn.aeronautics.events.AeronauticsCommonEvents;
import dev.eriksonn.aeronautics.fabric.service.FabricAeroConfigService;
import dev.eriksonn.aeronautics.index.AeroAdvancements;
import dev.eriksonn.aeronautics.index.AeroArmInteractionPoints;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

public final class AeronauticsFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        FabricAeroConfigService.register();
        Aeronautics.init();

        FabricAeroFluids.init();
        FabricAeroParticleTypes.register();
        AeroArmInteractionPoints.init();
        AeroAdvancements.init();
        AeroAdvancementTriggers.register();

        Aeronautics.getRegistrate().register();

        ServerTickEvents.END_WORLD_TICK.register(AeronauticsCommonEvents::onServerTickEnd);
        ServerLifecycleEvents.SERVER_STOPPED.register(AeronauticsCommonEvents::onServerStopped);
    }
}
