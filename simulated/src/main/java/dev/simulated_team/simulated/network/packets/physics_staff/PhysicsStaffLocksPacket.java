package dev.simulated_team.simulated.network.packets.physics_staff;

import dev.simulated_team.simulated.Simulated;
import dev.simulated_team.simulated.SimulatedClient;
import foundry.veil.api.network.handler.PacketContext;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import com.zurrtum.create.catnip.codecs.stream.CatnipStreamCodecBuilders;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class PhysicsStaffLocksPacket implements CustomPacketPayload {

    public static Type<PhysicsStaffLocksPacket> TYPE = new Type<>(Simulated.path("physics_staff_locks"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PhysicsStaffLocksPacket> CODEC = StreamCodec.composite(
            ResourceKey.streamCodec(Registries.DIMENSION), packet -> packet.dimension,
            CatnipStreamCodecBuilders.list(UUIDUtil.STREAM_CODEC), packet -> packet.locks,
            PhysicsStaffLocksPacket::new
    );

    protected final List<UUID> locks;
    private final ResourceKey<Level> dimension;

    public PhysicsStaffLocksPacket(final ResourceKey<Level> dimension, final Collection<UUID> locks) {
        this.dimension = dimension;
        this.locks = new ObjectArrayList<>(locks);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(final PacketContext context) {
        SimulatedClient.PHYSICS_STAFF_CLIENT_HANDLER.setLocks(this.dimension, this.locks);
    }
}
