package dev.ryanhcode.sable.mixin.sublevel_render.dynamic_lighting;

import dev.ryanhcode.sable.render.SubLevelDynamicLights;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.BlockLightEngine;
import net.minecraft.world.level.lighting.LightEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LightEngine.class)
public class LightEngineMixin {
    @Inject(method = "getState", at = @At("RETURN"), cancellable = true)
    private void sable$includeSubLevelOcclusion(
            final BlockPos blockPos,
            final CallbackInfoReturnable<BlockState> cir
    ) {
        if (!((Object) this instanceof BlockLightEngine)) {
            return;
        }

        final BlockState subLevelState =
                SubLevelDynamicLights.getOccludingBlockState(blockPos.asLong());
        if (subLevelState != null) {
            cir.setReturnValue(subLevelState);
        }
    }
}
