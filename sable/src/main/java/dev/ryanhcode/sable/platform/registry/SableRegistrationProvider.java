package dev.ryanhcode.sable.platform.registry;

import com.mojang.serialization.Lifecycle;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;

import java.util.function.Supplier;

/**
 * Minimal replacement for Veil's {@code foundry.veil.platform.registry.RegistrationProvider}
 * on the mc26.1 port branch. Creates a standalone vanilla registry and registers
 * entries eagerly — sufficient for Sable's two custom registries (force groups,
 * physics block property types), whose entries are added in static initializers
 * on both sides in deterministic order.
 *
 * <p>The registry is intentionally left unfrozen so addon mods can keep
 * registering custom physics property types after Sable's own bootstrap.
 */
public final class SableRegistrationProvider<T> {

    private final MappedRegistry<T> registry;

    private SableRegistrationProvider(final MappedRegistry<T> registry) {
        this.registry = registry;
    }

    public static <T> SableRegistrationProvider<T> get(final ResourceKey<? extends Registry<T>> key, final String modId) {
        return new SableRegistrationProvider<>(new MappedRegistry<>(key, Lifecycle.stable()));
    }

    public Registry<T> asVanillaRegistry() {
        return this.registry;
    }

    public <I extends T> SableRegistryObject<I> register(final ResourceLocation name, final Supplier<I> supplier) {
        final I value = supplier.get();
        Registry.register(this.registry, name, value);
        return new SableRegistryObject<>(name, value);
    }
}
