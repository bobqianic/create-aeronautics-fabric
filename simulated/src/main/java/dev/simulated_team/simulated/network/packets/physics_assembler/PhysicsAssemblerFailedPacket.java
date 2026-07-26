package dev.simulated_team.simulated.network.packets.physics_assembler;

import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.api.SubLevelHelper;
import dev.simulated_team.simulated.Simulated;
import dev.simulated_team.simulated.content.blocks.physics_assembler.PhysicsAssemblerBlockEntity;
import dev.simulated_team.simulated.index.SimSoundEvents;
import foundry.veil.api.network.handler.ClientPacketContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public record PhysicsAssemblerFailedPacket(BlockPos pos) implements CustomPacketPayload {

    public static Type<PhysicsAssemblerFailedPacket> TYPE = new Type<>(Simulated.path("assembler_failed"));
    public static StreamCodec<ByteBuf, PhysicsAssemblerFailedPacket> CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            PhysicsAssemblerFailedPacket::pos,
            PhysicsAssemblerFailedPacket::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(final ClientPacketContext context) {
        final Level level = context.level();

        assert level != null;

        if (level.getBlockEntity(this.pos) instanceof final PhysicsAssemblerBlockEntity blockEntity) {
            blockEntity.clientFlickLeverTo(Sable.HELPER.getContaining(level, this.pos) != null);
            blockEntity.setClientHoldLeverInPlace(false);
            SimSoundEvents.ASSEMBLER_FAIL.playAt(level, this.pos, 1.0f, 1.0f, false);
        }
    }
}
