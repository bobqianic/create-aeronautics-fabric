package dev.simulated_team.simulated.ponder.instructions;

import dev.simulated_team.simulated.mixin_interface.ponder.PonderSceneExtension;
import com.zurrtum.create.client.ponder.foundation.PonderScene;
import com.zurrtum.create.client.ponder.foundation.instruction.PonderInstruction;

public class CustomToggleBaseShadowInstruction extends PonderInstruction {
    @Override
    public boolean isComplete() {
        return true;
    }

    @Override
    public void tick(final PonderScene scene) {
        ((PonderSceneExtension)scene).simulated$toggleRenderBasePlateShadow();
    }
}
