package dev.eriksonn.aeronautics.index.client;

import dev.eriksonn.aeronautics.Aeronautics;
import dev.eriksonn.aeronautics.api.CustomSituationalMusic;
import foundry.veil.platform.registry.RegistrationProvider;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

public class AeroClientRegistries {
	public static class Keys {
		public static final ResourceKey<Registry<CustomSituationalMusic>> CUSTOM_SITUATIONAL_MUSIC = key("custom_situational_music");

		private static <T> ResourceKey<Registry<T>> key(final String name) {
			return ResourceKey.createRegistryKey(Aeronautics.path(name));
		}
	}

	public static RegistrationProvider<CustomSituationalMusic> CUSTOM_SITUATIONAL_MUSIC = registry(Keys.CUSTOM_SITUATIONAL_MUSIC);

	private static <T> RegistrationProvider<T> registry(final ResourceKey<Registry<T>> registryKey) {
		return RegistrationProvider.get(registryKey, Aeronautics.MOD_ID);
	}

	public static void init() {

	}

}
