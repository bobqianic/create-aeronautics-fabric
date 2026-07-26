package dev.ryanhcode.sable.mixin.camera.camera_rotation;

import dev.ryanhcode.sable.ActiveSableCompanion;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.api.SubLevelHelper;
import dev.ryanhcode.sable.sublevel.SubLevel;
import net.minecraft.client.renderer.item.properties.numeric.CompassAngleState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

// PORT-NOTE(mc26.1): CompassItemPropertyFunction was replaced by CompassAngleState in the item-model
// rework; getAngleFromEntityToPos is now a private static taking an ItemOwner instead of an Entity.
@Mixin(CompassAngleState.class)
public abstract class CompassItemPropertyFunctionMixin {

    /**
     * @author RyanH
     * @reason Take into account sub-levels
     */
    @Overwrite
    private static double getAngleFromEntityToPos(final ItemOwner owner, final BlockPos pos) {
        Vec3 localPos = Vec3.atCenterOf(pos);
        final Vec3 ownerPosition = owner.position();
        double entityX = ownerPosition.x();
        double entityZ = ownerPosition.z();

        final ActiveSableCompanion helper = Sable.HELPER;
        final LivingEntity entity = owner.asLivingEntity();
        SubLevel subLevel = entity != null ? helper.getContaining(entity) : helper.getContaining(owner.level(), ownerPosition);

        if (subLevel == null && entity != null) {
            final Entity vehicle = entity.getVehicle();

            if (vehicle != null) {
                subLevel = helper.getContaining(vehicle);

                if (subLevel != null) {
                    final Vec3 localEntityPos = subLevel.lastPose().transformPositionInverse(ownerPosition);
                    entityX = localEntityPos.x;
                    entityZ = localEntityPos.z;
                }
            }
        }

        if (subLevel != null) {
            localPos = subLevel.lastPose().transformPositionInverse(localPos);
        }

        return Math.atan2(localPos.z() - entityZ, localPos.x() - entityX) / (float) (Math.PI * 2);
    }

}
