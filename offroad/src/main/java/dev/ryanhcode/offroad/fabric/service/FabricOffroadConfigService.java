package dev.ryanhcode.offroad.fabric.service;

import com.zurrtum.create.api.stress.BlockStressValues;
import com.zurrtum.create.catnip.config.Builder;
import dev.ryanhcode.offroad.Offroad;
import dev.ryanhcode.offroad.config.OffroadConfig;
import dev.ryanhcode.offroad.config.client.OffroadClientConfig;
import dev.ryanhcode.offroad.config.server.OffroadServer;

public final class FabricOffroadConfigService implements OffroadConfig {
    private static OffroadServer server;
    private static OffroadClientConfig client;

    public static void register() {
        server = Builder.create(OffroadServer::new, Offroad.MOD_ID, "server");
        client = Builder.create(OffroadClientConfig::new, Offroad.MOD_ID, "client");
        BlockStressValues.IMPACTS.registerProvider(server.kinetics.stressValues::getImpact);
        BlockStressValues.CAPACITIES.registerProvider(server.kinetics.stressValues::getCapacity);
    }

    @Override
    public OffroadServer getServerConfig() {
        return server;
    }

    @Override
    public OffroadClientConfig getClientConfig() {
        return client;
    }
}
