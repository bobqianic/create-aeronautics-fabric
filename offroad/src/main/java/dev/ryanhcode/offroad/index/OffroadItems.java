package dev.ryanhcode.offroad.index;

import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.foundation.data.AssetLookup;
import dev.simulated_team.simulated.registrate.SimulatedRegistrate;
import com.tterrag.registrate.providers.RegistrateRecipeProvider;
import dev.ryanhcode.offroad.Offroad;
import dev.ryanhcode.offroad.content.components.TireLike;
import dev.ryanhcode.offroad.content.items.tire.TireItem;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

public class OffroadItems {
    private static final SimulatedRegistrate REGISTRATE = Offroad.getRegistrate();

    static {
        REGISTRATE.item("small_tire", TireItem::new)
                .properties(x -> x.component(OffroadDataComponents.TIRE, TireLike.SMALL_TIRE))
                .recipe((c, p) -> p.shapeless(RecipeCategory.MISC, c.get(), 1)
                        .requires(AllBlocks.SHAFT)
                        .requires(Items.DRIED_KELP)
                        .unlockedBy("has_ingredient", RegistrateRecipeProvider.has(AllBlocks.SHAFT))
                        .save(p))
                .model(AssetLookup.itemModelWithPartials())
                .register();

        REGISTRATE.item("tire", TireItem::new)
                .properties(x -> x.component(OffroadDataComponents.TIRE, TireLike.TIRE))
                .recipe((c, p) -> p.shaped(RecipeCategory.MISC, c.get(), 1)
                        .pattern(" K ")
                        .pattern("KSK")
                        .pattern(" K ")
                        .define('K', Items.DRIED_KELP.asItem())
                        .define('S', AllBlocks.SHAFT.asItem())
                        .unlockedBy("has_ingredient", RegistrateRecipeProvider.has(AllBlocks.SHAFT))
                        .save(p))
                .model(AssetLookup.itemModelWithPartials())
                .register();

        REGISTRATE.item("large_tire", TireItem::new)
                .properties(x -> x.component(OffroadDataComponents.TIRE, TireLike.LARGE_TIRE))
                .recipe((c, p) -> p.shaped(RecipeCategory.MISC, c.get(), 1)
                        .pattern(" B ")
                        .pattern("BSB")
                        .pattern(" B ")
                        .define('B', AllBlocks.BELT.asItem())
                        .define('S', AllBlocks.SHAFT.asItem())
                        .unlockedBy("has_ingredient", RegistrateRecipeProvider.has(AllBlocks.SHAFT))
                        .save(p))
                .model(AssetLookup.itemModelWithPartials())
                .register();

        REGISTRATE.item("monstrous_tire", TireItem::new)
                .properties(x -> x.component(OffroadDataComponents.TIRE, TireLike.MONSTROUS_TIRE))
                .recipe((c, p) -> p.shaped(RecipeCategory.MISC, c.get(), 1)
                        .pattern(" K ")
                        .pattern("KSK")
                        .pattern(" K ")
                        .define('K', Blocks.DRIED_KELP_BLOCK.asItem())
                        .define('S', AllBlocks.SHAFT.asItem())
                        .unlockedBy("has_ingredient", RegistrateRecipeProvider.has(AllBlocks.SHAFT))
                        .save(p))
                .model(AssetLookup.itemModelWithPartials())
                .register();
    }

    public static void init() {
    }
}
