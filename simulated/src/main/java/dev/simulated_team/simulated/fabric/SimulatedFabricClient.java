package dev.simulated_team.simulated.fabric;

import dev.simulated_team.simulated.Simulated;
import dev.simulated_team.simulated.SimulatedClient;
import dev.simulated_team.simulated.content.blocks.lasers.IrisLaserRenderQueue;
import dev.simulated_team.simulated.content.blocks.lasers.LateLaserRenderQueue;
import dev.simulated_team.simulated.content.physics_staff.PhysicsStaffRenderHandler;
import dev.simulated_team.simulated.events.SimulatedCommonClientEvents;
import dev.simulated_team.simulated.fabric.service.FabricSimpleResourceManagerRegistryService;
import dev.simulated_team.simulated.index.SimKeys;
import dev.simulated_team.simulated.index.SimSpriteShifts;
import foundry.veil.api.network.VeilPacketManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.InteractionResult;

public final class SimulatedFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        VeilPacketManager.registerClientReceivers();
        FabricSimParticleTypes.registerFactories();
        SimSpriteShifts.init();
        SimKeys.registerTo(KeyBindingHelper::registerKeyBinding);

        SimulatedClient.init();

        ClientTickEvents.START_CLIENT_TICK.register(SimulatedCommonClientEvents::preClientTick);
        ClientTickEvents.END_CLIENT_TICK.register(SimulatedCommonClientEvents::postClientTick);
        WorldRenderEvents.START_MAIN.register(context -> {
            LateLaserRenderQueue.beginWorldFrame();
            IrisLaserRenderQueue.beginWorldFrame();
        });
        WorldRenderEvents.END_MAIN.register(context -> {
            LateLaserRenderQueue.finishWorldFrameCollection();
            IrisLaserRenderQueue.finishWorldFrameCollection();
            PhysicsStaffRenderHandler.renderSelectionBox(
                    context.consumers(), context.matrices(), Minecraft.getInstance().gameRenderer.getMainCamera());
        });
        HudElementRegistry.attachElementAfter(VanillaHudElements.HOTBAR, Simulated.path("interaction_overlay"),
                (graphics, tickCounter) -> SimulatedCommonClientEvents.renderOverlays(
                        graphics, tickCounter.getGameTimeDeltaPartialTick(false)));
        ItemTooltipCallback.EVENT.register((stack, context, tooltipFlag, lines) ->
                SimulatedCommonClientEvents.appendTooltip(
                        stack, tooltipFlag, Minecraft.getInstance().player, lines));

        UseBlockCallback.EVENT.register((player, level, hand, hitResult) -> {
            if (!level.isClientSide()) {
                return InteractionResult.PASS;
            }
            final InteractionResult result = SimulatedCommonClientEvents.onRightClickBlock(
                    player, hand, hitResult.getBlockPos(), hitResult);
            if (result != null) {
                return result;
            }
            if (SimulatedCommonClientEvents.useItemOnBlockEvent(
                    level, player, player.getItemInHand(hand), hand)) {
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.PASS;
        });
        UseItemCallback.EVENT.register((player, level, hand) -> {
            if (!level.isClientSide()) {
                return InteractionResult.PASS;
            }
            SimulatedCommonClientEvents.useItemOnAirEvent(
                    level, player, player.getItemInHand(hand), hand);
            return SimulatedCommonClientEvents.useItemMappingTriggered()
                    ? InteractionResult.FAIL
                    : InteractionResult.PASS;
        });

        int listenerIndex = 0;
        for (final var listener : FabricSimpleResourceManagerRegistryService.LISTENERS) {
            ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(
                    new FabricResourceReloadListener(
                            Simulated.path("client_resources/" + listenerIndex++), listener));
        }
    }
}
