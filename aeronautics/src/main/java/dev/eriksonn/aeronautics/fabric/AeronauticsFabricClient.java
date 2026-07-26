package dev.eriksonn.aeronautics.fabric;

import dev.eriksonn.aeronautics.Aeronautics;
import dev.eriksonn.aeronautics.AeronauticsClient;
import dev.eriksonn.aeronautics.content.blocks.hot_air.hot_air_burner.IrisBurnerFlameRenderQueue;
import dev.eriksonn.aeronautics.events.AeronauticsClientEvents;
import dev.eriksonn.aeronautics.index.AeroBlocks;
import foundry.veil.api.network.VeilPacketManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.fabricmc.fabric.api.client.render.fluid.v1.SimpleFluidRenderHandler;
import net.fabricmc.fabric.api.client.rendering.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.resources.ResourceLocation;

public final class AeronauticsFabricClient implements ClientModInitializer {
    private static final ResourceLocation LEVITITE_BLEND_STILL = Aeronautics.path("fluid/levitite_blend_still");
    private static final ResourceLocation LEVITITE_BLEND_FLOW = Aeronautics.path("fluid/levitite_blend_flow");

    @Override
    public void onInitializeClient() {
        VeilPacketManager.registerClientReceivers();
        FabricAeroParticleTypes.registerFactories();

        final SimpleFluidRenderHandler fluidRenderer = new SimpleFluidRenderHandler(
                LEVITITE_BLEND_STILL, LEVITITE_BLEND_FLOW);
        FluidRenderHandlerRegistry.INSTANCE.register(
                FabricAeroFluids.LEVITITE_BLEND.getSource(),
                FabricAeroFluids.LEVITITE_BLEND.get(),
                fluidRenderer);
        BlockRenderLayerMap.putFluids(ChunkSectionLayer.TRANSLUCENT,
                FabricAeroFluids.LEVITITE_BLEND.getSource(),
                FabricAeroFluids.LEVITITE_BLEND.get());
        BlockRenderLayerMap.putBlocks(ChunkSectionLayer.TRANSLUCENT,
                AeroBlocks.LEVITITE.get(), AeroBlocks.PEARLESCENT_LEVITITE.get());
        BlockRenderLayerMap.putBlock(
                AeroBlocks.HOT_AIR_BURNER.get(),
                ChunkSectionLayer.CUTOUT_MIPPED);

        AeronauticsClient.init();

        WorldRenderEvents.START_MAIN.register(
                context -> IrisBurnerFlameRenderQueue.beginWorldFrame()
        );
        WorldRenderEvents.END_MAIN.register(
                context -> IrisBurnerFlameRenderQueue.finishWorldFrameCollection()
        );
        ClientTickEvents.START_CLIENT_TICK.register(client -> AeronauticsClientEvents.clientLevelTick(false));
        ClientTickEvents.END_CLIENT_TICK.register(client -> AeronauticsClientEvents.clientLevelTick(true));
    }
}
