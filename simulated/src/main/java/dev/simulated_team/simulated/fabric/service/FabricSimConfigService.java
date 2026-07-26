package dev.simulated_team.simulated.fabric.service;

import com.zurrtum.create.api.stress.BlockStressValues;
import com.zurrtum.create.catnip.config.Builder;
import com.zurrtum.create.infrastructure.config.CStress;
import dev.simulated_team.simulated.Simulated;
import dev.simulated_team.simulated.config.client.SimClient;
import dev.simulated_team.simulated.config.server.SimServer;
import dev.simulated_team.simulated.service.SimConfigService;

public final class FabricSimConfigService implements SimConfigService {
    private static SimServer server;
    private static SimClient client;

    public static void register() {
        server = Builder.create(SimServer::new, Simulated.MOD_ID, "server");
        client = Builder.create(SimClient::new, Simulated.MOD_ID, "client");

        final CStress stress = server.kinetics.stressValues;
        BlockStressValues.IMPACTS.registerProvider(stress::getImpact);
        BlockStressValues.CAPACITIES.registerProvider(stress::getCapacity);
    }

    @Override
    public SimServer server() {
        return server;
    }

    @Override
    public boolean serverLoaded() {
        return server != null;
    }

    @Override
    public SimClient client() {
        return client;
    }

    @Override
    public boolean clientLoaded() {
        return client != null;
    }
}
