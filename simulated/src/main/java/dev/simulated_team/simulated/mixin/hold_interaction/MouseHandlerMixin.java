package dev.simulated_team.simulated.mixin.hold_interaction;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import dev.simulated_team.simulated.events.SimulatedCommonClientEvents;
import dev.simulated_team.simulated.util.SimDistUtil;
import dev.simulated_team.simulated.util.click_interactions.InteractCallback;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public class MouseHandlerMixin {

    @WrapOperation(method = "turnPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;turn(DD)V"))
    private void simulated$turnPlayer(final LocalPlayer player, final double x, final double y, final Operation<Void> original) {
        if (SimDistUtil.getClientPlayer() != null && !SimDistUtil.getClientPlayer().isSpectator()) {
            final InteractCallback.Result status = SimulatedCommonClientEvents.onMouseMove(x, y);
            if (status.cancelled()) {
                return;
            }
        }
        original.call(player, x, y);
    }

    @Inject(method = "onButton", at = @At("HEAD"), cancellable = true)
    private void simulated$preOnPress(final long windowPointer, final MouseButtonInfo button, final int action, final CallbackInfo ci) {
        if (SimDistUtil.getClientPlayer() != null && !SimDistUtil.getClientPlayer().isSpectator()) {
            final InteractCallback.Result status = SimulatedCommonClientEvents.onBeforeMouseInput(InteractCallback.Input.mouse(button.button()), button.modifiers(), action);
            if (status.cancelled()) {
                ci.cancel();
            }
        }
    }

    @Inject(method = "onButton", at = @At("TAIL"))
    private void simulated$postOnPress(final long windowPointer, final MouseButtonInfo button, final int action,
                                       final CallbackInfo ci) {
        SimulatedCommonClientEvents.onAfterMouseInput(button.button(), button.modifiers(), action);
    }

    @Inject(method = "onScroll",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;getOverlay()Lnet/minecraft/client/gui/screens/Overlay;", ordinal = 0),
            cancellable = true)
    private void simulated$preOnScroll(final long l, final double d, final double e, final CallbackInfo ci, @Local(ordinal = 3) final double deltaX, @Local(ordinal = 4) final double deltaY) {
        if (SimDistUtil.getClientPlayer() != null && !SimDistUtil.getClientPlayer().isSpectator()) {
            final InteractCallback.Result status = SimulatedCommonClientEvents.onMouseScroll(deltaX, deltaY);
            if (status.cancelled()) {
                ci.cancel();
            }
        }
    }
}
