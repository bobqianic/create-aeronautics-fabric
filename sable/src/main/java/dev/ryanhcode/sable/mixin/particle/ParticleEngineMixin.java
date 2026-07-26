package dev.ryanhcode.sable.mixin.particle;

import dev.ryanhcode.sable.mixinterface.particle.ParticleExtension;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ParticleEngine.class)
public abstract class ParticleEngineMixin {

    @Inject(method = "add", at = @At("TAIL"))
    private void sable$onParticleAdd(final Particle particle, final CallbackInfo ci) {
        ((ParticleExtension) particle).sable$initialKickOut();
    }
}
