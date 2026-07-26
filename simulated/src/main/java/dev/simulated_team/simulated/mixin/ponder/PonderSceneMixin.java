package dev.simulated_team.simulated.mixin.ponder;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.simulated_team.simulated.mixin_interface.ponder.PonderSceneExtension;
import com.zurrtum.create.catnip.animation.LerpedFloat;
import com.zurrtum.create.client.catnip.render.SuperRenderTypeBuffer;
import com.zurrtum.create.client.ponder.foundation.PonderScene;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = PonderScene.class, remap = false)
public class PonderSceneMixin implements PonderSceneExtension {

    @Shadow float scaleFactor;
    @Shadow float yOffset;
    @Unique
    LerpedFloat simulated$shadowAlpha = LerpedFloat.linear();
    @Unique
    Vec3 simulated$shadowOffset;
    @Unique
    Vec3 simulated$oldShadowOffset;
    @Unique
    float simulated$oldScaleFactor;
    @Unique
    float simulated$oldYOffset;

    @Inject(remap = false, method = "begin", at = @At("TAIL"))
    public void tailBegin(final CallbackInfo ci) {
        this.simulated$shadowAlpha.chase(1, 0.1, LerpedFloat.Chaser.LINEAR).startWithValue(1);
        this.simulated$shadowOffset = Vec3.ZERO;
        this.simulated$oldShadowOffset = Vec3.ZERO;
    }

    @Inject(remap = false, method = "tick", at = @At("HEAD"))
    public void headTick(final CallbackInfo ci) {
        this.simulated$shadowAlpha.tickChaser();
        this.simulated$oldShadowOffset = this.simulated$shadowOffset;
    }

    @Override
    public void simulated$toggleRenderBasePlateShadow() {
        this.simulated$shadowAlpha.updateChaseTarget(1f - this.simulated$shadowAlpha.getChaseTarget());
    }

    @Override
    public float simulated$getBasePlateAnimationTimer(final float partialTicks) {
        return this.simulated$shadowAlpha.getValue(partialTicks);
    }

    @Override
    public Vec3 simulated$getShadowOffset(final float pt) {
        return this.simulated$shadowOffset.scale(pt).add(this.simulated$oldShadowOffset.scale(1 - pt));
    }

    @Override
    public void simulated$setShadowOffset(final Vec3 shadowOffset) {
        this.simulated$shadowOffset = shadowOffset;
    }

    @Override
    public void simulated$setOldShadowOffset(final Vec3 oldShadowOffset) {
        this.simulated$oldShadowOffset = oldShadowOffset;
    }

    @Override
    public void simulated$moveShadowOffset(final Vec3 shadowDelta) {
        this.simulated$shadowOffset = this.simulated$shadowOffset.add(shadowDelta);
    }

    @Override
    public void simulated$setScaleFactor(final float scale) {
        this.scaleFactor = scale;
    }

    @Override
    public float simulated$getScale(final float pt) {
        return Mth.lerp(pt, this.simulated$oldScaleFactor, this.scaleFactor);
    }

    @Override
    public void simulated$setYOffset(final float yOffset) {
        this.yOffset = yOffset;
    }

    @Override
    public float simulated$getYOffset(final float pt) {
        return Mth.lerp(pt, this.simulated$oldYOffset, this.yOffset);
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void recordOldValues(final CallbackInfo ci) {
        this.simulated$oldYOffset = this.yOffset;
        this.simulated$oldScaleFactor = this.scaleFactor;
    }

    //todo: pr this to ponder
    @Shadow
    private PonderScene.SceneTransform transform;
    @Redirect(remap = false, method = "renderScene", at = @At(value = "INVOKE",
            target = "Lcom/zurrtum/create/client/ponder/foundation/PonderScene$SceneCamera;set(FF)V"))
    public void onCameraSet(final PonderScene.SceneCamera instance, final float xRotation, final float yRotation,
                            final Minecraft minecraft, final SuperRenderTypeBuffer buffer,
                            final SubmitNodeStorage queue, final PoseStack poseStack, final float partialTicks) {
        instance.set(-this.transform.xRotation.getValue(partialTicks),
                this.transform.yRotation.getValue(partialTicks) + 180);
    }
}
