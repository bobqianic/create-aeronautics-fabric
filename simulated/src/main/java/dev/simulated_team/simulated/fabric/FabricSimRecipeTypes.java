package dev.simulated_team.simulated.fabric;

import dev.simulated_team.simulated.Simulated;
import dev.simulated_team.simulated.fabric.data.PortableEngineDyeingRecipe;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.CustomRecipe;

public final class FabricSimRecipeTypes {
    public static final RecipeSerializer<PortableEngineDyeingRecipe> PORTABLE_ENGINE_DYEING =
            new CustomRecipe.Serializer<>(PortableEngineDyeingRecipe::new);

    private FabricSimRecipeTypes() {
    }

    public static void register() {
        Registry.register(BuiltInRegistries.RECIPE_SERIALIZER,
                Simulated.path("portable_engine_dyeing"), PORTABLE_ENGINE_DYEING);
    }
}
