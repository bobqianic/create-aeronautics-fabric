package dev.simulated_team.simulated.mixin.sable_render;

import com.zurrtum.create.client.flywheel.impl.visualization.VisualizationManagerImpl;
import dev.simulated_team.simulated.compat.create.SableCreateRenderContext;
import net.minecraft.world.level.LevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = VisualizationManagerImpl.class, remap = false)
public class VisualizationManagerImplMixin {

    @Inject(
            method = "supportsVisualization",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private static void simulated$useImmediateSubLevelRenderer(
            final LevelAccessor level,
            final CallbackInfoReturnable<Boolean> cir
    ) {
        if (SableCreateRenderContext.isActive()) {
            cir.setReturnValue(false);
        }
    }
}
