package dev.ryanhcode.offroad.fabric;

import dev.ryanhcode.offroad.OffroadClient;
import dev.ryanhcode.offroad.events.OffroadCommonEvents;
import foundry.veil.api.network.VeilPacketManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

public final class OffroadFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        VeilPacketManager.registerClientReceivers();
        OffroadClient.init();
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.level != null) {
                OffroadCommonEvents.tickLevelEvent(client.level);
            }
        });
    }
}
