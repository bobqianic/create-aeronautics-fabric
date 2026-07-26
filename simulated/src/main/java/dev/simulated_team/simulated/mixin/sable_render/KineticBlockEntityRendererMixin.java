package dev.simulated_team.simulated.mixin.sable_render;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationManager;
import com.zurrtum.create.content.kinetics.base.KineticBlockEntity;
import dev.ryanhcode.sable.Sable;
import net.minecraft.world.level.LevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Flywheel visualizes block entities from the parent client level at their
 * plot-space positions. Sable renders sublevels with a separate transform, so
 * those visuals cannot replace Create's block-entity renderer there.
 */
@Mixin(KineticBlockEntityRenderer.class)
public class KineticBlockEntityRendererMixin {

    @WrapOperation(
            method = "updateRenderState",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/zurrtum/create/client/flywheel/api/visualization/VisualizationManager;supportsVisualization(Lnet/minecraft/world/level/LevelAccessor;)Z"
            )
    )
    private boolean simulated$useFallbackRendererInSubLevel(
            final LevelAccessor level,
            final Operation<Boolean> original,
            @Local(argsOnly = true) final KineticBlockEntity blockEntity
    ) {
        if (Sable.HELPER.getContaining(blockEntity) != null) {
            return false;
        }
        return original.call(level);
    }
}
