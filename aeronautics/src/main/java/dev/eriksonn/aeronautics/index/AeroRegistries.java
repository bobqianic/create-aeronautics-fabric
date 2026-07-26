package dev.eriksonn.aeronautics.index;

import dev.eriksonn.aeronautics.Aeronautics;
import dev.eriksonn.aeronautics.api.levitite_blend_crystallization.CrystalPropagationContext;
import dev.eriksonn.aeronautics.content.blocks.hot_air.lifting_gas.LiftingGasType;
import foundry.veil.platform.registry.RegistrationProvider;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

public class AeroRegistries {
    public static class Keys {
        public static final ResourceKey<Registry<LiftingGasType>> LIFTING_GAS_TYPE = key("lifting_gas_type");
        public static final ResourceKey<Registry<CrystalPropagationContext>> LEVITITE_CRYSTAL_PROPAGATION_CONTEXT = key("levitite_crystal_propagation_context");

        private static <T> ResourceKey<Registry<T>> key(final String name) {
            return ResourceKey.createRegistryKey(Aeronautics.path(name));
        }
    }

    public static final RegistrationProvider<LiftingGasType> LIFTING_GAS_TYPE = registry(AeroRegistries.Keys.LIFTING_GAS_TYPE);
    public static final RegistrationProvider<CrystalPropagationContext> LEVITITE_CRYSTAL_PROPAGATION_CONTEXT = registry(Keys.LEVITITE_CRYSTAL_PROPAGATION_CONTEXT);

    private static <T> RegistrationProvider<T> registry(final ResourceKey<Registry<T>> registryKey) {
        return RegistrationProvider.get(registryKey, Aeronautics.MOD_ID);
    }

    public static void init() {
        // no-op, for JIT
    }
}
