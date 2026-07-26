package dev.simulated_team.simulated.client.sections;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SimulatedSectionManager {
	private static final Map<ResourceLocation, SimulatedSection> SECTIONS = new HashMap<>();
	private static final Map<SimulatedSection, ResourceLocation> BY_SECTION = new HashMap<>();
	private static List<SimulatedSection> sortedSections = new ArrayList<>();

	public static SimulatedSection getSection(final ResourceLocation id) {
		return SECTIONS.get(id);
	}

	public static ResourceLocation getId(final SimulatedSection section) {
		return BY_SECTION.get(section);
	}

	public static List<SimulatedSection> getSections() {
		return sortedSections;
	}

	public static class ReloadListener extends SimpleJsonResourceReloadListener<SimulatedSection> {

		public ReloadListener() {
			super(SimulatedSection.CODEC, FileToIdConverter.json("simulated_sections"));
		}

		@Override
		protected void apply(final Map<ResourceLocation, SimulatedSection> map, final ResourceManager resourceManager, final ProfilerFiller profilerFiller) {
			SECTIONS.clear();
			BY_SECTION.clear();
			for (final Map.Entry<ResourceLocation, SimulatedSection> entry : map.entrySet()) {
				final SimulatedSection tab = entry.getValue();
				SECTIONS.put(entry.getKey(), tab);
				BY_SECTION.put(tab, entry.getKey());
			}

			sortedSections = SECTIONS.values().stream().sorted().toList();
		}
	}
}
