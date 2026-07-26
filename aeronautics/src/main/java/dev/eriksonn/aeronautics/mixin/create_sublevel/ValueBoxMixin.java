package dev.eriksonn.aeronautics.mixin.create_sublevel;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.ValueBox;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.companion.math.Pose3dc;
import dev.ryanhcode.sable.sublevel.ClientSubLevel;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Renders Create value/filter controls at a sub-level's physical pose instead
 * of at the hidden plot coordinates stored by their block entities.
 */
@Mixin(value = ValueBox.class, remap = false)
public abstract class ValueBoxMixin {

    @Shadow
    protected BlockPos pos;

    @Unique
    private final Quaternionf aeronautics$orientationStorage = new Quaternionf();

    @WrapOperation(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/vertex/PoseStack;translate(DDD)V",
                    ordinal = 0,
                    remap = true
            )
    )
    private void aeronautics$renderAtPhysicalSubLevelPose(
            final PoseStack poseStack,
            final double localX,
            final double localY,
            final double localZ,
            final Operation<Void> original,
            @Local(argsOnly = true) final Minecraft minecraft,
            @Local(argsOnly = true) final Vec3 camera,
            @Local(argsOnly = true) final float partialTick
    ) {
        if (minecraft.level == null) {
            original.call(poseStack, localX, localY, localZ);
            return;
        }

        final ClientSubLevel subLevel =
                (ClientSubLevel) Sable.HELPER.getContaining(minecraft.level, this.pos);
        if (subLevel == null) {
            original.call(poseStack, localX, localY, localZ);
            return;
        }

        final Pose3dc pose = subLevel.renderPose(partialTick);
        final Vec3 physicalOrigin = pose.transformPosition(Vec3.atLowerCornerOf(this.pos));
        original.call(
                poseStack,
                physicalOrigin.x - camera.x,
                physicalOrigin.y - camera.y,
                physicalOrigin.z - camera.z
        );
        poseStack.mulPose(this.aeronautics$orientationStorage.set(pose.orientation()));
        poseStack.scale(
                (float) pose.scale().x(),
                (float) pose.scale().y(),
                (float) pose.scale().z()
        );
    }
}
