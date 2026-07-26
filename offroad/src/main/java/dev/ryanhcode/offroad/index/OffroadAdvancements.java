package dev.ryanhcode.offroad.index;

import dev.simulated_team.simulated.data.advancements.SimAdvancements;
import dev.simulated_team.simulated.data.advancements.SimulatedAdvancement;
import dev.ryanhcode.offroad.Offroad;
import dev.ryanhcode.offroad.data.OffroadAdvancementTriggers;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.UnaryOperator;

public class OffroadAdvancements extends SimAdvancements {
	public static final List<SimulatedAdvancement> OFFROAD_ADVANCEMENTS = new ArrayList<>();

	public OffroadAdvancements(final PackOutput output, final CompletableFuture<HolderLookup.Provider> registries) {
		super(output, registries);
	}

	public List<SimulatedAdvancement> getAdvancementsArray() {
		return OFFROAD_ADVANCEMENTS;
	}

	@Override
	public @NotNull String getName() {
		return "Create Offroad Advancements";
	}

	public static void provideLang(final BiConsumer<String, String> consumer) {
		for (final SimulatedAdvancement advancement : OFFROAD_ADVANCEMENTS) {
			advancement.provideLang(consumer);
		}
	}

	private static SimulatedAdvancement create(final String id, final UnaryOperator<SimulatedAdvancement.Builder> b) {
		final SimulatedAdvancement advancement = new SimulatedAdvancement(id, b, Offroad.path("gui/advancement"), Offroad.MOD_ID, OffroadAdvancementTriggers::addSimple);
		OFFROAD_ADVANCEMENTS.add(advancement);
		return advancement;
	}

	public static void init() {}
}
