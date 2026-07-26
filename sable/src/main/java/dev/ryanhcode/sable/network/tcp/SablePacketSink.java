package dev.ryanhcode.sable.network.tcp;

import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;

/**
 * Destination for outgoing Sable TCP payloads.
 *
 * <p>Replaces Veil's {@code VeilPacketManager.PacketSink}. Sends ride the
 * vanilla custom-payload packets directly using the codecs registered in
 * {@link SableTCPPackets}.
 */
@FunctionalInterface
public interface SablePacketSink {

    void sendPacket(CustomPacketPayload... payloads);

    /**
     * @return a sink delivering to a single player
     */
    static SablePacketSink player(final ServerPlayer player) {
        return payloads -> {
            for (final CustomPacketPayload payload : payloads) {
                player.connection.send(new ClientboundCustomPayloadPacket(payload));
            }
        };
    }

    /**
     * @return a sink delivering to every player in the collection
     */
    static SablePacketSink players(final Collection<ServerPlayer> players) {
        return payloads -> {
            for (final ServerPlayer player : players) {
                for (final CustomPacketPayload payload : payloads) {
                    player.connection.send(new ClientboundCustomPayloadPacket(payload));
                }
            }
        };
    }

    /**
     * @return a client→server sink for the local connection
     */
    static SablePacketSink server() {
        return payloads -> {
            final var connection = Minecraft.getInstance().getConnection();
            if (connection == null) {
                return;
            }
            for (final CustomPacketPayload payload : payloads) {
                connection.send(new ServerboundCustomPayloadPacket(payload));
            }
        };
    }
}
