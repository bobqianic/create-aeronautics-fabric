package dev.simulated_team.simulated.index;

import dev.simulated_team.simulated.Simulated;
import dev.simulated_team.simulated.content.worldgen.AirshipReadyPreset;
import dev.simulated_team.simulated.content.worldgen.EndSeaPreset;
import dev.simulated_team.simulated.content.worldgen.SimulatedWorldPreset;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

public class SimWorldPresets {
	public static final Map<ResourceLocation, SimulatedWorldPreset> PRESETS = new HashMap<>();

	public static final SimulatedWorldPreset AIRSHIP_READY = create(AirshipReadyPreset::new, "airship_ready", Component.translatable("generator.simulated.airship_ready.info"));
	public static final SimulatedWorldPreset END_SEA = create(EndSeaPreset::new, "end_sea", null);

	private static <T extends SimulatedWorldPreset> T create(final BiFunction<ResourceLocation, Component, T> constructor, final String name, @Nullable final Component description) {
		final ResourceLocation id = Simulated.path(name);
		final T preset = constructor.apply(id, description);
		PRESETS.put(id, preset);
		return preset;
	}

}
