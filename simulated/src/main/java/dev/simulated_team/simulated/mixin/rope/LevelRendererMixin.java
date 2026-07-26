package dev.simulated_team.simulated.mixin.rope;

import dev.simulated_team.simulated.content.blocks.rope.strand.client.ZiplineClientManager;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.state.LevelRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {

    @Inject(method = "extractBlockOutline", at = @At("HEAD"), cancellable = true)
    private void simulated$cancelBlockHitOutline(final Camera camera, final LevelRenderState renderState, final CallbackInfo ci) {
        if (ZiplineClientManager.hoveringRope != null) {
            ci.cancel();
        }
    }
}
