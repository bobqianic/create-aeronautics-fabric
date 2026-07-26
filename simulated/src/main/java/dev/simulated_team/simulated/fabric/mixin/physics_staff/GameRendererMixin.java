package dev.simulated_team.simulated.fabric.mixin.physics_staff;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.simulated_team.simulated.content.blocks.lasers.IrisLaserRenderQueue;
import dev.simulated_team.simulated.content.physics_staff.PhysicsStaffItem;
import dev.simulated_team.simulated.content.physics_staff.PhysicsStaffRenderHandler;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
    @Shadow
    @Final
    private Minecraft minecraft;

    @Inject(method = "shouldRenderBlockOutline", at = @At("HEAD"), cancellable = true)
    private void simulated$replaceVanillaOutlineForPhysicsStaff(
            final CallbackInfoReturnable<Boolean> cir) {
        if (this.minecraft.player != null && PhysicsStaffItem.isHolding(this.minecraft.player)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(
            method = "renderLevel",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/LevelRenderer;renderLevel(Lcom/mojang/blaze3d/resource/GraphicsResourceAllocator;Lnet/minecraft/client/DeltaTracker;ZLnet/minecraft/client/Camera;Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;Lcom/mojang/blaze3d/buffers/GpuBufferSlice;Lorg/joml/Vector4f;Z)V",
                    shift = At.Shift.AFTER
            )
    )
    private void simulated$renderPhysicsStaffShaderOverlay(final DeltaTracker deltaTracker,
                                                            final CallbackInfo ci) {
        final Camera camera = this.minecraft.gameRenderer.getMainCamera();
        final PoseStack poseStack = new PoseStack();
        poseStack.mulPose(camera.rotation().conjugate(new Quaternionf()));

        final MultiBufferSource.BufferSource bufferSource = this.minecraft.renderBuffers().bufferSource();
        IrisLaserRenderQueue.drawAfterShaderComposite(bufferSource);
        if (PhysicsStaffRenderHandler.renderAfterShaderComposite(bufferSource, poseStack, camera)) {
            bufferSource.endBatch();
        }
    }
}
