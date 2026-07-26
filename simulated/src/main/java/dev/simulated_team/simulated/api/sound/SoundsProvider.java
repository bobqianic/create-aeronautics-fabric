package dev.simulated_team.simulated.api.sound;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;

import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class SoundsProvider implements DataProvider {
	private static final Codec<Map<String, SoundDefinition>> CODEC = Codec.unboundedMap(Codec.STRING, SoundDefinition.CODEC);

	private final String modId;
	private final PackOutput output;
	private final Map<String, SoundDefinition> definitionMap;

	public SoundsProvider(final String modId, final PackOutput output, final Map<String, SoundDefinition> definitionMap) {
		this.modId = modId;
		this.output = output;
		this.definitionMap = definitionMap;
	}

	@Override
	public CompletableFuture<?> run(final CachedOutput cache) {
		return this.generate(this.output.getOutputFolder(), cache);
	}

	private CompletableFuture<?> generate(Path path, final CachedOutput cache) {
		path = path.resolve("assets").resolve(this.modId);

		JsonObject json = new JsonObject();
		final DataResult<JsonElement> result = CODEC.encode(this.definitionMap, JsonOps.INSTANCE, new JsonObject());

		if (result.isSuccess()) {
			json = result.getOrThrow().getAsJsonObject();
		}

		return DataProvider.saveStable(cache, json, path.resolve("sounds.json"));
	}

	@Override
	public String getName() {
		return this.modId + " sounds.json";
	}
}
