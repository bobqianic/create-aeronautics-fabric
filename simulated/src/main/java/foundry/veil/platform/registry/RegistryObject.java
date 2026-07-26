package foundry.veil.platform.registry;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Supplier;

public interface RegistryObject<T> extends Supplier<T> {
    ResourceKey<T> getResourceKey();

    default ResourceLocation getId() {
        return this.getResourceKey().location();
    }

    boolean isPresent();

    Holder<T> asHolder();
}
