package dev.simulated_team.simulated.content.worldgen;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.chunk.ChunkAccess;
import org.jetbrains.annotations.Nullable;

public class SimulatedWorldPreset {

	private final ResourceLocation id;
	@Nullable
	private final Component description;

	public SimulatedWorldPreset(final ResourceLocation id, @Nullable final Component description) {
		this.id = id;
		this.description = description;
	}

	public void onPlayerJoin(final ServerLevel level, final ServerPlayer player) {}
	public void onChunkLoad(final ServerLevel level, final ChunkAccess chunkAccess, final boolean newChunk) {}
	public void modifyGameRules(final GameRules gameRules) {}

	public ResourceLocation id() {
		return this.id;
	}

	public @Nullable Component description() {
		return this.description;
	}
}
