package dev.ryanhcode.sable.network.tcp;

import dev.ryanhcode.sable.network.packets.ClientboundSableSnapshotDualPacket;
import dev.ryanhcode.sable.network.packets.ClientboundSableSnapshotInfoDualPacket;
import dev.ryanhcode.sable.network.packets.tcp.*;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Declarative registry of Sable's TCP payloads.
 *
 * <p>Veil's {@code VeilPacketManager} is replaced by Fabric-side
 * registration, which reads {@link #entries()} and adapts the networking
 * context to {@link SablePacketContext}.
 */
public class SableTCPPackets {

    public record Entry<T extends SableTCPPacket>(CustomPacketPayload.Type<T> type,
                                                  StreamCodec<? super RegistryFriendlyByteBuf, T> codec,
                                                  boolean clientbound) {
    }

    private static final List<Entry<?>> ENTRIES = new ArrayList<>();

    public static void init() {
        clientbound(ClientboundSableSnapshotDualPacket.TYPE, ClientboundSableSnapshotDualPacket.CODEC);
        clientbound(ClientboundSableSnapshotInfoDualPacket.TYPE, ClientboundSableSnapshotInfoDualPacket.CODEC);
        clientbound(ClientboundStopMovingSubLevelPacket.TYPE, ClientboundStopMovingSubLevelPacket.CODEC);

        clientbound(ClientboundChangeSubLevelNamePacket.TYPE, ClientboundChangeSubLevelNamePacket.CODEC);

        clientbound(ClientboundStartTrackingSubLevelPacket.TYPE, ClientboundStartTrackingSubLevelPacket.CODEC);
        clientbound(ClientboundFinalizeSubLevelPacket.TYPE, ClientboundFinalizeSubLevelPacket.CODEC);
        clientbound(ClientboundStopTrackingSubLevelPacket.TYPE, ClientboundStopTrackingSubLevelPacket.CODEC);
        clientbound(ClientboundChangeBoundsSubLevelPacket.TYPE, ClientboundChangeBoundsSubLevelPacket.CODEC);

        clientbound(ClientboundFreezePlayerPacket.TYPE, ClientboundFreezePlayerPacket.CODEC);

        clientbound(ClientboundPhysicsPropertyPacket.TYPE, ClientboundPhysicsPropertyPacket.CODEC);
        clientbound(ClientboundFloatingBlockMaterialPacket.TYPE, ClientboundFloatingBlockMaterialPacket.CODEC);
        clientbound(ClientboundRecentlySplitSubLevelPacket.TYPE, ClientboundRecentlySplitSubLevelPacket.CODEC);

        clientbound(ClientboundSableUDPActivationPacket.TYPE, ClientboundSableUDPActivationPacket.CODEC);

        clientbound(ClientboundEnterGizmoPacket.TYPE, ClientboundEnterGizmoPacket.CODEC);

        serverbound(ServerboundPunchSubLevelPacket.TYPE, ServerboundPunchSubLevelPacket.CODEC);
        serverbound(ServerboundGizmoMoveSubLevelPacket.TYPE, ServerboundGizmoMoveSubLevelPacket.CODEC);
    }

    public static List<Entry<?>> entries() {
        return Collections.unmodifiableList(ENTRIES);
    }

    private static <T extends SableTCPPacket> void clientbound(final CustomPacketPayload.Type<T> type, final StreamCodec<? super RegistryFriendlyByteBuf, T> codec) {
        ENTRIES.add(new Entry<>(type, codec, true));
    }

    private static <T extends SableTCPPacket> void serverbound(final CustomPacketPayload.Type<T> type, final StreamCodec<? super RegistryFriendlyByteBuf, T> codec) {
        ENTRIES.add(new Entry<>(type, codec, false));
    }
}
