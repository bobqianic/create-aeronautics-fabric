package com.tterrag.registrate.util;

import java.util.Arrays;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import com.google.common.collect.ObjectArrays;
import com.tterrag.registrate.providers.RegistrateRecipeProvider;
import com.tterrag.registrate.util.nullness.NonNullSupplier;

import lombok.Getter;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.Nullable;

/**
 * A helper for data generation when using ingredients as input(s) to recipes.<br>
 * It remembers the name of the primary ingredient for use in creating recipe names/criteria.
 * <p>
 * Create an instance of this class with the various factory methods such as {@link #items(ItemLike, ItemLike...)} and {@link #tag(TagKey)}.
 * <p>
 * <strong>This class should not be used for any purpose other than data generation</strong>, it will throw an exception if it is serialized to a packet buffer.
 */
public final class DataIngredient {
    private final Function<RegistrateRecipeProvider, Ingredient> ingredientFactory;
    @Nullable
    private final Ingredient immediateIngredient;
    @Getter
    private final ResourceLocation id;
    private final Function<RegistrateRecipeProvider, Criterion<InventoryChangeTrigger.TriggerInstance>> criteriaFactory;

    private DataIngredient(Ingredient parent, ItemLike item) {
        this.ingredientFactory = provider -> parent;
        this.immediateIngredient = parent;
        this.id = BuiltInRegistries.ITEM.getKey(item.asItem());
        this.criteriaFactory = prov -> RegistrateRecipeProvider.has(item);
    }
    
    private DataIngredient(TagKey<Item> tag) {
        this.ingredientFactory = provider -> provider.tag(tag);
        this.immediateIngredient = null;
        this.id = tag.location();
        this.criteriaFactory = prov -> prov.has(tag);
    }
    
    private DataIngredient(Ingredient parent, ResourceLocation id, ItemPredicate... predicates) {
        this.ingredientFactory = provider -> parent;
        this.immediateIngredient = parent;
        this.id = id;
        this.criteriaFactory = prov -> RegistrateRecipeProvider.inventoryTrigger(predicates);
    }

    public Criterion<InventoryChangeTrigger.TriggerInstance> getCriterion(RegistrateRecipeProvider prov) {
        return criteriaFactory.apply(prov);
    }
    
    @SuppressWarnings("unchecked")
    @SafeVarargs
    public static <T extends ItemLike> DataIngredient items(NonNullSupplier<? extends T> first, NonNullSupplier<? extends T>... others) {
        return items(first.get(), (T[]) Arrays.stream(others).map(Supplier::get).toArray(ItemLike[]::new));
    }

    @SafeVarargs
    public static <T extends ItemLike> DataIngredient items(T first, T... others) {
        return ingredient(Ingredient.of(ObjectArrays.concat(first, others)), first);
    }

    public static DataIngredient stacks(ItemStack first, ItemStack... others) {
        return ingredient(Ingredient.of(Stream.concat(Stream.of(first), Arrays.stream(others)).map(ItemStack::getItem)), first.getItem());
    }

    public static DataIngredient tag(TagKey<Item> tag) {
        return new DataIngredient(tag);
    }
    
    public static DataIngredient ingredient(Ingredient parent, ItemLike required) {
        return new DataIngredient(parent, required);
    }
    
    public static DataIngredient ingredient(Ingredient parent, TagKey<Item> required) {
        return new DataIngredient(required);
    }
    
    public static DataIngredient ingredient(Ingredient parent, ResourceLocation id, ItemPredicate... criteria) {
        return new DataIngredient(parent, id, criteria);
    }

    public Ingredient toVanilla(RegistrateRecipeProvider provider) {
        return ingredientFactory.apply(provider);
    }

    /**
     * Retained for source compatibility for item-backed ingredients. Tag-backed
     * ingredients require the registry lookup supplied by the recipe provider.
     */
    @Deprecated
    public Ingredient toVanilla() {
        if (immediateIngredient == null) {
            throw new IllegalStateException("Tag ingredients require toVanilla(RegistrateRecipeProvider)");
        }
        return immediateIngredient;
    }
}
