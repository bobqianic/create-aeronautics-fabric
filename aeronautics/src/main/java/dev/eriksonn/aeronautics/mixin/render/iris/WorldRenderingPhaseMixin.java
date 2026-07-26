package dev.eriksonn.aeronautics.mixin.render.iris;

import dev.eriksonn.aeronautics.index.client.AeroRenderTypes;
import net.irisshaders.iris.pipeline.WorldRenderingPhase;
import net.minecraft.client.renderer.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WorldRenderingPhase.class)
public class WorldRenderingPhaseMixin {

    @Inject(method = "fromTerrainRenderType", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/RenderType;tripwire()Lnet/minecraft/client/renderer/RenderType;"), cancellable = true)
    private static void aeronautics$injectMaterialMapping(final RenderType layer, final CallbackInfoReturnable<WorldRenderingPhase> cir) {
        if (layer == AeroRenderTypes.levitite()) {
            cir.setReturnValue(WorldRenderingPhase.NONE);
        } else if (layer == AeroRenderTypes.levititeGhosts()) {
            cir.setReturnValue(WorldRenderingPhase.NONE);
        }
    }

}
