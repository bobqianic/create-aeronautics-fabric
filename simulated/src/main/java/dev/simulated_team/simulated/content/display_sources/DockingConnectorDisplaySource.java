package dev.simulated_team.simulated.content.display_sources;

import com.zurrtum.create.content.redstone.displayLink.DisplayLinkContext;
import com.zurrtum.create.content.redstone.displayLink.source.SingleLineDisplaySource;
import com.zurrtum.create.content.redstone.displayLink.target.DisplayTargetStats;
import com.zurrtum.create.client.foundation.gui.ModularGuiLineBuilder;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.api.SubLevelHelper;
import dev.ryanhcode.sable.sublevel.SubLevel;
import dev.simulated_team.simulated.content.blocks.docking_connector.DockingConnectorBlockEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class DockingConnectorDisplaySource extends SingleLineDisplaySource implements ConfigurableDisplaySource {

    @Override
    protected MutableComponent provideLine(final DisplayLinkContext context, final DisplayTargetStats stats) {
        if (!(context.getSourceBlockEntity() instanceof final DockingConnectorBlockEntity be)) {
            return EMPTY_LINE.copy();
        }
        final DockingConnectorBlockEntity otherConnector = be.getOtherConnector();

        if (otherConnector != null) {
            final SubLevel otherSubLevel = Sable.HELPER.getContaining(otherConnector);

            if (otherSubLevel != null) {
                final String name = otherSubLevel.getName();
                return name != null ? Component.literal(name) : EMPTY_LINE.copy();
            }
        }

        return EMPTY_LINE.copy();
    }

    @Override
    public void initConfigurationWidgets(final DisplayLinkContext context, final ModularGuiLineBuilder builder, final boolean isFirstLine) {
    }

    @Override
    protected String getTranslationKey() {
        return "sublevel_name";
    }

    @Override
    public boolean allowsLabeling(final DisplayLinkContext context) {
        return true;
    }
}
