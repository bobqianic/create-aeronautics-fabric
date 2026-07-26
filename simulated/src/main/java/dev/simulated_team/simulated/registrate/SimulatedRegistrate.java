package dev.simulated_team.simulated.registrate;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.zurrtum.create.foundation.data.CreateRegistrate;
import com.tterrag.registrate.builders.Builder;
import com.tterrag.registrate.util.entry.RegistryEntry;
import com.tterrag.registrate.util.nullness.NonNullFunction;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import dev.simulated_team.simulated.content.blocks.nav_table.navigation_target.NavigationTarget;
import dev.simulated_team.simulated.events.SimulatedCommonEvents;
import dev.simulated_team.simulated.index.SimDataComponents;
import dev.simulated_team.simulated.index.SimRegistries;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import io.github.fabricators_of_create.porting_lib.util.DeferredHolder;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class SimulatedRegistrate extends CreateRegistrate {

    public static final Set<String> MODS = new HashSet<>();
    public static final List<Supplier<Item>> TAB_ITEMS = Collections.synchronizedList(new ArrayList<>());
    public static final Map<ResourceLocation, ResourceLocation> ITEM_TO_SECTION = new ConcurrentHashMap<>();

    private static final Map<ResourceLocation, Supplier<ItemLike>> NAVIGATION_TARGET_ITEMS = new ConcurrentHashMap<>();

    private ResourceLocation currentSection;

    public SimulatedRegistrate(final ResourceLocation initialSection, final String modId) {
        super(modId);
        this.currentSection = initialSection;
        MODS.add(modId);
    }

    public SimulatedRegistrate inSection(final ResourceLocation section) {
        this.currentSection = section;
        return this;
    }

    public <T> Codec<T> byNameCodecExpanded(final ResourceKey<? extends Registry<T>> key) {
        return ResourceLocation.CODEC.flatXmap((resourceLoc) -> {
            T gatheredEntry = null;
            for (final RegistryEntry<T, T> entry : this.getAll(key)) {
                if (entry.getId().equals(resourceLoc)) {
                    gatheredEntry = entry.get();
                    break;
                }
            }

            if (gatheredEntry != null) {
                return DataResult.success(gatheredEntry);
            } else {
                return DataResult.error(() -> "Unknown registry element in " + key + ":" + resourceLoc);
            }
        }, (T) -> {
            ResourceLocation id = null;
            for (final RegistryEntry<T, T> entry : this.getAll(key)) {
                if (entry.is(T)) {
                    id = entry.getId();
                    break;
                }
            }

            if (id != null) {
                return DataResult.success(id);
            } else {
                return DataResult.error(() -> "Unknown registry element in " + key + ":" + T);
            }
        });
    }

    public static ResourceLocation sectionOf(final Item item) {
        return ITEM_TO_SECTION.get(BuiltInRegistries.ITEM.getKey(item));
    }

    @Override
    protected <R, T extends R> @NotNull RegistryEntry<R, T> accept(final String name, final ResourceKey<? extends Registry<R>> type, final Builder<R, T, ?, ?> builder, final NonNullSupplier<? extends T> creator, final NonNullFunction<DeferredHolder<R, T>, ? extends RegistryEntry<R, T>> entryFactory) {
        final RegistryEntry<R, T> entry = super.accept(name, type, builder, creator, entryFactory);

        if (type.equals(Registries.ITEM)) {
            final RegistryEntry<Item, ? extends Item> itemEntry = (RegistryEntry<Item, ? extends Item>) entry;
            TAB_ITEMS.add(itemEntry::get);
            ITEM_TO_SECTION.put(entry.getId(), this.currentSection);
        }

        return entry;
    }

    public void addExtraItem(final ResourceLocation item) {
        TAB_ITEMS.add(() -> BuiltInRegistries.ITEM.getOptional(item).orElse(net.minecraft.world.item.Items.AIR));
        ITEM_TO_SECTION.put(item, this.currentSection);
    }

    public <T extends NavigationTarget> RegistryEntry<NavigationTarget, T> navTarget(final String name, final NonNullSupplier<T> navTableItem, Supplier<ItemLike> itemSupplier) {
        RegistryEntry<NavigationTarget, T> entry = this.simple(this.self(), name, SimRegistries.Keys.NAVIGATION_TARGET, navTableItem);
        NAVIGATION_TARGET_ITEMS.put(entry.getId(), itemSupplier);
        return entry;
    }

    public <T extends NavigationTarget> RegistryEntry<NavigationTarget, T> navTarget(final String name, final NonNullSupplier<T> navTableItem, ItemLike item) {
        return navTarget(name, navTableItem, () -> item);
    }

    public static void onAddDefaultComponents(BiConsumer<ItemLike, Consumer<SimulatedCommonEvents.ComponentBuilder>> modify) {
        for (Map.Entry<ResourceLocation, Supplier<ItemLike>> entry : NAVIGATION_TARGET_ITEMS.entrySet()) {
            NavigationTarget target = SimRegistries.NAVIGATION_TARGET.get(entry.getKey()).map(holder -> holder.value()).orElse(null);
            ItemLike item = entry.getValue().get();
            modify.accept(item, builder -> builder
                    .set(SimDataComponents.TARGET, target));
        }
    }
}
