package dev.simulated_team.simulated.content.display_sources;

import com.zurrtum.create.content.redstone.displayLink.DisplayLinkContext;
import com.zurrtum.create.content.redstone.displayLink.target.DisplayTargetStats;
import dev.simulated_team.simulated.content.blocks.portable_engine.PortableEngineBlockEntity;
import dev.simulated_team.simulated.data.SimLang;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.List;

public class PortableEngineDisplaySource extends AbstractNumericDisplaysource {

    @Override
    List<Component> getOptions() {
        return SimLang.translatedOptions("display_source.portable_engine", "current_burn", "total_burn");
    }

    @Override
    String getKey() {
        return "portable_engine.data";
    }

    @Override
    String getSelectionKey() {
        return "PortableEngineSelection";
    }

    @Override
    protected MutableComponent provideLine(final DisplayLinkContext displayLinkContext, final DisplayTargetStats displayTargetStats) {
        if (!(displayLinkContext.getSourceBlockEntity() instanceof final PortableEngineBlockEntity be)) {
            return ZERO.copy();
        }

        switch (displayLinkContext.sourceConfig().getIntOr(this.getSelectionKey(), 0)) {
            case 0 -> {
                if (be.isCurrentFuelInfinite()) {
                    return SimLang.translate("portable_engine.infinite").component();
                }
                return SimLang.number((double) be.getCurrentBurnTime() / 20).component();
            }
            case 1 -> {
                if (be.isCurrentFuelInfinite()) {
                    return SimLang.translate("portable_engine.infinite").component();
                }
                return SimLang.number((double) be.getTotalBurnTime() / 20).component();
            }
        }

        return ZERO.copy();
    }
}
