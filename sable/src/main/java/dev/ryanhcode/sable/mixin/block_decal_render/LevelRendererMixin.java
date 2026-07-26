package dev.ryanhcode.sable.mixin.block_decal_render;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.companion.math.Pose3dc;
import dev.ryanhcode.sable.sublevel.ClientSubLevel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.state.BlockBreakingRenderState;
import net.minecraft.client.renderer.state.LevelRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Changes the distance block damage is rendered from, and transforms block damage rendering for sublevels.
 */
@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin {

    // Storage vectors to avoid repeated allocation
    private final @Unique Quaternionf sable$orientationStorage = new Quaternionf();

    @Shadow
    @Nullable
    private ClientLevel level;

    @Inject(method = "renderBlockDestroyAnimation", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;translate(DDD)V", shift = At.Shift.AFTER))
    private void sable$transformBlockDamage(final PoseStack poseStack, final net.minecraft.client.renderer.MultiBufferSource.BufferSource bufferSource, final LevelRenderState levelRenderState, final CallbackInfo ci, @Local final BlockBreakingRenderState renderState, @Local final BlockPos pos) {
        final Vec3 plotPos = Vec3.atLowerCornerOf(pos);
        final ClientSubLevel subLevel = (ClientSubLevel) Sable.HELPER.getContaining(this.level, plotPos);
        if (subLevel == null) {
            return;
        }

        final Pose3dc renderPose = subLevel.renderPose();
        final Vec3 cameraPos = levelRenderState.cameraRenderState.pos;
        final Vec3 projectedPos = renderPose.transformPosition(plotPos);
        poseStack.translate(
                -(plotPos.x - cameraPos.x),
                -(plotPos.y - cameraPos.y),
                -(plotPos.z - cameraPos.z)
        );
        poseStack.translate(projectedPos.x - cameraPos.x, projectedPos.y - cameraPos.y, projectedPos.z - cameraPos.z);
        poseStack.mulPose(this.sable$orientationStorage.set(renderPose.orientation()));
    }

    @Redirect(method = "extractBlockDestroyAnimation", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/BlockPos;distToCenterSqr(DDD)D"))
    private double sable$blockDamageDistance(final BlockPos pos, final double cameraX, final double cameraY, final double cameraZ) {
        return Sable.HELPER.distanceSquaredWithSubLevels(this.level, Vec3.atCenterOf(pos), cameraX, cameraY, cameraZ);
    }
}
