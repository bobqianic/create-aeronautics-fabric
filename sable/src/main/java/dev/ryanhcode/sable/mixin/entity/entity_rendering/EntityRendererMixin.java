package dev.ryanhcode.sable.mixin.entity.entity_rendering;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.companion.math.BoundingBox3d;
import dev.ryanhcode.sable.companion.math.JOMLConversion;
import dev.ryanhcode.sable.sublevel.ClientSubLevel;
import dev.ryanhcode.sable.sublevel.SubLevel;
import dev.ryanhcode.sable.sublevel.plot.LevelPlot;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Leashable;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin {

    @Shadow
    @Final
    protected EntityRenderDispatcher entityRenderDispatcher;

    // mc26.1: Entity#getBoundingBoxForCulling moved onto EntityRenderer.
    @Shadow
    protected abstract AABB getBoundingBoxForCulling(Entity entity);

    // mc26.1: Entity#noCulling became EntityRenderer#affectedByCulling.
    @Shadow
    protected abstract boolean affectedByCulling(Entity entity);

    @ModifyReturnValue(method = "getPackedLightCoords", at = @At("RETURN"))
    public final int getPackedLightCoords(final int original, final Entity arg, final float f) {
        final Vec3 lightProbeOffset = arg.getLightProbePosition(f).subtract(arg.getEyePosition(f));
        final Vector3d lightProbePosition = JOMLConversion.toJOML(Sable.HELPER.getEyePositionInterpolated(arg, f)).add(lightProbeOffset.x, lightProbeOffset.y, lightProbeOffset.z);
        final BlockPos blockpos = BlockPos.containing(lightProbePosition.x, lightProbePosition.y, lightProbePosition.z);
        // Dynamic sub-level lights are projected into the parent light engine.
        // Re-merging plot-space block light here would bypass physical blocks
        // surrounding the sub-level (for example, a normal wool enclosure) and
        // make entities and first-person items glow through the enclosure.
        return LightTexture.pack(LightTexture.block(original),
                sable$getSubLevelAccountedSkyLight(original, arg.level(), LightLayer.SKY, blockpos, lightProbePosition));
    }

    @Redirect(method = "extractRenderState", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/EntityRenderDispatcher;distanceToSqr(Lnet/minecraft/world/entity/Entity;)D"))
    private double sable$distanceToCamera(final EntityRenderDispatcher dispatcher, final Entity entity) {
        return Sable.HELPER.distanceSquaredWithSubLevels(entity.level(), Minecraft.getInstance().gameRenderer.getMainCamera().position(), entity.position());
    }

    @Redirect(method = "getSkyLightLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;getBrightness(Lnet/minecraft/world/level/LightLayer;Lnet/minecraft/core/BlockPos;)I"))
    private int sable$getSkyLightLevel(final Level instance, final LightLayer lightLayer, final BlockPos blockPos) {
        return sable$getSubLevelAccountedSkyLight(-1, instance, lightLayer, blockPos, JOMLConversion.atCenterOf(blockPos));
    }

    @Unique
    private static int sable$getSubLevelAccountedSkyLight(final int original, final Level instance, final LightLayer lightLayer, final BlockPos blockPos, final Vector3dc probePosition) {
        final Iterable<SubLevel> all = Sable.HELPER.getAllIntersecting(instance, new BoundingBox3d(blockPos));

        int baseBrightness = original == -1 ? instance.getBrightness(lightLayer, blockPos) : LightTexture.sky(original);
        final BlockPos.MutableBlockPos localPosition = new BlockPos.MutableBlockPos();
        final BlockPos.MutableBlockPos heightmapPos = new BlockPos.MutableBlockPos();
        final Vector3d tempProbePosition = new Vector3d();

        for (final SubLevel subLevel : all) {
            final ClientSubLevel clientSubLevel = (ClientSubLevel) subLevel;

            clientSubLevel.renderPose().transformPositionInverse(probePosition, tempProbePosition);
            localPosition.set(tempProbePosition.x, tempProbePosition.y, tempProbePosition.z);

            final Level level = subLevel.getLevel();
            heightmapPos.setWithOffset(localPosition, Direction.UP);
            final LevelPlot plot = subLevel.getPlot();
            boolean isAboveGround = false;

            while (heightmapPos.getY() >= plot.getBoundingBox().minY()) {
                if (!level.getBlockState(heightmapPos).isAir()) {
                    isAboveGround = true;
                    break;
                }

                heightmapPos.move(Direction.DOWN);
            }

            if (isAboveGround) {
                if (lightLayer == LightLayer.BLOCK) {
                    baseBrightness = Math.max(baseBrightness, level.getBrightness(lightLayer, localPosition));
                } else if (lightLayer == LightLayer.SKY) {
                    final int brightness = clientSubLevel.scaleSkyLight(level.getBrightness(lightLayer, localPosition));
                    baseBrightness = Math.min(baseBrightness, brightness);
                }
            }
        }

        return baseBrightness;
    }

    @Inject(method = "shouldRender", at = @At("HEAD"), cancellable = true)
    private <E extends Entity> void sable$shouldRender(final E entity, final Frustum frustum, final double pCamX, final double pCamY, final double pCamZ, final CallbackInfoReturnable<Boolean> cir) {
        if (!this.affectedByCulling(entity)) {
            cir.setReturnValue(true);
            return;
        }

        final ClientSubLevel subLevel = Sable.HELPER.getContainingClient(entity);

        if (subLevel != null) {
            final Vec3 globalPos = subLevel.renderPose().transformPosition(entity.position());
            final AABB aabb = new AABB(globalPos.x - 2.0D, globalPos.y - 2.0D, globalPos.z - 2.0D, globalPos.x + 2.0D, globalPos.y + 2.0D, globalPos.z + 2.0D);

            cir.setReturnValue(frustum.isVisible(aabb));

            return;
        }

        // on fast moving sub-levels
        final SubLevel trackingSubLevel = Sable.HELPER.getTrackingSubLevel(entity);

        if (trackingSubLevel != null) {
            final float pt = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(true);
            final Vec3 positionInterpolated = Sable.HELPER.getEyePositionInterpolated(entity, pt)
                    .subtract(0.0, entity.getEyeHeight(), 0.0);


            AABB aABB = this.getBoundingBoxForCulling(entity).inflate(0.5);
            if (aABB.hasNaN() || aABB.getSize() == 0.0) {
                aABB = new AABB(entity.getX() - 2.0, entity.getY() - 2.0, entity.getZ() - 2.0, entity.getX() + 2.0, entity.getY() + 2.0, entity.getZ() + 2.0);
            }

            aABB = aABB.move(positionInterpolated.subtract(entity.position()));

            if (frustum.isVisible(aABB)) {
                cir.setReturnValue(true);
            } else {
                if (entity instanceof final Leashable leashable) {
                    final Entity entity2 = leashable.getLeashHolder();
                    if (entity2 != null) {
                        cir.setReturnValue(frustum.isVisible(this.getBoundingBoxForCulling(entity2)));
                        return;
                    }
                }

                cir.setReturnValue(false);
            }
        }
    }
}
