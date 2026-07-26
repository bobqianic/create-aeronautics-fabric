package dev.ryanhcode.offroad.index;

import com.tterrag.registrate.util.entry.ItemProviderEntry;
import dev.ryanhcode.offroad.Offroad;
import dev.ryanhcode.offroad.content.ponder.scenes.BoreheadBearingScenes;
import com.zurrtum.create.client.ponder.api.registration.PonderSceneRegistrationHelper;
import net.minecraft.resources.ResourceLocation;

public class OffroadPonderScenes {

    public static void register(final PonderSceneRegistrationHelper<ResourceLocation> registry) {
        final PonderSceneRegistrationHelper<ItemProviderEntry<?, ?>> helper = registry.withKeyFunction(ItemProviderEntry::getId);

        helper.forComponents(OffroadBlocks.BOREHEAD_BEARING_BLOCK, OffroadBlocks.ROCK_CUTTER_BLOCK)
                .addStoryBoard("borehead_bearing/intro", BoreheadBearingScenes::boreheadIntro)
                .addStoryBoard("borehead_bearing/excavating", BoreheadBearingScenes::boreheadExcavating)
                .addStoryBoard("borehead_bearing/efficiency", BoreheadBearingScenes::boreheadEfficiency);
    }
}
