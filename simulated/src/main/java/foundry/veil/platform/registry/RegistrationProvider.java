package foundry.veil.platform.registry;

import com.mojang.serialization.Lifecycle;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

public interface RegistrationProvider<T> {
    static <T> RegistrationProvider<T> get(final ResourceKey<? extends Registry<T>> resourceKey, final String modId) {
        return new FabricRegistrationProvider<>(registry(resourceKey), modId);
    }

    static <T> RegistrationProvider<T> get(final Registry<T> registry, final String modId) {
        return new FabricRegistrationProvider<>(registry, modId);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static <T> Registry<T> registry(final ResourceKey<? extends Registry<T>> resourceKey) {
        final Registry<T> existing = (Registry<T>) BuiltInRegistries.REGISTRY.getValue(resourceKey.location());
        if (existing != null) {
            return existing;
        }

        final MappedRegistry<T> registry = new MappedRegistry<>(resourceKey, Lifecycle.stable());
        Registry.register((Registry) BuiltInRegistries.REGISTRY, resourceKey.location(), registry);
        return registry;
    }

    default <I extends T> RegistryObject<I> register(final String name, final Supplier<? extends I> supplier) {
        return this.register(ResourceLocation.fromNamespaceAndPath(this.getModId(), name), supplier);
    }

    <I extends T> RegistryObject<I> register(ResourceLocation id, Supplier<? extends I> supplier);

    Collection<RegistryObject<T>> getEntries();

    Registry<T> asVanillaRegistry();

    String getModId();

    final class FabricRegistrationProvider<T> implements RegistrationProvider<T> {
        private final Registry<T> registry;
        private final String modId;
        private final List<RegistryObject<T>> entries = new ArrayList<>();

        private FabricRegistrationProvider(final Registry<T> registry, final String modId) {
            this.registry = registry;
            this.modId = modId;
        }

        @Override
        public <I extends T> RegistryObject<I> register(final ResourceLocation id, final Supplier<? extends I> supplier) {
            final I value = Registry.register(this.registry, id, supplier.get());
            final ResourceKey<I> key = ResourceKey.create((ResourceKey) this.registry.key(), id);
            final RegistryObject<I> object = new FabricRegistryObject<>(this.registry, key, value);
            this.entries.add((RegistryObject<T>) object);
            return object;
        }

        @Override
        public Collection<RegistryObject<T>> getEntries() {
            return Collections.unmodifiableList(this.entries);
        }

        @Override
        public Registry<T> asVanillaRegistry() {
            return this.registry;
        }

        @Override
        public String getModId() {
            return this.modId;
        }
    }

    record FabricRegistryObject<T>(Registry<? super T> registry, ResourceKey<T> getResourceKey, T value)
            implements RegistryObject<T> {
        @Override
        public boolean isPresent() {
            return true;
        }

        @Override
        public T get() {
            return this.value;
        }

        @Override
        public net.minecraft.core.Holder<T> asHolder() {
            return (net.minecraft.core.Holder<T>) this.registry.wrapAsHolder(this.value);
        }
    }
}
