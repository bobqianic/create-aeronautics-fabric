package dev.simulated_team.simulated.fabric.compat.jei;

import dev.simulated_team.simulated.Simulated;
import dev.simulated_team.simulated.index.SimBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.ShapelessRecipe;

import java.util.Arrays;
import java.util.List;

public final class PortableEngineDyeingRecipeMaker {

    private static final String GROUP = "simulated.portable_engine.color";

    public static List<RecipeHolder<CraftingRecipe>> createRecipes() {
        final Ingredient redEngine = Ingredient.of(
                SimBlocks.PORTABLE_ENGINES.get(DyeColor.RED).asItem());

        return Arrays.stream(DyeColor.values())
                .filter(color -> color != DyeColor.RED)
                .map(color -> createRecipe(color, redEngine))
                .toList();
    }

    private static RecipeHolder<CraftingRecipe> createRecipe(
            final DyeColor color, final Ingredient redEngine) {
        final DyeItem dye = DyeItem.byColor(color);
        final ItemStack output = SimBlocks.PORTABLE_ENGINES.get(color)
                .asItem()
                .getDefaultInstance();
        final ResourceLocation id = Simulated.path(
                "jei/portable_engine_dyeing/" + color.getSerializedName());
        final ResourceKey<Recipe<?>> recipeKey =
                ResourceKey.create(Registries.RECIPE, id);
        final CraftingRecipe recipe = new ShapelessRecipe(
                GROUP,
                CraftingBookCategory.MISC,
                output,
                List.of(redEngine, Ingredient.of(dye)));

        return new RecipeHolder<>(recipeKey, recipe);
    }

    private PortableEngineDyeingRecipeMaker() {
    }
}
