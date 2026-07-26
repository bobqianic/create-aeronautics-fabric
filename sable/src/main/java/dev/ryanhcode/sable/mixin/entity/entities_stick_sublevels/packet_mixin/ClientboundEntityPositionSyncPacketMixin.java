package dev.ryanhcode.sable.mixin.entity.entities_stick_sublevels.packet_mixin;

import dev.ryanhcode.sable.mixinterface.entity.entities_stick_sublevels.packet_mixin.PacketActuallyInSubLevelExtension;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.game.ClientboundEntityPositionSyncPacket;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Carries the same plot-space marker as the relative-move and teleport
 * packets. Minecraft 1.21.10 uses this packet for full entity position syncs.
 */
@Mixin(ClientboundEntityPositionSyncPacket.class)
public class ClientboundEntityPositionSyncPacketMixin implements PacketActuallyInSubLevelExtension {

    @Shadow
    @Final
    @Mutable
    public static StreamCodec<FriendlyByteBuf, ClientboundEntityPositionSyncPacket> STREAM_CODEC;

    @Unique
    private boolean sable$actuallyInSubLevel;

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void sable$extendStreamCodec(final CallbackInfo ci) {
        final StreamCodec<FriendlyByteBuf, ClientboundEntityPositionSyncPacket> original = STREAM_CODEC;
        STREAM_CODEC = StreamCodec.of(
                (buffer, packet) -> {
                    original.encode(buffer, packet);
                    buffer.writeBoolean(((PacketActuallyInSubLevelExtension) (Object) packet).sable$isActuallyInSubLevel());
                },
                buffer -> {
                    final ClientboundEntityPositionSyncPacket packet = original.decode(buffer);
                    ((PacketActuallyInSubLevelExtension) (Object) packet).sable$setActuallyInSubLevel(buffer.readBoolean());
                    return packet;
                }
        );
    }

    @Override
    public void sable$setActuallyInSubLevel(final boolean actuallyInSubLevel) {
        this.sable$actuallyInSubLevel = actuallyInSubLevel;
    }

    @Override
    public boolean sable$isActuallyInSubLevel() {
        return this.sable$actuallyInSubLevel;
    }
}
