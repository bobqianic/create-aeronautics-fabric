package dev.eriksonn.aeronautics.content.ponder;

import com.zurrtum.create.client.foundation.ponder.CreatePonderPlugin;
import dev.eriksonn.aeronautics.Aeronautics;
import dev.eriksonn.aeronautics.index.AeroPonderScenes;
import com.zurrtum.create.client.ponder.api.level.PonderLevel;
import com.zurrtum.create.client.ponder.api.registration.IndexExclusionHelper;
import com.zurrtum.create.client.ponder.api.registration.PonderSceneRegistrationHelper;
import com.zurrtum.create.client.ponder.api.registration.PonderTagRegistrationHelper;
import com.zurrtum.create.client.ponder.api.registration.SharedTextRegistrationHelper;
import net.minecraft.resources.ResourceLocation;

public class AeroPonderPlugin extends CreatePonderPlugin {
    public AeroPonderPlugin() {
    }

    public String getModId() {
        return Aeronautics.MOD_ID;
    }

    @Override
    public void registerScenes(final PonderSceneRegistrationHelper<ResourceLocation> helper) {
        AeroPonderScenes.register(helper);
    }

    @Override
    public void registerTags(final PonderTagRegistrationHelper<ResourceLocation> helper) {
        AeroPonderTags.register(helper);
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
