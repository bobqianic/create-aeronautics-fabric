package dev.ryanhcode.sable.mixin.entity.entity_leashing;

import dev.ryanhcode.sable.ActiveSableCompanion;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.api.SubLevelHelper;
import dev.ryanhcode.sable.sublevel.SubLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Leashable;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(Leashable.class)
public interface LeashableMixin {

    /**
     * @author Ryan H
     * @reason Take into account sub-levels
     */
    @Overwrite
    default double leashDistanceTo(final Entity handlerEntity) {
        final ActiveSableCompanion helper = Sable.HELPER;
        final Level level = handlerEntity.level();
        final Entity leashedEntity = (Entity) (Object) this;
        final Vec3 handlerPos = helper.projectOutOfSubLevel(level, handlerEntity.getBoundingBox().getCenter());
        final Vec3 leashedPos = helper.projectOutOfSubLevel(level, leashedEntity.getBoundingBox().getCenter());
        return handlerPos.distanceTo(leashedPos);
    }

    /**
     * @author Ryan H
     * @reason Apply leash forces in the leashed entity's local frame
     */
    @Overwrite
    default boolean checkElasticInteractions(final Entity handlerEntity, final Leashable.LeashData leashData) {
        final ActiveSableCompanion helper = Sable.HELPER;
        final Level level = handlerEntity.level();
        final Entity leashedEntity = (Entity) (Object) this;
        final Vec3 handlerPos = helper.projectOutOfSubLevel(level, handlerEntity.position());
        final Vec3 leashedPos = helper.projectOutOfSubLevel(level, leashedEntity.position());
        final double elasticDistance = ((Leashable) this).leashElasticDistance();
        final double d = (handlerPos.x - leashedPos.x) / elasticDistance;
        final double e = (handlerPos.y - leashedPos.y) / elasticDistance;
        final double g = (handlerPos.z - leashedPos.z) / elasticDistance;

        Vec3 impulse = leashedEntity.getDeltaMovement().add(Math.copySign(d * d * 0.4, d), Math.copySign(e * e * 0.4, e), Math.copySign(g * g * 0.4, g));
        final SubLevel leashedSubLevel = helper.getContaining(leashedEntity);

        if (leashedSubLevel != null) {
            impulse = leashedSubLevel.logicalPose().transformNormalInverse(impulse);
        }

        leashedEntity.setDeltaMovement(impulse);
        return true;
    }
}
