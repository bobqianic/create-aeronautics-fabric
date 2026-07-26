package dev.eriksonn.aeronautics.config;

import dev.simulated_team.simulated.service.ServiceUtil;
import dev.eriksonn.aeronautics.config.client.AeroClient;
import dev.eriksonn.aeronautics.config.server.AeroServer;

public interface AeroConfig {
	AeroConfig INSTANCE = ServiceUtil.load(AeroConfig.class);

	static AeroServer server() {
		return INSTANCE.getServerConfig();
	}

	static AeroClient client() {
		return INSTANCE.getClientConfig();
	}

	AeroServer getServerConfig();
	AeroClient getClientConfig();
}
