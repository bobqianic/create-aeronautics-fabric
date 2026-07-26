package dev.ryanhcode.offroad;

import com.zurrtum.create.client.AllBlockEntityBehaviours;
import com.zurrtum.create.client.flywheel.lib.visualization.SimpleBlockEntityVisualizer;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.audio.KineticAudioBehaviour;
import com.zurrtum.create.client.ponder.foundation.PonderIndex;
import dev.ryanhcode.offroad.content.ponder.OffroadPonderPlugin;
import dev.ryanhcode.offroad.content.blocks.borehead_bearing.BoreheadBearingVisual;
import dev.ryanhcode.offroad.content.blocks.rock_cutting_wheel.RockCuttingWheelActorRender;
import dev.ryanhcode.offroad.index.OffroadBlocks;
import dev.ryanhcode.offroad.index.OffroadBlockEntityTypes;
import dev.ryanhcode.offroad.index.OffroadPartialModels;
import dev.simulated_team.simulated.compat.create.LegacyCustomKineticTooltipBehaviour;
import dev.simulated_team.simulated.compat.create.LegacyItemTooltips;
import dev.simulated_team.simulated.compat.create.LegacyScrollValueClientBehaviour;
import dev.simulated_team.simulated.compat.create.SableSubLevelRendererCompat;

public class OffroadClient {
	public static void init() {
		AllBlockEntityBehaviours.add(OffroadBlockEntityTypes.WHEEL_MOUNT.get(), LegacyCustomKineticTooltipBehaviour::new,
				KineticAudioBehaviour::new, LegacyScrollValueClientBehaviour::new);
		AllBlockEntityBehaviours.add(OffroadBlockEntityTypes.BOREHEAD_BEARING.get(), LegacyCustomKineticTooltipBehaviour::new,
				KineticAudioBehaviour::new);
		SimpleBlockEntityVisualizer.builder(OffroadBlockEntityTypes.BOREHEAD_BEARING.get())
				.factory(BoreheadBearingVisual::new).apply();
		registerSableImmediateRenderers();

		LegacyItemTooltips.register(Offroad.getRegistrate());

		PonderIndex.addPlugin(new OffroadPonderPlugin());
		OffroadBlocks.ROCK_CUTTING_WHEEL_ACTOR.attachRender = new RockCuttingWheelActorRender();

		OffroadPartialModels.init();
	}

	private static void registerSableImmediateRenderers() {
		SableSubLevelRendererCompat.registerLegacyRenderers(
				Offroad.LOGGER,
				OffroadBlockEntityTypes.BOREHEAD_BEARING.get(),
				OffroadBlockEntityTypes.ROCKCUTTING_WHEEL_BLOCK_ENTITY.get(),
				OffroadBlockEntityTypes.WHEEL_MOUNT.get()
		);
	}
}
