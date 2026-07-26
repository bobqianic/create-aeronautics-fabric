package dev.simulated_team.simulated.network.packets.physics_assembler;

import dev.simulated_team.simulated.Simulated;
import dev.simulated_team.simulated.content.blocks.physics_assembler.PhysicsAssemblerBlockEntity;
import foundry.veil.api.network.handler.ClientPacketContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public record PhysicsAssemblerFlickAndHoldLeverPacket(BlockPos pos, boolean flicked) implements CustomPacketPayload {

    public static Type<PhysicsAssemblerFlickAndHoldLeverPacket> TYPE = new Type<>(Simulated.path("flick_assembler_lever"));
    public static StreamCodec<ByteBuf, PhysicsAssemblerFlickAndHoldLeverPacket> CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            PhysicsAssemblerFlickAndHoldLeverPacket::pos,
            ByteBufCodecs.BOOL,
            PhysicsAssemblerFlickAndHoldLeverPacket::flicked,
            PhysicsAssemblerFlickAndHoldLeverPacket::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(final ClientPacketContext context) {
        final Level level = context.level();

        assert level != null;

        if (level.getBlockEntity(this.pos) instanceof final PhysicsAssemblerBlockEntity blockEntity) {
            blockEntity.clientFlickLeverTo(this.flicked);
            blockEntity.setClientHoldLeverInPlace(true);
        }
    }
}
