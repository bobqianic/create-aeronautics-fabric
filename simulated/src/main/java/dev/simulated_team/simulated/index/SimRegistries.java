package dev.simulated_team.simulated.index;

import dev.simulated_team.simulated.Simulated;
import dev.simulated_team.simulated.content.blocks.nav_table.navigation_target.NavigationTarget;
import foundry.veil.platform.registry.RegistrationProvider;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

public class SimRegistries {
	public static class Keys {
		public static final ResourceKey<Registry<NavigationTarget>> NAVIGATION_TARGET = key("navigation_target");

		private static <T> ResourceKey<Registry<T>> key(final String name) {
			return ResourceKey.createRegistryKey(Simulated.path(name));
		}
	}

	public static final Registry<NavigationTarget> NAVIGATION_TARGET = registry(Keys.NAVIGATION_TARGET);

	private static <T> Registry<T> registry(final ResourceKey<Registry<T>> registryKey) {
		final RegistrationProvider<T> provider = RegistrationProvider.get(registryKey, Simulated.MOD_ID);
		return provider.asVanillaRegistry();
	}

	public static void register() {}
}
