package dev.simulated_team.simulated.network.packets.honey_glue;

import dev.simulated_team.simulated.Simulated;
import dev.simulated_team.simulated.content.entities.honey_glue.HoneyGlueEntity;
import foundry.veil.api.network.handler.ClientPacketContext;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.UUID;

public record HoneyGlueSyncBoundsPacket(AABB bounds, int honeyGlueId, UUID uuid) implements CustomPacketPayload {
    public static Type<HoneyGlueSyncBoundsPacket> TYPE = new Type<>(Simulated.path("honey_glue_sync"));
    public static StreamCodec<RegistryFriendlyByteBuf, HoneyGlueSyncBoundsPacket> CODEC = StreamCodec.of(HoneyGlueSyncBoundsPacket::writeToBuf, HoneyGlueSyncBoundsPacket::readFromBuf);

    public static void writeToBuf(final RegistryFriendlyByteBuf buf, final HoneyGlueSyncBoundsPacket packet) {
        writeAABB(buf, packet.bounds);
        buf.writeInt(packet.honeyGlueId());
        buf.writeBoolean(packet.uuid != null);

        if (packet.uuid != null) {
            buf.writeUUID(packet.uuid);
        }
    }

    public static HoneyGlueSyncBoundsPacket readFromBuf(final RegistryFriendlyByteBuf buf) {
        final AABB serializedBounds = new AABB(buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readDouble());
        final int honeyGlueId = buf.readInt();

        UUID uuid = null;
        if (buf.readBoolean()) {
            uuid = buf.readUUID();
        }

        return new HoneyGlueSyncBoundsPacket(serializedBounds, honeyGlueId, uuid);
    }

    public static void writeAABB(final RegistryFriendlyByteBuf byteBuf, final AABB bb) {
        byteBuf.writeDouble(bb.minX);
        byteBuf.writeDouble(bb.minY);
        byteBuf.writeDouble(bb.minZ);
        byteBuf.writeDouble(bb.maxX);
        byteBuf.writeDouble(bb.maxY);
        byteBuf.writeDouble(bb.maxZ);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(final ClientPacketContext context) {
        // don't care about updates that came from us changing the glue
        if (this.uuid != null && this.uuid.equals(context.player().getUUID()))
            return;

        final Level level = context.level();
        final Entity entity = level.getEntity(this.honeyGlueId);

        if (entity instanceof final HoneyGlueEntity honeyGlue) {
            honeyGlue.setBounds(this.bounds);
        }
    }
}
