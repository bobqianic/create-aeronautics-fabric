package dev.ryanhcode.sable.fabric;

import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.SableCommonEvents;
import dev.ryanhcode.sable.SableConfig;
import dev.ryanhcode.sable.command.SableCommand;
import dev.ryanhcode.sable.command.argument.SubLevelSelectorModifiers;
import dev.ryanhcode.sable.fabric.network.FabricSablePacketContext;
import dev.ryanhcode.sable.index.SableAttributes;
import dev.ryanhcode.sable.index.SableTicketTypes;
import dev.ryanhcode.sable.network.tcp.SableTCPPacket;
import dev.ryanhcode.sable.network.tcp.SableTCPPackets;
import dev.ryanhcode.sable.physics.config.FloatingBlockMaterialDataHandler;
import dev.ryanhcode.sable.physics.config.block_properties.PhysicsBlockPropertiesDefinitionLoader;
import dev.ryanhcode.sable.physics.config.dimension_physics.DimensionPhysicsData;
import fuzs.forgeconfigapiport.fabric.api.v5.ConfigRegistry;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.packs.PackType;
import net.neoforged.fml.config.ModConfig;

public final class SableFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        SableTicketTypes.SUB_LEVEL_LOADED = new net.minecraft.server.level.TicketType(
                net.minecraft.server.level.TicketType.NO_TIMEOUT,
                net.minecraft.server.level.TicketType.FLAG_LOADING | net.minecraft.server.level.TicketType.FLAG_SIMULATION
        );
        Sable.init();
        SableTCPPackets.entries().forEach(SableFabric::registerPacket);

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            SableCommand.register(dispatcher, registryAccess);
        });

        SubLevelSelectorModifiers.registerModifiers();

        SableAttributes.PUNCH_STRENGTH = Registry.registerForHolder(BuiltInRegistries.ATTRIBUTE, Sable.sablePath(SableAttributes.PUNCH_STRENGTH_NAME), SableAttributes.PUNCH_STRENGTH_ATTRIBUTE);
        SableAttributes.PUNCH_COOLDOWN = Registry.registerForHolder(BuiltInRegistries.ATTRIBUTE, Sable.sablePath(SableAttributes.PUNCH_COOLDOWN_NAME), SableAttributes.PUNCH_COOLDOWN_ATTRIBUTE);
        SableAttributes.register();

        final ResourceManagerHelper helper = ResourceManagerHelper.get(PackType.SERVER_DATA);
        helper.registerReloadListener(new ResourceReloadDelegate(PhysicsBlockPropertiesDefinitionLoader.ID, PhysicsBlockPropertiesDefinitionLoader.INSTANCE));
        helper.registerReloadListener(new ResourceReloadDelegate(DimensionPhysicsData.ReloadListener.ID, DimensionPhysicsData.ReloadListener.INSTANCE));
        helper.registerReloadListener(new ResourceReloadDelegate(FloatingBlockMaterialDataHandler.ReloadListener.ID, FloatingBlockMaterialDataHandler.ReloadListener.INSTANCE));

        ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS.register((player, joined) -> SableCommonEvents.syncDataPacket(dev.ryanhcode.sable.network.tcp.SablePacketSink.player(player)));

        ConfigRegistry.INSTANCE.register(Sable.MOD_ID, ModConfig.Type.COMMON, SableConfig.SPEC);
    }

    private static <T extends SableTCPPacket> void registerPacket(final SableTCPPackets.Entry<T> entry) {
        if (entry.clientbound()) {
            PayloadTypeRegistry.playS2C().register(entry.type(), entry.codec());
            return;
        }

        PayloadTypeRegistry.playC2S().register(entry.type(), entry.codec());
        ServerPlayNetworking.registerGlobalReceiver(entry.type(),
                (payload, context) -> payload.handle(new FabricSablePacketContext(context.player())));
    }
}
