package dev.ryanhcode.sable.fabric.platform;

import dev.ryanhcode.sable.platform.SableLoaderPlatform;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Path;

public class SableLoaderPlatformImpl implements SableLoaderPlatform {
	@Override
	public boolean isModLoaded(final String modId) {
		return FabricLoader.getInstance().isModLoaded(modId);
	}

	@Override
	public Path getGameDirectory() {
		return FabricLoader.getInstance().getGameDir();
	}

	@Override
	public String getModVersion(final String modId) {
		return FabricLoader.getInstance()
				.getModContainer(modId)
				.orElseThrow()
				.getMetadata()
				.getVersion()
				.getFriendlyString();
	}
}
