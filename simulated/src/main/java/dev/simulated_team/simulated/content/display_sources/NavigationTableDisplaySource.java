package dev.simulated_team.simulated.content.display_sources;

import com.zurrtum.create.content.redstone.displayLink.DisplayLinkContext;
import com.zurrtum.create.content.redstone.displayLink.source.SingleLineDisplaySource;
import com.zurrtum.create.content.redstone.displayLink.target.DisplayTargetStats;
import com.zurrtum.create.client.foundation.gui.ModularGuiLineBuilder;
import dev.simulated_team.simulated.content.blocks.nav_table.NavTableBlockEntity;
import dev.simulated_team.simulated.content.blocks.nav_table.navigation_target.NavigationTarget;
import dev.simulated_team.simulated.data.SimLang;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.time.Duration;

public class NavigationTableDisplaySource extends SingleLineDisplaySource implements ConfigurableDisplaySource {
	@Override
	protected MutableComponent provideLine(final DisplayLinkContext context, final DisplayTargetStats stats) {
		if (!(context.getSourceBlockEntity() instanceof final NavTableBlockEntity be)) {
			return EMPTY_LINE.copy();
		}

		switch (context.sourceConfig().getIntOr("NavTableSelection", 0)) {
			case 0 -> {
				final NavigationTarget navigationTarget = be.getNavTableItem();
				if(navigationTarget == null) return EMPTY_LINE.copy();

				final int distance = (int) navigationTarget.distanceToTarget(be);
				return Component.literal(String.valueOf(distance));
			}
			case 1 -> {
				final double distance = be.distanceToTarget();
				final double lastDistance = be.lastDistanceToTarget();
				final double change = lastDistance - distance;
				final double speed = change / .5f;

				final int totalSeconds = (int) (distance / speed);
				final Duration duration = Duration.ofSeconds(totalSeconds);

				String eta = "%2s:%2s".formatted(duration.toMinutesPart(), duration.toSecondsPart());

				if(duration.toHoursPart() > 0) {
					eta = "%2s:".formatted(duration.toHoursPart()) + eta;
				}

				if(totalSeconds < 0 || change < 0.001) {
					return Component.literal("N/A");
				}

				return Component.literal(eta.replace(' ', '0'));
			}
		}
		return EMPTY_LINE.copy();
	}

	@Override
	public void initConfigurationWidgets(final DisplayLinkContext context, final ModularGuiLineBuilder builder, final boolean isFirstLine) {
		if (isFirstLine) {
			return;
		}

		builder.addSelectionScrollInput(0, 95, (selectionScrollInput, label) -> {
			selectionScrollInput.forOptions(SimLang.translatedOptions("display_source.navigation_table",
					"distance", "eta_real"));
		}, "NavTableSelection");
	}

	@Override
	protected String getTranslationKey() {
		return "navigation_table.data";
	}

	@Override
	public boolean allowsLabeling(final DisplayLinkContext context) {
		return true;
	}
}
