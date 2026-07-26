package dev.simulated_team.simulated.fabric.service;

import com.zurrtum.create.AllItemTags;
import dev.simulated_team.simulated.service.SimItemService;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.FuelValues;

public final class FabricSimItemService implements SimItemService {
    private static final FuelValues FALLBACK_FUELS = FuelValues.vanillaBurnTimes(
            RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY), FeatureFlags.DEFAULT_FLAGS);
    private static volatile FuelValues serverFuels;

    public static void setServerFuels(final FuelValues fuelValues) {
        serverFuels = fuelValues;
    }

    @Override
    public int getBurnTime(final ItemStack stack) {
        final FuelValues fuels = serverFuels == null ? FALLBACK_FUELS : serverFuels;
        return fuels.burnDuration(stack);
    }

    @Override
    public int getSuperheatedBurnTime(final ItemStack stack) {
        return stack.getItemHolder().is(AllItemTags.BLAZE_BURNER_FUEL_SPECIAL) ? 3200 : 0;
    }
}
