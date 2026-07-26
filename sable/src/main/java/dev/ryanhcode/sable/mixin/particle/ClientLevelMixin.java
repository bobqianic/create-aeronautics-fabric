package dev.ryanhcode.sable.mixin.particle;

import com.llamalad7.mixinextras.sugar.Local;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.api.SubLevelHelper;
import dev.ryanhcode.sable.companion.math.BoundingBox3d;
import dev.ryanhcode.sable.companion.math.BoundingBox3i;
import dev.ryanhcode.sable.mixinterface.particle.ParticleExtension;
import dev.ryanhcode.sable.sublevel.ClientSubLevel;
import dev.ryanhcode.sable.sublevel.SubLevel;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.TerrainParticle;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.WritableLevelData;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Adds animate ticks for blocks on sub-levels.
 */
@Mixin(ClientLevel.class)
public abstract class ClientLevelMixin extends Level {

    @Shadow
    public abstract void doAnimateTick(int i, int j, int k, int l, RandomSource randomSource, @Nullable Block block, BlockPos.MutableBlockPos mutableBlockPos);

    // PORT-NOTE(mc26.1): the Level ctor dropped the Supplier<ProfilerFiller> parameter (Profiler.get()).
    private ClientLevelMixin(final WritableLevelData writableLevelData, final ResourceKey<Level> resourceKey, final RegistryAccess registryAccess, final Holder<DimensionType> holder, final boolean bl, final boolean bl2, final long l, final int i) {
        super(writableLevelData, resourceKey, registryAccess, holder, bl, bl2, l, i);
    }

    @Inject(method = "animateTick", at = @At("TAIL"))
    public void sable$subLevelAnimateTick(final int x, final int y, final int z, final CallbackInfo ci, @Local final RandomSource randomSource, @Local final Block block, @Local final BlockPos.MutableBlockPos pos) {
        final Iterable<SubLevel> intersectingSubLevels = Sable.HELPER.getAllIntersecting(this, new BoundingBox3d(x - 32, y - 32, z - 32, x + 32, y + 32, z + 32));

        final BoundingBox3i tickingBounds = new BoundingBox3i();
        final Vector3d playerPos = new Vector3d();

        for (final SubLevel subLevel : intersectingSubLevels) {
            final Vector3d position = subLevel.logicalPose().transformPositionInverse(playerPos.set(x, y, z));
            tickingBounds.set(
                    Mth.floor(position.x),
                    Mth.floor(position.y),
                    Mth.floor(position.z),
                    Mth.floor(position.x),
                    Mth.floor(position.y),
                    Mth.floor(position.z));
            tickingBounds.expand(16, 16, 16);
            tickingBounds.intersect(subLevel.getPlot().getBoundingBox());

            // The extra random float at the end adds random variance to the selection, so tiny sub-levels are also ticked
            final int randomCount = Mth.floor(667.0F * tickingBounds.volume() / (32 * 32 * 32) + randomSource.nextFloat());
            for (int i = 0; i < randomCount; i++) {
                final int randomX = Mth.randomBetweenInclusive(randomSource, tickingBounds.minX, tickingBounds.maxX);
                final int randomY = Mth.randomBetweenInclusive(randomSource, tickingBounds.minY, tickingBounds.maxY);
                final int randomZ = Mth.randomBetweenInclusive(randomSource, tickingBounds.minZ, tickingBounds.maxZ);
                this.doAnimateTick(randomX, randomY, randomZ, 1, randomSource, block, pos);
            }
        }
    }

    @Redirect(method = "doAddParticle", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/Vec3;distanceToSqr(DDD)D"))
    private double sable$particleDistance(final Vec3 cameraPosition, final double x, final double y, final double z) {
        return Sable.HELPER.distanceSquaredWithSubLevels(this, cameraPosition, x, y, z);
    }

    @Redirect(method = "addBreakingBlockEffect", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/particle/TerrainParticle;setPower(F)Lnet/minecraft/client/particle/Particle;"))
    private Particle sable$addCrackParticle(final TerrainParticle particle, final float power, @Local(argsOnly = true) final BlockPos pos, @Local final BlockState state) {
        final Vec3 particlePosition = new Vec3(particle.x, particle.y, particle.z);
        final SubLevel subLevel = Sable.HELPER.getContaining(this, particlePosition);
        if (subLevel == null) {
            return particle.setPower(power);
        }

        final Vec3 globalVelocity = subLevel.logicalPose().transformNormal(new Vec3(particle.xd, particle.yd, particle.zd));
        particle.xd = globalVelocity.x;
        particle.yd = globalVelocity.y;
        particle.zd = globalVelocity.z;
        particle.setPower(power);

        final Vec3 localVelocity = subLevel.logicalPose().transformNormalInverse(new Vec3(particle.xd, particle.yd, particle.zd));
        particle.xd = localVelocity.x;
        particle.yd = localVelocity.y;
        particle.zd = localVelocity.z;
        ((ParticleExtension) particle).sable$setTrackingSubLevel((ClientSubLevel) subLevel, particlePosition);
        return particle;
    }
}
