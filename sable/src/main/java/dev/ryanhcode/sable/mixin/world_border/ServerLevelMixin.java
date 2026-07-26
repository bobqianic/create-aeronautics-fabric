package dev.ryanhcode.sable.mixin.world_border;

import dev.ryanhcode.sable.mixinterface.world_border.WorldBorderExtension;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * mc26.1: the worldBorder field moved off Level — link the border to its level
 * from the subclass constructors instead (see ClientLevelMixin for the client side).
 */
@Mixin(ServerLevel.class)
public class ServerLevelMixin {

    @Inject(method = "<init>", at = @At("TAIL"))
    private void sable$linkWorldBorder(final CallbackInfo ci) {
        final ServerLevel self = (ServerLevel) (Object) this;
        ((WorldBorderExtension) self.getWorldBorder()).sable$setLevel((Level) (Object) this);
    }
}
