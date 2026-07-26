package dev.simulated_team.simulated.content.display_sources;

import com.zurrtum.create.content.redstone.displayLink.DisplayLinkContext;
import com.zurrtum.create.content.redstone.displayLink.target.DisplayTargetStats;
import dev.simulated_team.simulated.content.blocks.gimbal_sensor.GimbalSensorBlockEntity;
import dev.simulated_team.simulated.data.SimLang;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.List;

public class GimbalSensorDisplaySource extends AbstractNumericDisplaysource {
    @Override
    List<Component> getOptions() {
        return SimLang.translatedOptions("display_source.gimbal_sensor", "x_angle", "z_angle");
    }

    @Override
    String getKey() {
        return "gimbal_sensor.data";
    }

    @Override
    String getSelectionKey() {
        return "GimbalSensorSelection";
    }

    @Override
    public int getWidth() {
        return 50;
    }

    @Override
    public MutableComponent provideLine(final DisplayLinkContext displayLinkContext, final DisplayTargetStats displayTargetStats) {
        if (!(displayLinkContext.getSourceBlockEntity() instanceof final GimbalSensorBlockEntity be)) {
            return ZERO.copy();
        }

        switch (displayLinkContext.sourceConfig().getIntOr(this.getSelectionKey(), 0)) {
            case (0) -> {
                return SimLang.number(Math.toDegrees(be.getXAngle())).component();
            }
            case (1) -> {
                return SimLang.number(Math.toDegrees(be.getZAngle())).component();
            }
        }

        return ZERO.copy();
    }
}
