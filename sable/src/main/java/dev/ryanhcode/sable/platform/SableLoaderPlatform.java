package dev.ryanhcode.sable.platform;

import org.jetbrains.annotations.ApiStatus;

import java.nio.file.Path;

@ApiStatus.Internal
public interface SableLoaderPlatform {
	SableLoaderPlatform INSTANCE = SablePlatformUtil.load(SableLoaderPlatform.class);

	String getModVersion(String modId);

	/**
	 * Early-safe mod presence check (usable from mixin plugins). Replaces
	 * Veil's {@code Veil.platform().isModLoaded(...)} on the mc26.1 port branch.
	 */
	boolean isModLoaded(String modId);

	default Path getGameDirectory() {
		return null;
	}
}
