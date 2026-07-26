package dev.ryanhcode.offroad.config;

import dev.simulated_team.simulated.service.ServiceUtil;
import dev.ryanhcode.offroad.config.client.OffroadClientConfig;
import dev.ryanhcode.offroad.config.server.OffroadServer;

public interface OffroadConfig {
	OffroadConfig INSTANCE = ServiceUtil.load(OffroadConfig.class);

	static OffroadServer server() {
		return INSTANCE.getServerConfig();
	}

	static OffroadClientConfig client() {
		return INSTANCE.getClientConfig();
	}

	OffroadServer getServerConfig();
	OffroadClientConfig getClientConfig();
}
