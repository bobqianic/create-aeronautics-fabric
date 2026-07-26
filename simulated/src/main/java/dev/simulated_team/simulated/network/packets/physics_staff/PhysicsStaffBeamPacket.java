package dev.simulated_team.simulated.network.packets.physics_staff;

import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.api.SubLevelHelper;
import dev.ryanhcode.sable.companion.math.JOMLConversion;
import dev.simulated_team.simulated.Simulated;
import dev.simulated_team.simulated.SimulatedClient;
import dev.simulated_team.simulated.util.SimCodecUtil;
import foundry.veil.api.network.handler.PacketContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.level.Level;
import org.joml.Vector3d;

import java.util.UUID;

public class PhysicsStaffBeamPacket implements CustomPacketPayload {

    public static final StreamCodec<ByteBuf, PhysicsStaffBeamPacket> CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, packet -> packet.uuid,
            SimCodecUtil.STREAM_VECTOR3D, packet -> packet.start,
            SimCodecUtil.STREAM_VECTOR3D, packet -> packet.end,
            PhysicsStaffBeamPacket::new
    );
    public static Type<PhysicsStaffBeamPacket> TYPE = new Type<>(Simulated.path("physics_staff_beam"));
    private final UUID uuid;
    private final Vector3d start;
    private final Vector3d end;

    public PhysicsStaffBeamPacket(final UUID uuid, final Vector3d start, final Vector3d end) {
        this.uuid = uuid;
        this.start = start;
        this.end = end;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(final PacketContext context) {
        final Level level = context.level();

        SimulatedClient.PHYSICS_STAFF_CLIENT_HANDLER.updateBeam(level,
                this.uuid,
                Sable.HELPER.projectOutOfSubLevel(level, JOMLConversion.toMojang(this.start)),
                JOMLConversion.toMojang(this.end));
    }
}
