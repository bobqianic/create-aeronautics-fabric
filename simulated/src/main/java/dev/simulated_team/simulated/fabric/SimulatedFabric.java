package dev.simulated_team.simulated.fabric;

import dev.simulated_team.simulated.Simulated;
import dev.simulated_team.simulated.command.SimCommand;
import dev.simulated_team.simulated.content.entities.honey_glue.HoneyGlueEntity;
import dev.simulated_team.simulated.content.end_sea.EndSeaPhysicsData;
import dev.simulated_team.simulated.data.advancements.SimAdvancementTriggers;
import dev.simulated_team.simulated.data.advancements.SimAdvancements;
import dev.simulated_team.simulated.events.SimulatedCommonEvents;
import dev.simulated_team.simulated.fabric.service.FabricSimConfigService;
import dev.simulated_team.simulated.fabric.service.FabricSimItemService;
import dev.simulated_team.simulated.index.SimArmInteractions;
import dev.simulated_team.simulated.index.SimBlocks;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.item.v1.DefaultItemComponentEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.EntityTrackingEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public final class SimulatedFabric implements ModInitializer {
    public static CreativeModeTab TAB;

    @Override
    public void onInitialize() {
        FabricSimConfigService.register();
        Simulated.init();

        FabricSimParticleTypes.register();
        FabricSimRecipeTypes.register();
        FabricSimStats.register();
        SimAdvancements.register();
        SimAdvancementTriggers.register();
        SimArmInteractions.init();

        Simulated.getRegistrate().register();
        TAB = Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, Simulated.path("main_tab"),
                CreativeModeTab.builder(null, -1)
                        .title(Component.translatable("itemGroup." + Simulated.MOD_ID + ".group"))
                        .icon(() -> new ItemStack(SimBlocks.PHYSICS_ASSEMBLER.get()))
                        .build());

        DefaultItemComponentEvents.MODIFY.register(context ->
                SimulatedCommonEvents.modifyDefaultComponents((item, modifier) ->
                        context.modify(item.asItem(), builder -> modifier.accept(builder::set))));
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                SimCommand.register(dispatcher, registryAccess));

        ServerChunkEvents.CHUNK_LOAD.register((level, chunk) ->
                SimulatedCommonEvents.onChunkLoad(level, chunk, false));
        ServerChunkEvents.CHUNK_GENERATE.register((level, chunk) ->
                SimulatedCommonEvents.onChunkLoad(level, chunk, true));
        ServerTickEvents.END_WORLD_TICK.register(SimulatedCommonEvents::onServerTickEnd);
        ServerLifecycleEvents.SERVER_STARTED.register(server ->
                FabricSimItemService.setServerFuels(server.fuelValues()));
        ServerLifecycleEvents.SERVER_STOPPED.register(SimulatedCommonEvents::onServerStopped);
        ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS.register((player, joined) ->
                EndSeaPhysicsData.syncDataPacket(packet -> player.connection.send(packet)));
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) ->
                SimulatedCommonEvents.onPlayerLoggedIn(handler.player));
        // Fabric's entity spawn packet does not include Honey Glue's custom bounding box.
        EntityTrackingEvents.START_TRACKING.register((entity, player) -> {
            if (entity instanceof final HoneyGlueEntity honeyGlue) {
                honeyGlue.syncBoundsTo(player);
            }
        });

        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(
                new FabricResourceReloadListener(EndSeaPhysicsData.ReloadListener.ID,
                        EndSeaPhysicsData.ReloadListener.INSTANCE));

        UseBlockCallback.EVENT.register((player, level, hand, hitResult) -> {
            final InteractionResult result = SimulatedCommonEvents.rightClickBlock(
                    level, hitResult.getBlockPos(), player, player.getItemInHand(hand));
            return result == null ? InteractionResult.PASS : result;
        });
    }
}
