package dev.ryanhcode.sable.mixin.entity.entities_stick_sublevels;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.api.SubLevelHelper;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.mixinterface.entity.entities_stick_sublevels.EntityStickExtension;
import dev.ryanhcode.sable.mixinterface.entity.entities_stick_sublevels.packet_mixin.PacketActuallyInSubLevelExtension;
import dev.ryanhcode.sable.sublevel.SubLevel;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundEntityPositionSyncPacket;
import net.minecraft.network.protocol.game.ClientboundMoveEntityPacket;
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PositionMoveRotation;
import net.minecraft.world.entity.Relative;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Set;
import java.util.function.BooleanSupplier;

// PORT-NOTE(mc26.1): Entity.lerpTo(DDDFFI) was replaced by the InterpolationHandler API
// (moveOrInterpolateTo). handleTeleportEntity now funnels through the private static
// setValuesFromPositionPacket(PositionMoveRotation, Set<Relative>, Entity, boolean) and
// handleMoveEntity calls Entity.moveOrInterpolateTo directly; the wraps were re-targeted
// accordingly. Lerp step counts are owned by the entity's InterpolationHandler now, so the
// custom plot lerp keeps the legacy default of 3 steps.
@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerMixin {

    @Unique
    private static final int SABLE$DEFAULT_LERP_STEPS = 3;

    @Shadow
    private ClientLevel level;

    // ordinal 1 = the main (entity != null) path; ordinal 0 is the removed-player-vehicle fallback.
    @WrapOperation(method = "handleTeleportEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientPacketListener;setValuesFromPositionPacket(Lnet/minecraft/world/entity/PositionMoveRotation;Ljava/util/Set;Lnet/minecraft/world/entity/Entity;Z)Z", ordinal = 1))
    private boolean sable$handleTeleportEntity(final PositionMoveRotation change,
                                               final Set<Relative> relatives,
                                               final Entity entity,
                                               final boolean interpolate,
                                               final Operation<Boolean> original,
                                               @Local(argsOnly = true) final ClientboundTeleportEntityPacket packet) {
        final PositionMoveRotation newValues = PositionMoveRotation.calculateAbsolute(PositionMoveRotation.of(entity), change, relatives);
        final boolean actuallyInSubLevel = (Object) packet instanceof final PacketActuallyInSubLevelExtension extension && extension.sable$isActuallyInSubLevel();

        return this.sable$lerp(entity, newValues.position(), newValues.yRot(), newValues.xRot(), SABLE$DEFAULT_LERP_STEPS, true, actuallyInSubLevel,
                () -> original.call(change, relatives, entity, interpolate));
    }

    @WrapOperation(method = "handleMoveEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;moveOrInterpolateTo(Lnet/minecraft/world/phys/Vec3;FF)V"))
    private void sable$handleMoveEntity(final Entity instance,
                                        final Vec3 target,
                                        final float yRot,
                                        final float xRot,
                                        final Operation<Void> original,
                                        @Local(argsOnly = true) final ClientboundMoveEntityPacket packet) {
        final boolean actuallyInSubLevel = (Object) packet instanceof final PacketActuallyInSubLevelExtension extension && extension.sable$isActuallyInSubLevel();

        this.sable$lerp(instance, target, yRot, xRot, SABLE$DEFAULT_LERP_STEPS, false, actuallyInSubLevel, () -> {
            original.call(instance, target, yRot, xRot);
            return false;
        });
    }

    // mc26.1 added this full-sync path for ordinary entities. In particular,
    // ServerEntity uses it when an entity's on-ground state changes, so it is
    // commonly the first movement packet sent after a mob steps onto a
    // sub-level.
    @WrapOperation(method = "handleEntityPositionSync", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;moveOrInterpolateTo(Lnet/minecraft/world/phys/Vec3;FF)V"))
    private void sable$handleEntityPositionSyncInterpolation(final Entity instance,
                                                             final Vec3 target,
                                                             final float yRot,
                                                             final float xRot,
                                                             final Operation<Void> original,
                                                             @Local(argsOnly = true) final ClientboundEntityPositionSyncPacket packet) {
        this.sable$handleEntityPositionSync(instance, target, yRot, xRot, original, packet);
    }

    @WrapOperation(method = "handleEntityPositionSync", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;snapTo(Lnet/minecraft/world/phys/Vec3;FF)V"))
    private void sable$handleEntityPositionSyncSnap(final Entity instance,
                                                    final Vec3 target,
                                                    final float yRot,
                                                    final float xRot,
                                                    final Operation<Void> original,
                                                    @Local(argsOnly = true) final ClientboundEntityPositionSyncPacket packet) {
        this.sable$handleEntityPositionSync(instance, target, yRot, xRot, original, packet);
    }

    @Unique
    private void sable$handleEntityPositionSync(final Entity entity,
                                                final Vec3 target,
                                                final float yRot,
                                                final float xRot,
                                                final Operation<Void> original,
                                                final ClientboundEntityPositionSyncPacket packet) {
        final boolean actuallyInSubLevel =
                (Object) packet instanceof final PacketActuallyInSubLevelExtension extension
                        && extension.sable$isActuallyInSubLevel();

        this.sable$lerp(entity, target, yRot, xRot, SABLE$DEFAULT_LERP_STEPS, true, actuallyInSubLevel, () -> {
            original.call(entity, target, yRot, xRot);
            return false;
        });
    }

    /**
     * @return whether the movement was applied via interpolation (relevant for the teleport path)
     */
    @Unique
    private boolean sable$lerp(final Entity entity,
                               final Vec3 target,
                               final float pYRot,
                               final float pXRot,
                               final int pLerpSteps,
                               final boolean pTeleport,
                               final boolean actuallyInSubLevel,
                               final BooleanSupplier vanillaCall) {
        final EntityStickExtension extension = (EntityStickExtension) entity;
        Vec3 pos = target;

        final SubLevelContainer container = SubLevelContainer.getContainer(this.level);
        final SubLevel subLevel = Sable.HELPER.getContaining(this.level, pos);
        final Vec3 plotPosition = extension.sable$getPlotPosition();

        if (!actuallyInSubLevel && subLevel == null && container.inBounds(BlockPos.containing(pos))) {
            // Drop the update entirely; report it as handled so vanilla skips its snap fallback.
            return true;
        }

        if (subLevel != null && !actuallyInSubLevel) {
            if (!(entity instanceof LivingEntity)) {
                pos = subLevel.logicalPose().transformPosition(pos);
                entity.moveOrInterpolateTo(pos, pYRot, pXRot);
                return true;
            }

            if (plotPosition == null) {
                // just jumped on a sub-level
                extension.sable$setPlotPosition(subLevel.logicalPose().transformPositionInverse(entity.position()));
            } else {
                final SubLevel existingSubLevel = Sable.HELPER.getContaining(this.level, plotPosition);
                if (existingSubLevel != null && subLevel != existingSubLevel) {
                    final Vec3 globalPlotPos = existingSubLevel.logicalPose().transformPosition(plotPosition);
                    extension.sable$setPlotPosition(subLevel.logicalPose().transformPositionInverse(globalPlotPos));
                }
            }

            // Rotation-only interpolation; the position is lerped by the custom plot lerp below
            entity.moveOrInterpolateTo(pYRot, pXRot);

            // This does a custom position lerp
            extension.sable$plotLerpTo(pos, pLerpSteps);
            return true;
        } else {
            final SubLevel existingSubLevel = Sable.HELPER.getContaining(this.level, entity.position());

            if (subLevel != null && actuallyInSubLevel && existingSubLevel != subLevel) {
                entity.setPos(subLevel.logicalPose().transformPositionInverse(entity.position()));
            } else if (existingSubLevel != null && subLevel == null) {
                entity.setPos(existingSubLevel.logicalPose().transformPosition(entity.position()));
            }

            final boolean result = vanillaCall.getAsBoolean();
            extension.sable$setPlotPosition(null);
            return result;
        }
    }
}
