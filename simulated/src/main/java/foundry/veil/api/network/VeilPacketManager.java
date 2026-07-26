package foundry.veil.api.network;

import foundry.veil.api.network.handler.ClientPacketContext;
import foundry.veil.api.network.handler.PacketContext;
import foundry.veil.api.network.handler.ServerPacketContext;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public final class VeilPacketManager {
    private static final Map<CustomPacketPayload.Type<?>, ClientRegistration<?>> CLIENTBOUND = new LinkedHashMap<>();
    private static final Set<CustomPacketPayload.Type<?>> REGISTERED_CLIENT_RECEIVERS = new LinkedHashSet<>();

    private VeilPacketManager() {
    }

    public static VeilPacketManager create(final String modId, final String version) {
        return new VeilPacketManager();
    }

    public <T extends CustomPacketPayload> void registerClientbound(
            final CustomPacketPayload.Type<T> id,
            final StreamCodec<? super RegistryFriendlyByteBuf, T> codec,
            final PacketHandler<ClientPacketContext, T> handler) {
        this.registerClientbound(id, codec, handler, false);
    }

    public <T extends CustomPacketPayload> void registerClientbound(
            final CustomPacketPayload.Type<T> id,
            final StreamCodec<? super RegistryFriendlyByteBuf, T> codec,
            final PacketHandler<ClientPacketContext, T> handler,
            final boolean optional) {
        PayloadTypeRegistry.playS2C().register(id, codec);
        CLIENTBOUND.put(id, new ClientRegistration<>(id, handler));
    }

    public <T extends CustomPacketPayload> void registerServerbound(
            final CustomPacketPayload.Type<T> id,
            final StreamCodec<? super RegistryFriendlyByteBuf, T> codec,
            final PacketHandler<ServerPacketContext, T> handler) {
        this.registerServerbound(id, codec, handler, false);
    }

    public <T extends CustomPacketPayload> void registerServerbound(
            final CustomPacketPayload.Type<T> id,
            final StreamCodec<? super RegistryFriendlyByteBuf, T> codec,
            final PacketHandler<ServerPacketContext, T> handler,
            final boolean optional) {
        PayloadTypeRegistry.playC2S().register(id, codec);
        ServerPlayNetworking.registerGlobalReceiver(id,
                (payload, context) -> handler.handlePacket(payload, new FabricServerPacketContext(context.player())));
    }

    public static void registerClientReceivers() {
        CLIENTBOUND.values().forEach(VeilPacketManager::registerClientReceiver);
    }

    @SuppressWarnings("unchecked")
    private static <T extends CustomPacketPayload> void registerClientReceiver(final ClientRegistration<T> registration) {
        if (!REGISTERED_CLIENT_RECEIVERS.add(registration.type())) {
            return;
        }
        ClientPlayNetworking.registerGlobalReceiver(registration.type(),
                (payload, context) -> registration.handler().handlePacket(payload,
                        new FabricClientPacketContext(context.client(), context.player())));
    }

    public static PacketSink server() {
        return new PacketSink() {
            @Override
            public void sendPacket(final CustomPacketPayload... payloads) {
                for (final CustomPacketPayload payload : payloads) {
                    ClientPlayNetworking.send(payload);
                }
            }

            @Override
            public void sendPacket(final Packet<?> packet) {
                final Minecraft minecraft = Minecraft.getInstance();
                if (minecraft.getConnection() != null) {
                    minecraft.getConnection().send(packet);
                }
            }
        };
    }

    public static PacketSink player(final ServerPlayer player) {
        return packet -> player.connection.send(packet);
    }

    public static PacketSink level(final ServerLevel level) {
        return players(PlayerLookup.world(level));
    }

    public static PacketSink around(@Nullable final ServerPlayer excluded, final ServerLevel level,
                                    final double x, final double y, final double z, final double radius) {
        final Vec3 center = new Vec3(x, y, z);
        return players(PlayerLookup.around(level, center, radius).stream()
                .filter(player -> player != excluded)
                .toList());
    }

    public static PacketSink all(final MinecraftServer server) {
        return players(PlayerLookup.all(server));
    }

    public static PacketSink tracking(final Entity entity) {
        return players(PlayerLookup.tracking(entity));
    }

    public static PacketSink trackingAndSelf(final Entity entity) {
        final Set<ServerPlayer> players = new LinkedHashSet<>(PlayerLookup.tracking(entity));
        if (entity instanceof final ServerPlayer player) {
            players.add(player);
        }
        return players(players);
    }

    public static PacketSink tracking(final ServerLevel level, final ChunkPos pos) {
        return players(PlayerLookup.tracking(level, pos));
    }

    public static PacketSink tracking(final ServerLevel level, final BlockPos pos) {
        return players(PlayerLookup.tracking(level, pos));
    }

    public static PacketSink tracking(final ServerLevel level, final BlockPos min, final BlockPos max) {
        final Set<ServerPlayer> players = new LinkedHashSet<>();
        ChunkPos.rangeClosed(new ChunkPos(min), new ChunkPos(max))
                .forEach(pos -> players.addAll(PlayerLookup.tracking(level, pos)));
        return players(players);
    }

    public static PacketSink tracking(final BlockEntity blockEntity) {
        return players(PlayerLookup.tracking(blockEntity));
    }

    private static PacketSink players(final Iterable<ServerPlayer> players) {
        return packet -> players.forEach(player -> player.connection.send(packet));
    }

    @FunctionalInterface
    public interface PacketHandler<T extends PacketContext, P extends CustomPacketPayload> {
        void handlePacket(P payload, T context);
    }

    @FunctionalInterface
    public interface PacketSink {
        default void sendPacket(final CustomPacketPayload... payloads) {
            for (final CustomPacketPayload payload : payloads) {
                this.sendPacket(new ClientboundCustomPayloadPacket(payload));
            }
        }

        void sendPacket(Packet<?> packet);
    }

    private record ClientRegistration<T extends CustomPacketPayload>(
            CustomPacketPayload.Type<T> type,
            PacketHandler<ClientPacketContext, T> handler) {
    }

    private record FabricServerPacketContext(ServerPlayer player) implements ServerPacketContext {
        @Override
        public Packet<?> createPacket(final CustomPacketPayload payload) {
            return new ClientboundCustomPayloadPacket(payload);
        }

        @Override
        public void sendPacket(final Packet<?> packet, @Nullable final PacketSendListener callback) {
            this.player.connection.send(packet);
        }

        @Override
        public void disconnect(final Component disconnectReason) {
            this.player.connection.disconnect(disconnectReason);
        }
    }

    private record FabricClientPacketContext(Minecraft client, @Nullable LocalPlayer player) implements ClientPacketContext {
        @Override
        public Packet<?> createPacket(final CustomPacketPayload payload) {
            return new ServerboundCustomPayloadPacket(payload);
        }

        @Override
        public void sendPacket(final Packet<?> packet, @Nullable final PacketSendListener callback) {
            if (this.client.getConnection() != null) {
                this.client.getConnection().send(packet);
            }
        }

        @Override
        public void disconnect(final Component disconnectReason) {
            if (this.client.getConnection() != null) {
                this.client.getConnection().getConnection().disconnect(disconnectReason);
            }
        }
    }
}
