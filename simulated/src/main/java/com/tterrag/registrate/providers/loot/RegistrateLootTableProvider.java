package com.tterrag.registrate.providers.loot;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;
import com.mojang.datafixers.util.Function4;
import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.providers.RegistrateProvider;
import com.tterrag.registrate.util.nullness.NonNullConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.util.context.ContextKeySet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class RegistrateLootTableProvider extends LootTableProvider implements RegistrateProvider {

    public interface LootType<T extends RegistrateLootTables> {

        static LootType<RegistrateBlockLootTables> BLOCK = register("block", LootContextParamSets.BLOCK, RegistrateBlockLootTables::new);
        static LootType<RegistrateEntityLootTables> ENTITY = register("entity", LootContextParamSets.ENTITY, RegistrateEntityLootTables::new);

        T getLootCreator(HolderLookup.Provider provider, AbstractRegistrate<?> parent, Consumer<T> callback, FabricDataOutput output);
        ContextKeySet getLootSet();

        static <T extends RegistrateLootTables> LootType<T> register(String name, ContextKeySet set, Function4<HolderLookup.Provider, AbstractRegistrate<?>, Consumer<T>, FabricDataOutput, T> factory) {
            LootType<T> type = new LootType<T>() {
                @Override
                public T getLootCreator(HolderLookup.Provider provider, AbstractRegistrate<?> parent, Consumer<T> callback, FabricDataOutput output) {
                    return factory.apply(provider, parent, callback, output);
                }

                @Override
                public ContextKeySet getLootSet() {
                    return set;
                }
            };
            LOOT_TYPES.put(name, type);
            return type;
        }
    }

    private static final Map<String, LootType<?>> LOOT_TYPES = new HashMap<>();

    private final AbstractRegistrate<?> parent;

    private final Multimap<LootType<?>, Consumer<? super RegistrateLootTables>> specialLootActions = HashMultimap.create();
    private final Multimap<ContextKeySet, Consumer<BiConsumer<ResourceKey<LootTable>, LootTable.Builder>>> lootActions = HashMultimap.create();
    private final List<SubProviderEntry> subProviders;
    private final FabricDataOutput output;

    private CompletableFuture<HolderLookup.Provider> provider;

    public RegistrateLootTableProvider(AbstractRegistrate<?> parent, FabricDataOutput output,
                                       CompletableFuture<HolderLookup.Provider> provider) {
        this(parent, output, provider, new ArrayList<>());
    }

    private RegistrateLootTableProvider(AbstractRegistrate<?> parent, FabricDataOutput output,
                                        CompletableFuture<HolderLookup.Provider> provider,
                                        List<SubProviderEntry> subProviders) {
        super(output, Set.of(), subProviders, provider);
        this.parent = parent;
        this.output = output;
        this.provider = provider;
        this.subProviders = subProviders;
    }

    public HolderLookup.Provider getProvider(){
        return provider.getNow(null);
    }

    public <T> Holder<T> resolve(ResourceKey<T> key) {
        return getProvider().lookupOrThrow(key.registryKey()).getOrThrow(key);
    }

    @Override
    public EnvType getSide() {
        return EnvType.SERVER;
    }

    @SuppressWarnings("unchecked")
    public <T extends RegistrateLootTables> void addLootAction(LootType<T> type, NonNullConsumer<? extends RegistrateLootTables> action) {
        this.specialLootActions.put(type, (Consumer<RegistrateLootTables>) action);
    }

    public void addLootAction(ContextKeySet set, Consumer<BiConsumer<ResourceKey<LootTable>, LootTable.Builder>> action) {
        this.lootActions.put(set, action);
    }

    private LootTableSubProvider getLootCreator(HolderLookup. Provider provider, AbstractRegistrate<?> parent, LootType<?> type, FabricDataOutput output) {
        RegistrateLootTables creator = type.getLootCreator(provider, parent, cons -> specialLootActions.get(type).forEach(c -> c.accept(cons)), output);
        return creator;
    }

    public List<LootTableProvider.SubProviderEntry> getTables(FabricDataOutput output) {
        parent.genData(ProviderType.LOOT, this);
        ImmutableList.Builder<LootTableProvider.SubProviderEntry> builder = ImmutableList.builder();
        for (LootType<?> type : LOOT_TYPES.values()) {
            builder.add(new SubProviderEntry(provider -> getLootCreator(provider, parent, type, output), type.getLootSet()));
        }
        for (ContextKeySet set : List.of(LootContextParamSets.BLOCK, LootContextParamSets.ENTITY)) {
            builder.add(new SubProviderEntry((provider) -> callback -> lootActions.get(set).forEach(a -> a.accept(callback)), set));
        }
        return builder.build();
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        subProviders.clear();
        subProviders.addAll(getTables(output));
        return super.run(cache);
    }
}
