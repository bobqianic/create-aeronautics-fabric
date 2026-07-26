package dev.simulated_team.simulated.network.packets.honey_glue;

import dev.simulated_team.simulated.Simulated;
import dev.simulated_team.simulated.content.entities.honey_glue.HoneyGlueEntity;
import dev.simulated_team.simulated.content.entities.honey_glue.HoneyGlueMaxSizing;
import dev.simulated_team.simulated.index.SimSoundEvents;
import foundry.veil.api.network.handler.PacketContext;
import com.zurrtum.create.catnip.data.Pair;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.UUID;

public record HoneyGlueChangeBoundsPacket(AABB bounds, UUID honeyGlue) implements CustomPacketPayload {
    public static Type<HoneyGlueChangeBoundsPacket> TYPE = new Type<>(Simulated.path("honey_glue_change_bounds"));
    public static StreamCodec<RegistryFriendlyByteBuf, HoneyGlueChangeBoundsPacket> CODEC = StreamCodec.of(HoneyGlueChangeBoundsPacket::writeToBuf, HoneyGlueChangeBoundsPacket::readFromBuf);

    public static void writeToBuf(final RegistryFriendlyByteBuf buf, final HoneyGlueChangeBoundsPacket packet) {
        writeAABB(buf, packet.bounds);
        buf.writeUUID(packet.honeyGlue());
    }

    public static HoneyGlueChangeBoundsPacket readFromBuf(final RegistryFriendlyByteBuf buf) {
        final AABB serializedBounds = new AABB(buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readDouble());
        return new HoneyGlueChangeBoundsPacket(serializedBounds, buf.readUUID());
    }

    public static void writeAABB(final RegistryFriendlyByteBuf bytebuf, final AABB bb) {
        HoneyGlueSyncBoundsPacket.writeAABB(bytebuf, bb);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(final PacketContext context) {
        final Level level = context.level();
        final Entity entity = ((ServerLevel) level).getEntity(this.honeyGlue);

        if (entity instanceof final HoneyGlueEntity honeyGlue) {
            final Pair<Boolean, String> pair = HoneyGlueMaxSizing.checkBounds(this.bounds);

            if (!pair.getFirst()) {
                SimSoundEvents.HONEY_ADDED.play(entity.level(), null, honeyGlue.getBoundingBox().getCenter(), 0.5F, 0.5F);
                honeyGlue.spawnParticles();
                entity.remove(Entity.RemovalReason.KILLED);
            } else {
                honeyGlue.setBoundsAndSync(this.bounds, context.player());
            }
        }
    }
}
