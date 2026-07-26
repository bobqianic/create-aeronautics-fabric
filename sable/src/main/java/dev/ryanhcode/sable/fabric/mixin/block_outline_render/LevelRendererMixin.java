package dev.ryanhcode.sable.fabric.mixin.block_outline_render;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.companion.math.Pose3dc;
import dev.ryanhcode.sable.sublevel.ClientSubLevel;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.state.BlockOutlineRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Transforms block hover outlines for sublevels.
 */
@Mixin(value = LevelRenderer.class, priority = 400)
// Make sure this applies first so the camera can be modified
public abstract class LevelRendererMixin {

    // Storage quaternion to avoid repeated allocation
    private final @Unique Quaternionf sable$orientationStorage = new Quaternionf();

    @Shadow
    @Nullable
    private ClientLevel level;

    @WrapOperation(method = "renderBlockOutline", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;renderHitOutline(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;DDDLnet/minecraft/client/renderer/state/BlockOutlineRenderState;I)V"))
    private void sable$renderHitOutline(final LevelRenderer instance, final PoseStack poseStack,
                                        final VertexConsumer consumer, final double camX, final double camY,
                                        final double camZ, final BlockOutlineRenderState outlineState,
                                        final int color, final Operation<Void> original) {
        final BlockPos pos = outlineState.pos();
        final ClientSubLevel subLevel = (ClientSubLevel) Sable.HELPER.getContaining(this.level, pos);

        if (subLevel == null) {
            original.call(instance, poseStack, consumer, camX, camY, camZ, outlineState, color);
            return;
        }

        poseStack.pushPose();
        try {
            final Pose3dc pose = subLevel.renderPose();
            final Vec3 physicalOrigin = pose.transformPosition(Vec3.atLowerCornerOf(pos));

            /*
             * renderHitOutline normally subtracts the camera from the plot-local BlockPos.
             * Put the matrix at the physical block origin and pass the BlockPos itself as
             * the synthetic camera so that the vanilla offset becomes zero.
             */
            poseStack.translate(
                    physicalOrigin.x - camX,
                    physicalOrigin.y - camY,
                    physicalOrigin.z - camZ
            );
            poseStack.mulPose(this.sable$orientationStorage.set(pose.orientation()));
            poseStack.scale(
                    (float) pose.scale().x(),
                    (float) pose.scale().y(),
                    (float) pose.scale().z()
            );

            original.call(
                    instance,
                poseStack,
                consumer,
                (double) pos.getX(),
                (double) pos.getY(),
                (double) pos.getZ(),
                outlineState,
                color
            );
        } finally {
            poseStack.popPose();
        }
    }
}
