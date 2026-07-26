package dev.simulated_team.simulated.mixin_interface;

import net.minecraft.core.BlockPos;

public interface PlayerTypewriterExtension {

    BlockPos simulated$getCurrentTypewriter();
    void simulated$setCurrentTypewriter(BlockPos pos);
}
