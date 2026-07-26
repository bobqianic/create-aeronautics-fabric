package dev.ryanhcode.sable.mixin.world_border;

import dev.ryanhcode.sable.mixinterface.world_border.WorldBorderExtension;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.border.WorldBorder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientLevel.class)
public class ClientLevelMixin {
    @Inject(method = "getWorldBorder", at = @At("RETURN"))
    private void sable$initializeWorldBorder(final CallbackInfoReturnable<WorldBorder> cir) {
        ((WorldBorderExtension) cir.getReturnValue()).sable$setLevel((Level) (Object) this);
    }
}
