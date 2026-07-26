package dev.simulated_team.simulated.index;

import dev.simulated_team.simulated.Simulated;
import dev.simulated_team.simulated.api.SimpleResourceManager;
import dev.simulated_team.simulated.client.SearchAlias;
import dev.simulated_team.simulated.client.sections.SimulatedSection;
import dev.simulated_team.simulated.content.entities.diagram.screen.Greeble;

public class SimResourceManagers {
	public static SimpleResourceManager<SimulatedSection> SIMULATED_SECTION = SimpleResourceManager.create(SimulatedSection.CODEC, Simulated.path("sections")).sorted();
	public static SimpleResourceManager<Greeble> GREEBLE = SimpleResourceManager.create(Greeble.CODEC, Simulated.path("greebles"));
	public static SimpleResourceManager<SearchAlias> SEARCH_ALIAS = SimpleResourceManager.create(SearchAlias.CODEC, Simulated.path("search_aliases"));

	public static void init() {

	}
}