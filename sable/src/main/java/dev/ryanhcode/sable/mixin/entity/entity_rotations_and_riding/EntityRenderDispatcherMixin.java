package dev.ryanhcode.sable.mixin.entity.entity_rotations_and_riding;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.api.entity.EntitySubLevelUtil;
import dev.ryanhcode.sable.mixinhelpers.camera.camera_rotation.EntitySubLevelRotationHelper;
import dev.ryanhcode.sable.sublevel.ClientSubLevel;
import dev.ryanhcode.sable.sublevel.SubLevel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaterniond;
import org.joml.Quaternionf;
import org.joml.Vector3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.IdentityHashMap;
import java.util.Map;

/**
 * Rotates entity rendering to match the sub-level's rotation
 */
@Mixin(EntityRenderDispatcher.class)
public class EntityRenderDispatcherMixin {

    @Unique
    private final Map<EntityRenderState, SableRenderData> sable$renderData = new IdentityHashMap<>();
    @Unique
    private boolean sable$rotated = false;
    @Unique
    private Quaternionf sable$cameraOrientation;

    @Inject(method = "extractEntity", at = @At("RETURN"))
    private <E extends Entity> void sable$captureEntity(final E entity, final float partialTick, final CallbackInfoReturnable<EntityRenderState> cir) {
        final EntityRenderState renderState = cir.getReturnValue();
        this.sable$adjustLeashes(entity, renderState);
        this.sable$renderData.put(renderState, new SableRenderData(entity, partialTick));
    }

    @Inject(method = "submit", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;translate(DDD)V", shift = At.Shift.AFTER, ordinal = 0))
    private <S extends EntityRenderState> void sable$rotateEntity(final S renderState, final CameraRenderState cameraRenderState, final double x, final double y, final double z, final PoseStack poseStack, final SubmitNodeCollector submitNodeCollector, final CallbackInfo ci) {
        final SableRenderData renderData = this.sable$renderData.remove(renderState);
        if (renderData == null) {
            return;
        }

        final Entity entity = renderData.entity();
        if (!EntitySubLevelUtil.shouldKick(entity)) {
            return;
        }

        final float partialTick = renderData.partialTick();
        final Quaterniond orientation = EntitySubLevelRotationHelper.getEntityOrientation(entity, level -> ((ClientSubLevel) level).renderPose(), partialTick, EntitySubLevelRotationHelper.Type.ENTITY);

        if (orientation == null) {
            return;
        }

        poseStack.pushPose();

        final Vec3 eyeOffset = entity.getEyePosition().subtract(entity.position());

        final Vec3 offset = Sable.HELPER.getEyePositionInterpolated(entity, partialTick).subtract(entity.getEyePosition(partialTick));
        poseStack.translate(offset.x, offset.y, offset.z);

        poseStack.translate(eyeOffset.x, eyeOffset.y, eyeOffset.z);
        poseStack.mulPose(new Quaternionf(orientation));
        poseStack.translate(-eyeOffset.x, -eyeOffset.y, -eyeOffset.z);

        this.sable$cameraOrientation = new Quaternionf(cameraRenderState.orientation);
        cameraRenderState.orientation = new Quaternionf(orientation).conjugate().mul(cameraRenderState.orientation);
        this.sable$rotated = true;
    }

    @Inject(method = "submit", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;popPose()V", shift = At.Shift.BEFORE))
    private <S extends EntityRenderState> void sable$popPose(final S renderState, final CameraRenderState cameraRenderState, final double x, final double y, final double z, final PoseStack poseStack, final SubmitNodeCollector submitNodeCollector, final CallbackInfo ci) {
        if (this.sable$rotated) {
            cameraRenderState.orientation = this.sable$cameraOrientation;
            this.sable$cameraOrientation = null;
            poseStack.popPose();
            this.sable$rotated = false;
        }
    }

    @Unique
    private void sable$adjustLeashes(final Entity entity, final EntityRenderState renderState) {
        if (renderState.leashStates == null) {
            return;
        }

        final SubLevel entitySubLevel = Sable.HELPER.getContaining(entity);
        for (final EntityRenderState.LeashState leashState : renderState.leashStates) {
            final Vector3d end = new Vector3d(leashState.end.x, leashState.end.y, leashState.end.z);
            final SubLevel holderSubLevel = Sable.HELPER.getContaining(entity.level(), end);
            if (holderSubLevel != null) {
                holderSubLevel.logicalPose().transformPosition(end);
            }
            if (entitySubLevel != null) {
                entitySubLevel.logicalPose().transformPositionInverse(end);
            }
            leashState.end = new Vec3(end.x, end.y, end.z);
        }
    }

    @Unique
    private record SableRenderData(Entity entity, float partialTick) {
    }
}
