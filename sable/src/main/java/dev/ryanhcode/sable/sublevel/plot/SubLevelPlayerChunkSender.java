package dev.ryanhcode.sable.sublevel.plot;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.lighting.LevelLightEngine;

import java.util.function.Consumer;

public class SubLevelPlayerChunkSender {

    /**
     * A version of {@link net.minecraft.server.network.PlayerChunkSender} that uses the plots light engine
     */
    public static void sendChunk(final Consumer<Packet<? super ClientGamePacketListener>> listener, final LevelLightEngine lightEngine, final LevelChunk chunk) {
        listener.accept(new ClientboundLevelChunkWithLightPacket(chunk, lightEngine, null, null));
    }

    /**
     * A version of {@link net.minecraft.server.network.PlayerChunkSender} that uses the plots light engine
     */
    public static void sendChunkPoiData(final ServerLevel level, final LevelChunk chunk) {
        // PORT-NOTE(mc26.1): DebugPackets was removed; debug data now flows through the
        // subscription-based net.minecraft.util.debug system (TrackingDebugSynchronizer pushes POI
        // updates to subscribed clients automatically). The 1.21.1 call was a dev-only no-op in
        // production, so this is intentionally empty.
    }

}
