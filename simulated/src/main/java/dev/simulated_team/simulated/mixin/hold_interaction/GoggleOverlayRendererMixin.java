package dev.simulated_team.simulated.mixin.hold_interaction;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.zurrtum.create.client.content.equipment.goggles.GoggleOverlayRenderer;
import dev.simulated_team.simulated.index.SimClickInteractions;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GoggleOverlayRenderer.class)
public class GoggleOverlayRendererMixin {
    @Shadow public static int hoverTicks;

    @Inject(method = "renderOverlay", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;clamp(FFF)F", shift = At.Shift.BEFORE, remap = true), remap = false)
    private static void decrementRenderTicks(final CallbackInfo ci) {
        if (SimClickInteractions.STEERING_WHEEL_MANAGER.isActive()) {
            hoverTicks = Mth.clamp(hoverTicks - 2, 0, 24);
        }
    }

    @WrapOperation(method = "renderOverlay", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;clamp(FFF)F", remap = true), remap = false)
    private static float fixPartialTicks(final float value, final float min, final float max, final Operation<Float> original, @Local(argsOnly = true) final DeltaTracker deltaTracker) {
        if (SimClickInteractions.STEERING_WHEEL_MANAGER.isActive()) {
            return Mth.clamp(hoverTicks - deltaTracker.getGameTimeDeltaTicks(), 0, 24) / 24;
        }
        return original.call(value, min, max);
    }

    @Inject(method = "renderOverlay", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;clamp(FFF)F", remap = true), remap = false, cancellable = true)
    private static void dontRenderTheText(final Minecraft minecraft, final GuiGraphics guiGraphics, final DeltaTracker deltaTracker, final CallbackInfo ci) {
        if (hoverTicks - deltaTracker.getGameTimeDeltaTicks() <= 0) {
            ci.cancel();
        }
    }
}
