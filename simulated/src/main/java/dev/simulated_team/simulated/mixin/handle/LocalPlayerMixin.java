package dev.simulated_team.simulated.mixin.handle;

import dev.simulated_team.simulated.content.blocks.handle.ClientHandleHandler;
import dev.simulated_team.simulated.index.SimClickInteractions;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LocalPlayer.class)
public class LocalPlayerMixin {

    @Inject(method = "isMovingSlowly", at = @At("HEAD"), cancellable = true)
    private void simulated$alwaysMovingSlowly(final CallbackInfoReturnable<Boolean> cir) {
        final ClientHandleHandler handleHandler = SimClickInteractions.HANDLE_HANDLER;

        if (handleHandler.isActive() && handleHandler.movingSubLevel) {
            cir.setReturnValue(true);
        }
    }

}
