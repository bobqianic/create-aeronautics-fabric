package dev.ryanhcode.sable.fabric.client;

import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.SableClient;
import dev.ryanhcode.sable.SableClientConfig;
import dev.ryanhcode.sable.fabric.network.FabricSablePacketContext;
import dev.ryanhcode.sable.network.tcp.SableTCPPacket;
import dev.ryanhcode.sable.network.tcp.SableTCPPackets;
import dev.ryanhcode.sable.physics.config.FloatingBlockMaterialDataHandler;
import dev.ryanhcode.sable.sublevel.render.SubLevelRenderer;
import fuzs.forgeconfigapiport.fabric.api.v5.ConfigRegistry;
import fuzs.forgeconfigapiport.fabric.api.v5.ModConfigEvents;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.neoforged.fml.config.ModConfig;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public final class SableFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        SableClient.init();
        SableTCPPackets.entries().stream()
                .filter(SableTCPPackets.Entry::clientbound)
                .forEach(SableFabricClient::registerClientboundReceiver);

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> FloatingBlockMaterialDataHandler.clearMaterials());
        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(new IdentifiableResourceReloadListener() {
            @Override
            public ResourceLocation getFabricId() {
                return Sable.sablePath("sub_level_renderer");
            }

            @Override
            public CompletableFuture<Void> reload(final SharedState sharedState, final Executor preparationExecutor, final PreparationBarrier preparationBarrier, final Executor applicationExecutor) {
                return preparationBarrier.wait(null).thenRunAsync(
                        () -> SubLevelRenderer.getDispatcher().onResourceManagerReload(sharedState.resourceManager()),
                        applicationExecutor
                );
            }
        });

        ModConfigEvents.loading(Sable.MOD_ID).register(config -> {
            if (config.getSpec().equals(SableClientConfig.SPEC))
                SableClientConfig.onUpdate(false);
        });

        ModConfigEvents.reloading(Sable.MOD_ID).register(config -> {
            if (config.getSpec().equals(SableClientConfig.SPEC))
                SableClientConfig.onUpdate(true);
        });

        ConfigRegistry.INSTANCE.register(Sable.MOD_ID, ModConfig.Type.CLIENT, SableClientConfig.SPEC);
    }

    private static <T extends SableTCPPacket> void registerClientboundReceiver(final SableTCPPackets.Entry<T> entry) {
        ClientPlayNetworking.registerGlobalReceiver(entry.type(),
                (payload, context) -> payload.handle(new FabricSablePacketContext(context.player())));
    }
}
