package dev.simulated_team.simulated.content.display_sources;

import com.zurrtum.create.content.redstone.displayLink.DisplayLinkContext;
import com.zurrtum.create.content.redstone.displayLink.source.NumericSingleLineDisplaySource;
import com.zurrtum.create.client.foundation.gui.ModularGuiLineBuilder;
import net.minecraft.network.chat.Component;

import java.util.List;

public abstract class AbstractNumericDisplaysource extends NumericSingleLineDisplaySource implements ConfigurableDisplaySource {

    @Override
    public void initConfigurationWidgets(final DisplayLinkContext context, final ModularGuiLineBuilder builder, final boolean isFirstLine) {
        if (isFirstLine) {
            return;
        }

        builder.addSelectionScrollInput(0, this.getWidth(), ((selectionScrollInput, label) -> selectionScrollInput.forOptions(this.getOptions())), this.getSelectionKey());
    }

    public int getWidth() {
        return 100;
    }

    abstract List<Component> getOptions();
    abstract String getKey();
    abstract String getSelectionKey();

    @Override
    protected String getTranslationKey() {
        return this.getKey();
    }

    @Override
    public boolean allowsLabeling(final DisplayLinkContext displayLinkContext) {
        return true;
    }
}
