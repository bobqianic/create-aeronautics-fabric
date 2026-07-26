package dev.simulated_team.simulated.network.packets.contraption_diagram;

import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.api.SubLevelHelper;
import dev.ryanhcode.sable.sublevel.SubLevel;
import dev.simulated_team.simulated.Simulated;
import dev.simulated_team.simulated.content.entities.diagram.DiagramConfig;
import dev.simulated_team.simulated.content.entities.diagram.DiagramEntity;
import foundry.veil.api.network.handler.ServerPacketContext;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

public record DiagramSaveConfigPacket(int entityID, DiagramConfig config) implements CustomPacketPayload {
    public static final Type<DiagramSaveConfigPacket> TYPE = new Type<>(Simulated.path("save_diagram"));

    public static final StreamCodec<RegistryFriendlyByteBuf, DiagramSaveConfigPacket> CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, DiagramSaveConfigPacket::entityID,
            DiagramConfig.STREAM_CODEC, DiagramSaveConfigPacket::config,
            DiagramSaveConfigPacket::new
    );

    public void handle(final ServerPacketContext context) {
        final Level level = context.level();

        final Entity entity = level.getEntity(this.entityID());

        if (entity instanceof final DiagramEntity diagram && entity.distanceToSqr(context.player()) < 64.0 * 64.0) {
            final SubLevel subLevel = Sable.HELPER.getContaining(diagram);
            if (subLevel == null) return;

            diagram.setConfig(this.config);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
