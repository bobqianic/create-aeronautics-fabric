package dev.ryanhcode.offroad.content.ponder;

import com.zurrtum.create.client.foundation.ponder.CreatePonderPlugin;
import dev.ryanhcode.offroad.Offroad;
import dev.ryanhcode.offroad.index.OffroadPonderScenes;
import com.zurrtum.create.client.ponder.api.level.PonderLevel;
import com.zurrtum.create.client.ponder.api.registration.IndexExclusionHelper;
import com.zurrtum.create.client.ponder.api.registration.PonderSceneRegistrationHelper;
import com.zurrtum.create.client.ponder.api.registration.PonderTagRegistrationHelper;
import com.zurrtum.create.client.ponder.api.registration.SharedTextRegistrationHelper;
import net.minecraft.resources.ResourceLocation;

public class OffroadPonderPlugin extends CreatePonderPlugin {
    public OffroadPonderPlugin() {
    }

    public String getModId() {
        return Offroad.MOD_ID;
    }

    @Override
    public void registerScenes(final PonderSceneRegistrationHelper<ResourceLocation> helper) {
        OffroadPonderScenes.register(helper);
    }

    @Override
    public void registerTags(final PonderTagRegistrationHelper<ResourceLocation> helper) {
        OffroadPonderTags.register(helper);
    }

    @Override
    public void registerSharedText(final SharedTextRegistrationHelper helper) {

    }

    @Override
    public void onPonderLevelRestore(final PonderLevel ponderLevel) {

    }

    @Override
    public void indexExclusions(final IndexExclusionHelper helper) {

    }
}
