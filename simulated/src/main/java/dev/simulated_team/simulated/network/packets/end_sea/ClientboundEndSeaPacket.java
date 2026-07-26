package dev.simulated_team.simulated.network.packets.end_sea;

import dev.simulated_team.simulated.Simulated;
import dev.simulated_team.simulated.content.end_sea.EndSeaPhysics;
import dev.simulated_team.simulated.content.end_sea.EndSeaPhysicsData;
import foundry.veil.api.network.handler.ClientPacketContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.ArrayList;
import java.util.List;

public record ClientboundEndSeaPacket(List<EndSeaPhysics> physics) implements CustomPacketPayload {
    public static final Type<ClientboundEndSeaPacket> TYPE = new CustomPacketPayload.Type<>(Simulated.path("end_sea"));

    public static final StreamCodec<ByteBuf, ClientboundEndSeaPacket> CODEC = StreamCodec.of((buf, value) -> value.write(buf), ClientboundEndSeaPacket::read);

    private static ClientboundEndSeaPacket read(final ByteBuf buf) {
        final int entries = buf.readInt();
        final ArrayList<EndSeaPhysics> physics = new ArrayList<>();
        for (int i = 0; i < entries; i++) {
            physics.add(EndSeaPhysics.STREAM_CODEC.decode(buf));
        }
        return new ClientboundEndSeaPacket(physics);
    }

    private void write(final ByteBuf buf) {
        buf.writeInt(this.physics.size());
        for (final EndSeaPhysics physics : this.physics) {
            EndSeaPhysics.STREAM_CODEC.encode(buf, physics);
        }
    }

    public void handle(final ClientPacketContext context) {
        Minecraft.getInstance().execute(() -> EndSeaPhysicsData.handleDataPacket(this));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
