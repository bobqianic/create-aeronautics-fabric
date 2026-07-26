package dev.ryanhcode.sable;

import dev.ryanhcode.sable.network.client.SableClientNetworkEventLoop;
import net.minecraft.client.Minecraft;

public class SableClient {

    public static SableClientNetworkEventLoop NETWORK_EVENT_LOOP = new SableClientNetworkEventLoop();

    public static void init() {
        // mc26.1 port branch: Veil-based registrations removed (shader
        // preprocessors for dynamic shading / sky-light shadows / water
        // occlusion, the fancy sub-level shader processor, the ImGui container
        // inspector, and the debug gizmo handler). The vanilla render path
        // needs none of them. Re-port selectively if Veil ships a 26.1 build.
    }

    public static boolean useNativeTransport() {
        final Minecraft client = Minecraft.getInstance();
        return client.options.useNativeTransport();
    }
}
