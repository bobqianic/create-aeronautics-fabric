package dev.ryanhcode.sable.mixin.particle;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.ryanhcode.sable.mixinterface.particle.ParticleExtension;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleGroup;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ParticleGroup.class)
public abstract class ParticleGroupMixin {

    @WrapOperation(method = "tickParticle", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/particle/Particle;tick()V"))
    private void sable$onParticleTick(final Particle instance, final Operation<Void> original) {
        final ParticleExtension extension = (ParticleExtension) instance;

        extension.sable$initialKickOut();
        original.call(instance);
        extension.sable$moveWithInheritedVelocity();
    }
}
