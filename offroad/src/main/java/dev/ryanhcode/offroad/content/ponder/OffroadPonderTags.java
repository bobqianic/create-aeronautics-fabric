package dev.ryanhcode.offroad.content.ponder;

import com.zurrtum.create.catnip.registry.RegisteredObjectsHelper;
import com.zurrtum.create.client.ponder.api.registration.PonderTagRegistrationHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ItemLike;

public class OffroadPonderTags {

    public static void register(final PonderTagRegistrationHelper<ResourceLocation> helper) {
        final PonderTagRegistrationHelper<ItemLike> itemHelper = helper.withKeyFunction(
                RegisteredObjectsHelper::getKeyOrThrow);
    }

}
