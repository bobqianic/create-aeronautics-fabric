package dev.ryanhcode.sable.mixin.entity.teleport_players;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.ryanhcode.sable.Sable;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Relative;
import org.joml.Vector3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Set;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin {

    // PORT-NOTE(mc26.1): ServerPlayer.serverLevel() was replaced by the covariant level() override.
    @Shadow
    public abstract ServerLevel level();

    @WrapMethod(method = "teleportTo(DDD)V")
    public void sable$teleportTo(final double x, final double y, final double z, final Operation<Void> original) {
        final Vector3d globalPos = Sable.HELPER.projectOutOfSubLevel(this.level(), new Vector3d(x, y, z));
        original.call(globalPos.x, globalPos.y, globalPos.z);
    }

    // PORT-NOTE(mc26.1): RelativeMovement renamed to Relative; teleportTo gained a trailing resetCamera flag.
    @WrapMethod(method = "teleportTo(Lnet/minecraft/server/level/ServerLevel;DDDLjava/util/Set;FFZ)Z")
    public boolean sable$teleportTo(final ServerLevel serverLevel, final double x, final double y, final double z, final Set<Relative> set, final float g, final float h, final boolean resetCamera, final Operation<Boolean> original) {
        final Vector3d globalPos = Sable.HELPER.projectOutOfSubLevel(serverLevel, new Vector3d(x, y, z));
        return original.call(serverLevel, globalPos.x, globalPos.y, globalPos.z, set, g, h, resetCamera);
    }
}
