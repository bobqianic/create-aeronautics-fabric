package dev.simulated_team.simulated.compat.jei;

import com.zurrtum.create.client.compat.jei.GhostIngredientHandler;
import dev.simulated_team.simulated.Simulated;
import dev.simulated_team.simulated.client.SearchAlias;
import dev.simulated_team.simulated.content.blocks.redstone.linked_typewriter.screen.LinkedTypewriterScreen;
import dev.simulated_team.simulated.index.SimResourceManagers;
import dev.simulated_team.simulated.registrate.SimulatedRegistrate;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IIngredientAliasRegistration;
import mezz.jei.api.registration.IModInfoRegistration;
import mezz.jei.library.ingredients.itemStacks.TypedItemStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@JeiPlugin
@SuppressWarnings("unused")
@ParametersAreNonnullByDefault
public class SimulatedJEI implements IModPlugin {

    private static final ResourceLocation ID = Simulated.path("jei_plugin");

    @Override
    public ResourceLocation getPluginUid() {
        return ID;
    }

    @Override
    public void registerGuiHandlers(final IGuiHandlerRegistration registration) {
        IModPlugin.super.registerGuiHandlers(registration);

        registration.addGhostIngredientHandler(LinkedTypewriterScreen.class, new GhostIngredientHandler());

    }

    @Override
    public void registerModInfo(final IModInfoRegistration modAliasRegistration) {
        for (final String mod : SimulatedRegistrate.MODS) {
            for (String otherMod : SimulatedRegistrate.MODS.stream().filter(v -> !v.equals(mod)).toList()) {
                modAliasRegistration.addModAliases(mod, otherMod);
            }
        }
    }

    @Override
    public void registerIngredientAliases(final IIngredientAliasRegistration registration) {
        for (final SearchAlias searchAlias : SimResourceManagers.SEARCH_ALIAS.entries()) {
            final List<ITypedIngredient<ItemStack>> ingredients = searchAlias.getItems().stream().map(TypedItemStack::create).toList();
            registration.addAliases(ingredients, searchAlias.terms());
        }
    }
}
