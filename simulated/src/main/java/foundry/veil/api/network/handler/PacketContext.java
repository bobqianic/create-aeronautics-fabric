package foundry.veil.api.network.handler;

import net.minecraft.network.PacketSendListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public interface PacketContext {
    @Nullable
    Player player();

    default @Nullable Level level() {
        final Player player = this.player();
        return player == null ? null : player.level();
    }

    Packet<?> createPacket(CustomPacketPayload payload);

    default void sendPacket(final CustomPacketPayload payload) {
        this.sendPacket(this.createPacket(payload), null);
    }

    default void sendPacket(final CustomPacketPayload payload, @Nullable final PacketSendListener callback) {
        this.sendPacket(this.createPacket(payload), callback);
    }

    default void sendPacket(final Packet<?> packet) {
        this.sendPacket(packet, null);
    }

    void sendPacket(Packet<?> packet, @Nullable PacketSendListener callback);

    void disconnect(Component disconnectReason);
}
