package com.zurrtum.create.catnip.platform;

import foundry.veil.api.network.VeilPacketManager;

/** Small network facade used by legacy Create integrations. */
public final class CatnipServices {
    public static final Network NETWORK = new Network();

    private CatnipServices() {
    }

    public static final class Network {
        public void sendToAllClients(Object packet) {
            // Legacy packet bridge is unavailable on Fabric; callers still get a safe no-op.
        }
    }
}
