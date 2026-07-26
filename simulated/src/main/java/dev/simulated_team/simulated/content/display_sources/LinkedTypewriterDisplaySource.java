package dev.simulated_team.simulated.content.display_sources;

import com.zurrtum.create.content.redstone.displayLink.DisplayLinkContext;
import com.zurrtum.create.content.redstone.displayLink.source.SingleLineDisplaySource;
import com.zurrtum.create.content.redstone.displayLink.target.DisplayTargetStats;
import dev.simulated_team.simulated.content.blocks.redstone.linked_typewriter.LinkedTypewriterBlockEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class LinkedTypewriterDisplaySource extends SingleLineDisplaySource {

    @Override
    protected MutableComponent provideLine(final DisplayLinkContext context, final DisplayTargetStats stats) {
        if (!(context.getSourceBlockEntity() instanceof final LinkedTypewriterBlockEntity be)) {
            return EMPTY_LINE.copy();
        }

        return Component.literal(be.getTypedEntry());
    }

    @Override
    protected String getTranslationKey() {
        return "typewriter.typed_text";
    }

    @Override
    public boolean allowsLabeling(final DisplayLinkContext context) {
        return true;
    }
}
