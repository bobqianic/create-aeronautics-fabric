package dev.simulated_team.simulated.content.display_sources;

import com.zurrtum.create.content.redstone.displayLink.DisplayLinkContext;
import com.zurrtum.create.content.redstone.displayLink.source.NumericSingleLineDisplaySource;
import com.zurrtum.create.content.redstone.displayLink.target.DisplayTargetStats;
import com.zurrtum.create.client.foundation.gui.ModularGuiLineBuilder;
import dev.simulated_team.simulated.content.blocks.lasers.laser_sensor.LaserSensorBlockEntity;
import dev.simulated_team.simulated.data.SimLang;
import net.minecraft.network.chat.MutableComponent;

public class LaserSensorDisplaySource extends NumericSingleLineDisplaySource implements ConfigurableDisplaySource {
    @Override
    protected MutableComponent provideLine(final DisplayLinkContext context, final DisplayTargetStats displayTargetStats) {
        if (!(context.getSourceBlockEntity() instanceof final LaserSensorBlockEntity be)) {
            return EMPTY.getFirst();
        }

        return (be.closestHitDistance == Double.MAX_VALUE) ?
                EMPTY.getFirst() :
                SimLang.number(be.closestHitDistance)
                        .space()
                        .text("block" + (be.closestHitDistance != 1 ? "s" : ""))
                        .component();
    }

    @Override
    public void initConfigurationWidgets(final DisplayLinkContext context, final ModularGuiLineBuilder builder, final boolean isFirstLine) {
        if (isFirstLine) {
            return;
        }

        builder.addSelectionScrollInput(0, 88, (selectionScrollInput, label) -> {
            selectionScrollInput
                    .forOptions(SimLang.translatedOptions("display_source.laser_sensor", "laser_distance"));
        }, "LaserSensorSelection");
    }

    @Override
    protected String getTranslationKey() {
        return "laser_sensor.data";
    }

    @Override
    public boolean allowsLabeling(final DisplayLinkContext displayLinkContext) {
        return true;
    }
}
