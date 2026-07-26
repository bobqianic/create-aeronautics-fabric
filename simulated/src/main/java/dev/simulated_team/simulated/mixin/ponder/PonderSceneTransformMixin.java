package dev.simulated_team.simulated.mixin.ponder;

import com.llamalad7.mixinextras.sugar.Local;
import dev.simulated_team.simulated.mixin_interface.ponder.PonderSceneExtension;
import com.zurrtum.create.client.ponder.foundation.PonderScene;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = PonderScene.SceneTransform.class, remap = false)
public abstract class PonderSceneTransformMixin {
    // todo pr create to interpolate these variables
    @Redirect(
            method = "apply(Lcom/mojang/blaze3d/vertex/PoseStack;F)Lcom/mojang/blaze3d/vertex/PoseStack;",
            at = @At(
                    value = "FIELD",
                    target = "Lcom/zurrtum/create/client/ponder/foundation/PonderScene;scaleFactor:F",
                    remap = false
            ),
            remap = true
    )
    private float interpolateScaleFactor(final PonderScene instance, @Local(argsOnly = true) final float pt) {
        return ((PonderSceneExtension) instance).simulated$getScale(pt);
    }

    @Redirect(
            method = "apply(Lcom/mojang/blaze3d/vertex/PoseStack;F)Lcom/mojang/blaze3d/vertex/PoseStack;",
            at = @At(
                    value = "FIELD",
                    target = "Lcom/zurrtum/create/client/ponder/foundation/PonderScene;yOffset:F",
                    remap = false
            ),
            remap = true
    )
    private float interpolateYOffset(final PonderScene instance, @Local(argsOnly = true) final float pt) {
        return ((PonderSceneExtension) instance).simulated$getYOffset(pt);
    }
}
