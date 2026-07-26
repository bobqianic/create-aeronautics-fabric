package dev.ryanhcode.sable.mixin.sublevel_render.dynamic_lighting;

import dev.ryanhcode.sable.render.SubLevelDynamicLights;
import net.minecraft.world.level.lighting.LevelLightEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LevelLightEngine.class)
public class LevelLightEngineMixin {
    @Inject(method = "runLightUpdates", at = @At("HEAD"))
    private void sable$beginSubLevelLightUpdates(final CallbackInfoReturnable<Integer> cir) {
        SubLevelDynamicLights.beginLightUpdates((LevelLightEngine) (Object) this);
    }

    @Inject(method = "runLightUpdates", at = @At("RETURN"))
    private void sable$endSubLevelLightUpdates(final CallbackInfoReturnable<Integer> cir) {
        SubLevelDynamicLights.endLightUpdates((LevelLightEngine) (Object) this);
    }
}
