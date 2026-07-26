package dev.simulated_team.simulated.mixin.ponder;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.simulated_team.simulated.mixin_interface.ponder.PonderSceneExtension;
import com.zurrtum.create.client.ponder.foundation.render.SceneRenderer;
import com.zurrtum.create.client.ponder.foundation.render.SceneRenderState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SceneRenderer.class)
public class PonderUIMixin {
    @ModifyConstant(method = "renderScene", constant = @Constant(intValue = 0x66_000000, ordinal = 0))
    private int customShadowFade(final int constant, final Minecraft minecraft, final SceneRenderState renderState,
                                 final PoseStack poseStack, final SubmitNodeStorage queue) {
        final int alpha = (int) ((constant >>> 24) * ((PonderSceneExtension) renderState.scene())
                .simulated$getBasePlateAnimationTimer(renderState.partialTicks()));
        return (alpha << 24) | (constant & 0x00_FFFFFF);
    }

    @Inject(method = "renderScene", at = @At(value = "INVOKE",
            target = "Lcom/zurrtum/create/client/catnip/gui/UIRenderHelper;flipForGuiRender(Lcom/mojang/blaze3d/vertex/PoseStack;)V"))
    private void shadowTranslate(final Minecraft minecraft, final SceneRenderState renderState,
                                 final PoseStack poseStack, final SubmitNodeStorage queue, final CallbackInfo ci) {
        final Vec3 offset = ((PonderSceneExtension) renderState.scene())
                .simulated$getShadowOffset(renderState.partialTicks());
        poseStack.translate(offset);
    }
}
