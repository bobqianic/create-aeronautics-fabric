package dev.eriksonn.aeronautics.mixin.create_sublevel;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.zurrtum.create.AllHandle;
import dev.ryanhcode.sable.Sable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Create funnels most block-entity configuration packets through a shared range check.
 * Packet positions are plot-local for sub-level blocks while players remain in physical
 * world space, so the vanilla BlockPos distance check otherwise silently drops the packet.
 */
@Mixin(value = AllHandle.class, remap = false)
public abstract class AllHandleMixin {

    @WrapOperation(
            method = "onBlockEntityConfiguration",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/core/BlockPos;closerThan(Lnet/minecraft/core/Vec3i;D)Z",
                    remap = true
            )
    )
    private static boolean aeronautics$usePhysicalConfigurationDistance(
            final BlockPos blockPos,
            final Vec3i playerPos,
            final double distance,
            final Operation<Boolean> original,
            @Local(argsOnly = true) final ServerGamePacketListenerImpl listener
    ) {
        final ServerPlayer player = listener.player;
        return Sable.HELPER.distanceSquaredWithSubLevels(
                player.level(),
                blockPos.getX(),
                blockPos.getY(),
                blockPos.getZ(),
                playerPos.getX(),
                playerPos.getY(),
                playerPos.getZ()
        ) < distance * distance;
    }
}
