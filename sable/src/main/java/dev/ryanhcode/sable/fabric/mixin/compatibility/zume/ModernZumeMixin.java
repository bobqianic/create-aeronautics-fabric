package dev.ryanhcode.sable.fabric.mixin.compatibility.zume;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.ryanhcode.sable.annotation.MixinModVersionConstraint;
import dev.ryanhcode.sable.mixinhelpers.camera.new_camera_types.SableCameraTypes;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Zume 1.2.x maps Minecraft camera types to its own three-value perspective
 * enum by ordinal. Sable's additional camera types therefore overflow that
 * array. Both sub-level views behave as rear third-person cameras for zooming.
 */
@Pseudo
@Mixin(targets = "zume.K", remap = false)
@MixinModVersionConstraint("[1.2.1,1.2.2]")
public abstract class ModernZumeMixin {

    @ModifyExpressionValue(
            method = "d()Lzume/i;",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/lang/Enum;ordinal()I",
                    remap = false
            ),
            remap = false
    )
    private int sable$mapSubLevelCameraPerspective(final int ordinal) {
        final CameraType cameraType = Minecraft.getInstance().options.getCameraType();
        if (cameraType == SableCameraTypes.SUB_LEVEL_VIEW ||
                cameraType == SableCameraTypes.SUB_LEVEL_VIEW_UNLOCKED) {
            return CameraType.THIRD_PERSON_BACK.ordinal();
        }

        return ordinal;
    }
}
