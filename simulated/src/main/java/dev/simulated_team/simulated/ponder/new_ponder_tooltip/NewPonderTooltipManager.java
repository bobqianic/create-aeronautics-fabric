package dev.simulated_team.simulated.ponder.new_ponder_tooltip;

import com.google.gson.*;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import dev.simulated_team.simulated.Simulated;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class NewPonderTooltipManager {
	private static final Codec<Set<ResourceLocation>> CODEC = ResourceLocation.CODEC.listOf().xmap(
			HashSet::new, set -> set.stream().toList());
	private static final HashMap<Item, Set<ResourceLocation>> NEW_PONDER_SCENES = new HashMap<>();
	private static Set<ResourceLocation> WATCHED_PONDER_SCENES = null;

	private static Path filePath() {
		return Minecraft.getInstance().gameDirectory.toPath().resolve("ponders_watched.json");
	}

	public static RegisterBuilder forItems(final Item... item) {
		return new RegisterBuilder(item);
	}

	public static boolean hasWatchedAllScenes(final Item item) {
		load();
		if (NEW_PONDER_SCENES.containsKey(item)) {
			final Set<ResourceLocation> scenes = NEW_PONDER_SCENES.get(item);
            return WATCHED_PONDER_SCENES.containsAll(scenes);
        }
		return true;
	}

	public static void setSceneWatched(final ResourceLocation id) {
		load();
		if(WATCHED_PONDER_SCENES != null && !hasWatchedScene(id)) {
			WATCHED_PONDER_SCENES.add(id);
			save();
		}
	}

	public static boolean hasWatchedScene(final ResourceLocation id) {
		load();
		return WATCHED_PONDER_SCENES.contains(id);
	}

	public static void save() {
		final DataResult<JsonElement> result = CODEC.encode(WATCHED_PONDER_SCENES, JsonOps.INSTANCE, new JsonArray());
		if(result.isError()) {
			return;
		}

		try {
			final String data = result.getOrThrow().toString();
			Files.writeString(filePath(), data, StandardCharsets.UTF_8);
		} catch (final IOException ignored) {

		}
	}

	public static void load() {
		if(WATCHED_PONDER_SCENES != null) return;

		final DataResult<Set<ResourceLocation>> result = CODEC.parse(JsonOps.INSTANCE, getOrCreateFile());
		WATCHED_PONDER_SCENES = new HashSet<>();
		result.ifSuccess((set) -> WATCHED_PONDER_SCENES.addAll(set));
	}

	private static @NotNull JsonElement getOrCreateFile() {
		final Path path = filePath();
		String jsonString = "[]";

		try {
			final File file = path.toFile();
			if(file.exists()) {
				jsonString = Files.readString(path);
			} else {
				Files.writeString(path, jsonString);
			}
		} catch (IOException ignored) {
			Simulated.LOGGER.info("There was an error reading ponders_watched.json.");
        }

		JsonElement element = new JsonArray();

		try {
			element = JsonParser.parseString(jsonString);
		} catch (JsonSyntaxException ignored) {
			Simulated.LOGGER.info("ponders_watched.json was malformed.");
		}

		return element;
	}

	public record RegisterBuilder(Item... items) {
		/**
		 * @param scenes set of scene IDs as set by {@link com.zurrtum.create.client.ponder.foundation.PonderSceneBuilder#title(java.lang.String, java.lang.String)}
		 */
		public RegisterBuilder addScenes(final ResourceLocation... scenes) {
			final Set<ResourceLocation> sceneSet = new HashSet<>(List.of(scenes));
			for (final Item item : this.items) {
				NEW_PONDER_SCENES.computeIfAbsent(item, k -> new HashSet<>()).addAll(sceneSet);
			}
			return this;
		}
	}
}
