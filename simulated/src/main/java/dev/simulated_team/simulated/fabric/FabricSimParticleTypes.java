package dev.simulated_team.simulated.fabric;

import dev.simulated_team.simulated.Simulated;
import dev.simulated_team.simulated.content.particle.AugerIndicatorParticle;
import dev.simulated_team.simulated.content.particle.MagnetFieldParticle;
import dev.simulated_team.simulated.content.particle.MagnetFieldParticle2;
import dev.simulated_team.simulated.index.SimParticleTypes;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;

import java.util.Locale;

public final class FabricSimParticleTypes {
    private FabricSimParticleTypes() {
    }

    public static void register() {
        for (final SimParticleTypes type : SimParticleTypes.values()) {
            Registry.register(BuiltInRegistries.PARTICLE_TYPE,
                    Simulated.path(type.name().toLowerCase(Locale.ROOT)), type.get());
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void registerFactories() {
        final ParticleFactoryRegistry registry = ParticleFactoryRegistry.getInstance();
        registry.register((ParticleType) SimParticleTypes.MAGNET_FIELD.get(), MagnetFieldParticle.Factory::new);
        registry.register((ParticleType) SimParticleTypes.MAGNET_FIELD2.get(), MagnetFieldParticle2.Factory::new);
        registry.register((ParticleType) SimParticleTypes.AUGER_INDICATOR.get(), AugerIndicatorParticle.Factory::new);
    }
}
