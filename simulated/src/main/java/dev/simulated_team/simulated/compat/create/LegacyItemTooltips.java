package dev.simulated_team.simulated.compat.create;

import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.util.entry.RegistryEntry;
import com.zurrtum.create.client.catnip.lang.FontHelper;
import com.zurrtum.create.client.foundation.item.ItemDescription;
import com.zurrtum.create.client.foundation.item.KineticStats;
import com.zurrtum.create.client.foundation.item.TooltipHelper;
import com.zurrtum.create.client.foundation.item.TooltipModifier;
import dev.simulated_team.simulated.util.SimColors;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;

/**
 * Restores the item-tooltip registration formerly supplied by CreateRegistrate.
 */
public final class LegacyItemTooltips {
    private LegacyItemTooltips() {
    }

    public static void register(final AbstractRegistrate<?> registrate) {
        for (final RegistryEntry<Item, Item> entry : registrate.<Item, Item>getAll(Registries.ITEM)) {
            final Item item = entry.get();
            if (TooltipModifier.REGISTRY.get(item) != null) {
                continue;
            }

            final Rarity rarity = item.getDefaultInstance().getRarity();
            FontHelper.Palette color = FontHelper.Palette.STANDARD_CREATE;
            if (rarity == Rarity.EPIC) {
                color = new FontHelper.Palette(
                        TooltipHelper.styleFromColor(SimColors.EPIC_OURPLE),
                        TooltipHelper.styleFromColor(rarity.color())
                );
            }

            TooltipModifier.REGISTRY.register(
                    item,
                    new ItemDescription.Modifier(item, color)
                            .andThen(TooltipModifier.mapNull(KineticStats.create(item)))
            );
        }
    }
}
