package dev.simulated_team.simulated.content.display_sources;

import com.zurrtum.create.client.foundation.gui.ModularGuiLineBuilder;
import com.zurrtum.create.content.redstone.displayLink.DisplayLinkContext;

public interface ConfigurableDisplaySource {
    void initConfigurationWidgets(DisplayLinkContext context, ModularGuiLineBuilder builder, boolean isFirstLine);
}
