package dev.simulated_team.simulated.mixin.hold_interaction;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.authlib.GameProfile;
import dev.simulated_team.simulated.util.hold_interaction.HoldInteractionManager;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin extends AbstractClientPlayer {
    public LocalPlayerMixin(final ClientLevel clientLevel, final GameProfile gameProfile) {
        super(clientLevel, gameProfile);
    }

    @Inject(method = "isShiftKeyDown", at = @At("RETURN"), cancellable = true)
    private void simulated$handlerShiftBlock(final CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue() && !HoldInteractionManager.canCrouch()) {
            cir.setReturnValue(false);
        }
    }

    @WrapOperation(method = "tick", at = @At(value = "FIELD", target = "Lnet/minecraft/client/player/Input;shiftKeyDown:Z"))
    private boolean simulated$shhhhDontTellTheServer(final Input instance, final Operation<Boolean> original) {
        return original.call(instance) && HoldInteractionManager.canCrouch();
    }
}
