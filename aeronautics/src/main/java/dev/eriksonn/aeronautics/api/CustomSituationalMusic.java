package dev.eriksonn.aeronautics.api;

import dev.eriksonn.aeronautics.index.client.AeroClientRegistries;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.Music;

import java.util.Map;

public record CustomSituationalMusic(Music music, Condition condition) {
	public static Music getSituationalMusic(ClientLevel level, LocalPlayer player) {
		for (Map.Entry<ResourceKey<CustomSituationalMusic>, CustomSituationalMusic> entry : AeroClientRegistries.CUSTOM_SITUATIONAL_MUSIC.asVanillaRegistry().entrySet()) {
			CustomSituationalMusic value = entry.getValue();
			if(value.condition().test(level, player)) {
				return value.music();
			}
		}

		return null;
	}

	@FunctionalInterface
	public interface Condition {
		boolean test(ClientLevel level, LocalPlayer player);
	}
}
