package dev.eriksonn.aeronautics.fabric;

import dev.eriksonn.aeronautics.Aeronautics;
import dev.eriksonn.aeronautics.content.particle.AirPoofParticle;
import dev.eriksonn.aeronautics.content.particle.GustParticle;
import dev.eriksonn.aeronautics.content.particle.HotAirEmberParticle;
import dev.eriksonn.aeronautics.content.particle.LevititeSparkleParticle;
import dev.eriksonn.aeronautics.content.particle.PropellerAirParticle;
import dev.eriksonn.aeronautics.index.AeroParticleTypes;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;

import java.util.Locale;

public final class FabricAeroParticleTypes {
    private FabricAeroParticleTypes() {
    }

    public static void register() {
        for (final AeroParticleTypes type : AeroParticleTypes.values()) {
            Registry.register(BuiltInRegistries.PARTICLE_TYPE,
                    Aeronautics.path(type.name().toLowerCase(Locale.ROOT)), type.get());
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void registerFactories() {
        final ParticleFactoryRegistry registry = ParticleFactoryRegistry.getInstance();
        registry.register((ParticleType) AeroParticleTypes.PROPELLER_AIR_FLOW.get(), PropellerAirParticle.Factory::new);
        registry.register((ParticleType) AeroParticleTypes.HOT_AIR_EMBER.get(), HotAirEmberParticle.Factory::new);
        registry.register((ParticleType) AeroParticleTypes.LEVITITE_SPARKLE.get(), LevititeSparkleParticle.Factory::new);
        registry.register((ParticleType) AeroParticleTypes.GUST.get(), GustParticle.Factory::new);
        registry.register((ParticleType) AeroParticleTypes.AIR_POOF.get(), AirPoofParticle.Factory::new);
    }
}
