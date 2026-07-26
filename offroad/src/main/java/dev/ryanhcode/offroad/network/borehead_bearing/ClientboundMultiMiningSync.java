package dev.ryanhcode.offroad.network.borehead_bearing;

import dev.ryanhcode.offroad.Offroad;
import dev.ryanhcode.offroad.handlers.client.MultiMiningClientHandler;
import dev.ryanhcode.offroad.handlers.client.MultiMiningClientHandler.ClientBlockBreakingData;
import dev.ryanhcode.offroad.handlers.server.MultiMiningServerManager.BlockBreakingData;
import foundry.veil.api.network.handler.PacketContext;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.ApiStatus;

import java.util.Map;

/**
 * A packet specialized for converting server side {@link dev.ryanhcode.offroad.handlers.server.MultiMiningServerManager.BlockBreakingData server block breaking data} into {@link ClientBlockBreakingData client block breaking data}
 */
public final class ClientboundMultiMiningSync implements CustomPacketPayload {

    public static final Type<ClientboundMultiMiningSync> TYPE = new Type<>(Offroad.path("borehead_sync_blocks"));

    public static final StreamCodec<FriendlyByteBuf, ClientboundMultiMiningSync> CODEC = StreamCodec.of(
            (buf, p) -> p.write(buf), ClientboundMultiMiningSync::read
    );

    private int breakingID;

    public final Map<BlockPos, BlockBreakingData> inData;

    private final Map<BlockPos, ClientBlockBreakingData> clientInData;

    private ClientboundMultiMiningSync(final Map<BlockPos, BlockBreakingData> inData, final Map<BlockPos, ClientBlockBreakingData> outData) {
        this.inData = inData;
        this.clientInData = outData;
    }

    /**
     * The main way to create this packet.
     *
     * @return An instance that only contains the outData collection.
     */
    public static ClientboundMultiMiningSync serverOutboundData(final int id) {
        final ClientboundMultiMiningSync syncPacket = new ClientboundMultiMiningSync(new Object2ObjectOpenHashMap<>(), null);
        syncPacket.breakingID = id;

        return syncPacket;
    }

    @ApiStatus.Internal
    private static ClientboundMultiMiningSync clientInboundData() {
        return new ClientboundMultiMiningSync(null, new Object2ObjectOpenHashMap<>());
    }

    private void write(final FriendlyByteBuf buf) {
        buf.writeInt(this.breakingID);

        buf.writeInt(this.inData.size());
        for (final Map.Entry<BlockPos, BlockBreakingData> set : this.inData.entrySet()) {
            BlockPos.STREAM_CODEC.encode(buf, set.getKey());
            set.getValue().clientAimedSerialization(buf);
        }
    }

    private static ClientboundMultiMiningSync read(final FriendlyByteBuf buf) {
        final ClientboundMultiMiningSync clientSidePacket = clientInboundData();
        clientSidePacket.breakingID = buf.readInt();

        final int size = buf.readInt();
        for (int i = 0; i < size; i++) {
            final BlockPos pos = BlockPos.STREAM_CODEC.decode(buf);

            final ClientBlockBreakingData clientData = new ClientBlockBreakingData();

            clientData.invalid = buf.readBoolean();

            if (!clientData.invalid) {
                clientData.destroyProgress = buf.readByte();
            }

            clientSidePacket.clientInData.put(pos, clientData);
        }

        return clientSidePacket;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(final PacketContext context) {
        if (context.player() == null || !context.level().isClientSide() || this.clientInData == null) {
            return;
        }

        MultiMiningClientHandler.handleInboundClientUpdate(context.level(), this.clientInData, this.breakingID);
    }
}
