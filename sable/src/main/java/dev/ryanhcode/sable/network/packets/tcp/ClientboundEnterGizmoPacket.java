package dev.ryanhcode.sable.network.packets.tcp;

import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.network.tcp.SableTCPPacket;
import dev.ryanhcode.sable.network.tcp.SablePacketContext;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record ClientboundEnterGizmoPacket() implements SableTCPPacket {

    public static final Type<ClientboundEnterGizmoPacket> TYPE = new Type<>(Sable.sablePath("enter_gizmo_mode"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundEnterGizmoPacket> CODEC = StreamCodec.of((buf, value) -> value.write(buf), ClientboundEnterGizmoPacket::read);

    private static ClientboundEnterGizmoPacket read(final FriendlyByteBuf buf) {
        return new ClientboundEnterGizmoPacket();
    }

    private void write(final FriendlyByteBuf buf) {

    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Override
    public void handle(final SablePacketContext context) {
        // mc26.1 port branch: the ImGui gizmo editor was stripped with Veil;
        // the packet is kept for protocol parity but does nothing client-side.
        Sable.LOGGER.warn("Gizmo mode is not available in this build (ImGui debug tools stripped on the mc26.1 port)");
    }
}