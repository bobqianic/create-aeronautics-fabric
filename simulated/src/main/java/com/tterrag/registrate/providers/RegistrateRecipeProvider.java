package com.tterrag.registrate.providers;

import com.google.common.collect.ImmutableMap;
import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.util.DataIngredient;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import io.github.fabricators_of_create.porting_lib.tags.Tags;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.EnterBlockTrigger;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.BlockFamily;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.data.recipes.SingleItemRecipeBuilder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.BlastingRecipe;
import net.minecraft.world.item.crafting.CampfireCookingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.item.crafting.SmokingRecipe;
import net.minecraft.world.item.equipment.trim.TrimPattern;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SuspiciousEffectHolder;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 * Registrate's recipe runner and callback facade for the post-1.21 recipe API.
 * Vanilla split recipe generation into a {@link RecipeProvider.Runner} and a
 * registry-aware {@link RecipeProvider}; this class keeps Registrate's existing
 * callback type while delegating the actual generation to the latter.
 */
public class RegistrateRecipeProvider extends RecipeProvider.Runner implements RegistrateProvider, RecipeOutput {
    private final AbstractRegistrate<?> owner;

    @Nullable
    private RecipeOutput output;
    @Nullable
    private Delegate delegate;

    public RegistrateRecipeProvider(AbstractRegistrate<?> owner, FabricDataOutput output,
                                    CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
        this.owner = owner;
    }

    @Override
    protected RecipeProvider createRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
        this.output = output;
        this.delegate = new Delegate(registries);
        return delegate;
    }

    private final class Delegate extends RecipeProvider {
        private Delegate(HolderLookup.Provider registries) {
            super(registries, RegistrateRecipeProvider.this);
        }

        @Override
        public void buildRecipes() {
            try {
                owner.genData(ProviderType.RECIPE, RegistrateRecipeProvider.this);
            } finally {
                RegistrateRecipeProvider.this.output = null;
                RegistrateRecipeProvider.this.delegate = null;
            }
        }

        private HolderLookup.Provider registries() {
            return registries;
        }
    }

    private Delegate delegate() {
        if (delegate == null) {
            throw new IllegalStateException("Recipe helpers can only be used while Registrate recipes are being generated");
        }
        return delegate;
    }

    private RecipeOutput output() {
        if (output == null) {
            throw new IllegalStateException("Recipe output is only available while Registrate recipes are being generated");
        }
        return output;
    }

    public HolderLookup.Provider registries() {
        return delegate().registries();
    }

    public HolderLookup<Item> itemLookup() {
        return registries().lookupOrThrow(Registries.ITEM);
    }

    public HolderLookup<Block> blockLookup() {
        return registries().lookupOrThrow(Registries.BLOCK);
    }

    public <T> Holder<T> resolve(ResourceKey<T> key) {
        return registries().lookupOrThrow(key.registryKey()).getOrThrow(key);
    }

    @Override
    public void accept(ResourceKey<Recipe<?>> id, Recipe<?> recipe, @Nullable AdvancementHolder advancement) {
        output().accept(id, recipe, advancement);
    }

    @Override
    public Advancement.Builder advancement() {
        return output().advancement();
    }

    @Override
    public void includeRootAdvancement() {
        output().includeRootAdvancement();
    }

    @Override
    public String getName() {
        return "Registrate recipes for " + owner.getModid();
    }

    @Override
    public EnvType getSide() {
        return EnvType.SERVER;
    }

    public ResourceLocation safeId(ResourceLocation id) {
        return ResourceLocation.fromNamespaceAndPath(owner.getModid(), safeName(id));
    }

    public ResourceLocation safeId(DataIngredient source) {
        return safeId(source.getId());
    }

    public ResourceLocation safeId(ItemLike registryEntry) {
        return safeId(BuiltInRegistries.ITEM.getKey(registryEntry.asItem()));
    }

    public ResourceKey<Recipe<?>> safeKey(ResourceLocation id) {
        return ResourceKey.create(Registries.RECIPE, safeId(id));
    }

    public ResourceKey<Recipe<?>> safeKey(DataIngredient source) {
        return safeKey(source.getId());
    }

    public ResourceKey<Recipe<?>> safeKey(ItemLike registryEntry) {
        return safeKey(BuiltInRegistries.ITEM.getKey(registryEntry.asItem()));
    }

    public String safeName(ResourceLocation id) {
        return id.getPath().replace('/', '_');
    }

    public String safeName(DataIngredient source) {
        return safeName(source.getId());
    }

    public String safeName(ItemLike registryEntry) {
        return safeName(BuiltInRegistries.ITEM.getKey(registryEntry.asItem()));
    }

    public static final int DEFAULT_SMELT_TIME = 200;
    public static final int DEFAULT_BLAST_TIME = DEFAULT_SMELT_TIME / 2;
    public static final int DEFAULT_SMOKE_TIME = DEFAULT_BLAST_TIME;
    public static final int DEFAULT_CAMPFIRE_TIME = DEFAULT_SMELT_TIME * 3;

    private static final ImmutableMap<RecipeSerializer<? extends AbstractCookingRecipe>, String> COOKING_TYPE_NAMES =
            ImmutableMap.<RecipeSerializer<? extends AbstractCookingRecipe>, String>builder()
                    .put(RecipeSerializer.SMELTING_RECIPE, "smelting")
                    .put(RecipeSerializer.BLASTING_RECIPE, "blasting")
                    .put(RecipeSerializer.SMOKING_RECIPE, "smoking")
                    .put(RecipeSerializer.CAMPFIRE_COOKING_RECIPE, "campfire")
                    .build();

    public <T extends ItemLike, S extends AbstractCookingRecipe> void cooking(
            DataIngredient source, RecipeCategory category, Supplier<? extends T> result,
            float experience, int cookingTime, RecipeSerializer<S> serializer,
            AbstractCookingRecipe.Factory<S> factory) {
        cooking(source, category, result, experience, cookingTime,
                COOKING_TYPE_NAMES.get(serializer), serializer, factory);
    }

    public <T extends ItemLike, S extends AbstractCookingRecipe> void cooking(
            DataIngredient source, RecipeCategory category, Supplier<? extends T> result,
            float experience, int cookingTime, String typeName, RecipeSerializer<S> serializer,
            AbstractCookingRecipe.Factory<S> factory) {
        SimpleCookingRecipeBuilder.generic(source.toVanilla(this), category, result.get(), experience,
                        cookingTime, serializer, factory)
                .unlockedBy("has_" + safeName(source), source.getCriterion(this))
                .save(this, safeId(result.get()) + "_from_" + safeName(source) + "_" + typeName);
    }

    public <T extends ItemLike> void smelting(DataIngredient source, RecipeCategory category,
                                               Supplier<? extends T> result, float experience) {
        smelting(source, category, result, experience, DEFAULT_SMELT_TIME);
    }

    public <T extends ItemLike> void smelting(DataIngredient source, RecipeCategory category,
                                               Supplier<? extends T> result, float experience, int cookingTime) {
        cooking(source, category, result, experience, cookingTime, RecipeSerializer.SMELTING_RECIPE, SmeltingRecipe::new);
    }

    public <T extends ItemLike> void blasting(DataIngredient source, RecipeCategory category,
                                               Supplier<? extends T> result, float experience) {
        blasting(source, category, result, experience, DEFAULT_BLAST_TIME);
    }

    public <T extends ItemLike> void blasting(DataIngredient source, RecipeCategory category,
                                               Supplier<? extends T> result, float experience, int cookingTime) {
        cooking(source, category, result, experience, cookingTime, RecipeSerializer.BLASTING_RECIPE, BlastingRecipe::new);
    }

    public <T extends ItemLike> void smoking(DataIngredient source, RecipeCategory category,
                                              Supplier<? extends T> result, float experience) {
        smoking(source, category, result, experience, DEFAULT_SMOKE_TIME);
    }

    public <T extends ItemLike> void smoking(DataIngredient source, RecipeCategory category,
                                              Supplier<? extends T> result, float experience, int cookingTime) {
        cooking(source, category, result, experience, cookingTime, RecipeSerializer.SMOKING_RECIPE, SmokingRecipe::new);
    }

    public <T extends ItemLike> void campfire(DataIngredient source, RecipeCategory category,
                                               Supplier<? extends T> result, float experience) {
        campfire(source, category, result, experience, DEFAULT_CAMPFIRE_TIME);
    }

    public <T extends ItemLike> void campfire(DataIngredient source, RecipeCategory category,
                                               Supplier<? extends T> result, float experience, int cookingTime) {
        cooking(source, category, result, experience, cookingTime, RecipeSerializer.CAMPFIRE_COOKING_RECIPE,
                CampfireCookingRecipe::new);
    }

    public <T extends ItemLike> void stonecutting(DataIngredient source, RecipeCategory category,
                                                   Supplier<? extends T> result) {
        stonecutting(source, category, result, 1);
    }

    public <T extends ItemLike> void stonecutting(DataIngredient source, RecipeCategory category,
                                                   Supplier<? extends T> result, int resultAmount) {
        SingleItemRecipeBuilder.stonecutting(source.toVanilla(this), category, result.get(), resultAmount)
                .unlockedBy("has_" + safeName(source), source.getCriterion(this))
                .save(this, safeId(result.get()) + "_from_" + safeName(source) + "_stonecutting");
    }

    public <T extends ItemLike> void smeltingAndBlasting(DataIngredient source, RecipeCategory category,
                                                          Supplier<? extends T> result, float experience) {
        smelting(source, category, result, experience);
        blasting(source, category, result, experience);
    }

    public <T extends ItemLike> void food(DataIngredient source, RecipeCategory category,
                                          Supplier<? extends T> result, float experience) {
        smelting(source, category, result, experience);
        smoking(source, category, result, experience);
        campfire(source, category, result, experience);
    }

    public <T extends ItemLike> void square(DataIngredient source, RecipeCategory category,
                                            Supplier<? extends T> result, boolean small) {
        ShapedRecipeBuilder builder = shaped(category, result.get()).define('X', source.toVanilla(this));
        if (small) {
            builder.pattern("XX").pattern("XX");
        } else {
            builder.pattern("XXX").pattern("XXX").pattern("XXX");
        }
        builder.unlockedBy("has_" + safeName(source), source.getCriterion(this)).save(this, safeKey(result.get()));
    }

    /** @deprecated Use one of the overloads that supplies both the packed and unpacked ingredient. */
    @Deprecated
    public <T extends ItemLike> void storage(DataIngredient source, RecipeCategory category,
                                              NonNullSupplier<? extends T> output) {
        square(source, category, output, false);
        singleItemUnfinished(source, category, output, 1, 9)
                .save(this, safeId(source) + "_from_" + safeName(output.get()));
    }

    public <T extends ItemLike> void storage(NonNullSupplier<? extends T> source, RecipeCategory category,
                                              NonNullSupplier<? extends T> output) {
        storage(DataIngredient.items(source), category, source, DataIngredient.items(output), output);
    }

    public <T extends ItemLike> void storage(DataIngredient sourceIngredient, RecipeCategory category,
                                              NonNullSupplier<? extends T> source, DataIngredient outputIngredient,
                                              NonNullSupplier<? extends T> output) {
        square(sourceIngredient, category, output, false);
        singleItemUnfinished(outputIngredient, category, source, 1, 9)
                .save(this, safeId(sourceIngredient) + "_from_" + safeName(output.get()));
    }

    public <T extends ItemLike> ShapelessRecipeBuilder singleItemUnfinished(
            DataIngredient source, RecipeCategory category, Supplier<? extends T> result, int required, int amount) {
        return shapeless(category, result.get(), amount)
                .requires(source.toVanilla(this), required)
                .unlockedBy("has_" + safeName(source), source.getCriterion(this));
    }

    public <T extends ItemLike> void singleItem(DataIngredient source, RecipeCategory category,
                                                 Supplier<? extends T> result, int required, int amount) {
        singleItemUnfinished(source, category, result, required, amount).save(this, safeKey(result.get()));
    }

    public <T extends ItemLike> void planks(DataIngredient source, RecipeCategory category,
                                            Supplier<? extends T> result) {
        singleItemUnfinished(source, category, result, 1, 4).group("planks").save(this, safeKey(result.get()));
    }

    public <T extends ItemLike> void stairs(DataIngredient source, RecipeCategory category,
                                            Supplier<? extends T> result, @Nullable String group, boolean stone) {
        shaped(category, result.get(), 4)
                .pattern("X  ").pattern("XX ").pattern("XXX")
                .define('X', source.toVanilla(this)).group(group)
                .unlockedBy("has_" + safeName(source), source.getCriterion(this)).save(this, safeKey(result.get()));
        if (stone) stonecutting(source, category, result);
    }

    public <T extends ItemLike> void slab(DataIngredient source, RecipeCategory category,
                                          Supplier<? extends T> result, @Nullable String group, boolean stone) {
        shaped(category, result.get(), 6)
                .pattern("XXX").define('X', source.toVanilla(this)).group(group)
                .unlockedBy("has_" + safeName(source), source.getCriterion(this)).save(this, safeKey(result.get()));
        if (stone) stonecutting(source, category, result, 2);
    }

    public <T extends ItemLike> void fence(DataIngredient source, RecipeCategory category,
                                           Supplier<? extends T> result, @Nullable String group) {
        shaped(category, result.get(), 3)
                .pattern("W#W").pattern("W#W")
                .define('W', source.toVanilla(this)).define('#', Tags.Items.RODS_WOODEN).group(group)
                .unlockedBy("has_" + safeName(source), source.getCriterion(this)).save(this, safeKey(result.get()));
    }

    public <T extends ItemLike> void fenceGate(DataIngredient source, RecipeCategory category,
                                               Supplier<? extends T> result, @Nullable String group) {
        shaped(category, result.get())
                .pattern("#W#").pattern("#W#")
                .define('W', source.toVanilla(this)).define('#', Tags.Items.RODS_WOODEN).group(group)
                .unlockedBy("has_" + safeName(source), source.getCriterion(this)).save(this, safeKey(result.get()));
    }

    public <T extends ItemLike> void wall(DataIngredient source, RecipeCategory category,
                                          Supplier<? extends T> result) {
        shaped(category, result.get(), 6)
                .pattern("XXX").pattern("XXX").define('X', source.toVanilla(this))
                .unlockedBy("has_" + safeName(source), source.getCriterion(this)).save(this, safeKey(result.get()));
        stonecutting(source, category, result);
    }

    public <T extends ItemLike> void door(DataIngredient source, RecipeCategory category,
                                          Supplier<? extends T> result, @Nullable String group) {
        shaped(category, result.get(), 3)
                .pattern("XX").pattern("XX").pattern("XX")
                .define('X', source.toVanilla(this)).group(group)
                .unlockedBy("has_" + safeName(source), source.getCriterion(this)).save(this, safeKey(result.get()));
    }

    public <T extends ItemLike> void trapDoor(DataIngredient source, RecipeCategory category,
                                              Supplier<? extends T> result, @Nullable String group) {
        shaped(category, result.get(), 2)
                .pattern("XXX").pattern("XXX").define('X', source.toVanilla(this)).group(group)
                .unlockedBy("has_" + safeName(source), source.getCriterion(this)).save(this, safeKey(result.get()));
    }

    public Ingredient tag(TagKey<Item> tag) {
        return delegate().tag(tag);
    }

    public ShapedRecipeBuilder shaped(RecipeCategory category, ItemLike result) {
        return delegate().shaped(category, result);
    }

    public ShapedRecipeBuilder shaped(RecipeCategory category, ItemLike result, int count) {
        return delegate().shaped(category, result, count);
    }

    public ShapelessRecipeBuilder shapeless(RecipeCategory category, ItemStack result) {
        return delegate().shapeless(category, result);
    }

    public ShapelessRecipeBuilder shapeless(RecipeCategory category, ItemLike result) {
        return delegate().shapeless(category, result);
    }

    public ShapelessRecipeBuilder shapeless(RecipeCategory category, ItemLike result, int count) {
        return delegate().shapeless(category, result, count);
    }

    public static Criterion<EnterBlockTrigger.TriggerInstance> insideOf(Block block) {
        return RecipeProvider.insideOf(block);
    }

    public Criterion<InventoryChangeTrigger.TriggerInstance> has(MinMaxBounds.Ints count, ItemLike item) {
        return delegate().has(count, item);
    }

    public static Criterion<InventoryChangeTrigger.TriggerInstance> has(ItemLike item) {
        return InventoryChangeTrigger.TriggerInstance.hasItems(item);
    }

    public Criterion<InventoryChangeTrigger.TriggerInstance> has(TagKey<Item> tag) {
        return delegate().has(tag);
    }

    public static Criterion<InventoryChangeTrigger.TriggerInstance> inventoryTrigger(ItemPredicate.Builder... items) {
        return RecipeProvider.inventoryTrigger(items);
    }

    public static Criterion<InventoryChangeTrigger.TriggerInstance> inventoryTrigger(ItemPredicate... predicates) {
        return RecipeProvider.inventoryTrigger(predicates);
    }

    public static String getHasName(ItemLike item) {
        return RecipeProvider.getHasName(item);
    }

    public static String getItemName(ItemLike item) {
        return RecipeProvider.getItemName(item);
    }

    public static String getSimpleRecipeName(ItemLike item) {
        return RecipeProvider.getSimpleRecipeName(item);
    }

    public static String getConversionRecipeName(ItemLike result, ItemLike ingredient) {
        return RecipeProvider.getConversionRecipeName(result, ingredient);
    }

    public static String getSmeltingRecipeName(ItemLike item) {
        return RecipeProvider.getSmeltingRecipeName(item);
    }

    public static String getBlastingRecipeName(ItemLike item) {
        return RecipeProvider.getBlastingRecipeName(item);
    }

    /** Gives advanced callers access to the complete vanilla 1.21.10 recipe helper surface. */
    public RecipeProvider vanilla() {
        return delegate();
    }

    public void generateForEnabledBlockFamilies(FeatureFlagSet enabledFeatures) {
        delegate().generateForEnabledBlockFamilies(enabledFeatures);
    }

    public void generateRecipes(BlockFamily family, FeatureFlagSet enabledFeatures) {
        delegate().generateRecipes(family, enabledFeatures);
    }

    public RecipeBuilder buttonBuilder(ItemLike button, Ingredient material) {
        return delegate().buttonBuilder(button, material);
    }

    public RecipeBuilder doorBuilder(ItemLike door, Ingredient material) {
        return delegate().doorBuilder(door, material);
    }

    public void netheriteSmithing(Item ingredient, RecipeCategory category, Item result) {
        delegate().netheriteSmithing(ingredient, category, result);
    }

    public void trimSmithing(Item template, ResourceKey<TrimPattern> pattern, ResourceKey<Recipe<?>> recipe) {
        delegate().trimSmithing(template, pattern, recipe);
    }

    public void suspiciousStew(Item flower, SuspiciousEffectHolder effect) {
        delegate().suspiciousStew(flower, effect);
    }

    public void waxRecipes(FeatureFlagSet enabledFeatures) {
        delegate().waxRecipes(enabledFeatures);
    }
}
