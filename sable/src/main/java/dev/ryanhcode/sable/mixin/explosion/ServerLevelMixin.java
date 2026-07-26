package dev.ryanhcode.sable.mixin.explosion;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.ryanhcode.sable.Sable;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import org.joml.Vector3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin {

    @Shadow
    public abstract ServerLevel getLevel();

    // mc26.1: explode() returns void and gained a WeightedList<ExplosionParticleInfo> param.
    @WrapMethod(method = "explode")
    public void sable$preExplode(final Entity entity,
                                 final DamageSource damageSource,
                                 final ExplosionDamageCalculator explosionDamageCalculator,
                                 final double d,
                                 final double e,
                                 final double f,
                                 final float g,
                                 final boolean bl,
                                 final Level.ExplosionInteraction explosionInteraction,
                                 final ParticleOptions particleOptions,
                                 final ParticleOptions particleOptions2,
                                 final net.minecraft.util.random.WeightedList<net.minecraft.core.particles.ExplosionParticleInfo> blockParticles,
                                 final Holder<SoundEvent> holder,
                                 final Operation<Void> original) {

        final Vector3d projectedPos = Sable.HELPER.projectOutOfSubLevel(getLevel(), new Vector3d(d, e, f));
        original.call(entity, damageSource, explosionDamageCalculator, projectedPos.x, projectedPos.y, projectedPos.z, g, bl, explosionInteraction, particleOptions, particleOptions2, blockParticles, holder);
    }
}
