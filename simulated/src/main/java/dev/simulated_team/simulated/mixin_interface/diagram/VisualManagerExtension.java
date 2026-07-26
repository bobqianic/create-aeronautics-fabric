package dev.simulated_team.simulated.mixin_interface.diagram;

import dev.simulated_team.simulated.compat.flywheel.SubLevelEmbedding;
import dev.ryanhcode.sable.sublevel.ClientSubLevel;

public interface VisualManagerExtension {

    SubLevelEmbedding sable$getBEEmbeddingInfo(ClientSubLevel subLevel);
}
