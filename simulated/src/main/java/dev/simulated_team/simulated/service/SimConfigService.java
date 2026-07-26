package dev.simulated_team.simulated.service;

import dev.simulated_team.simulated.config.client.SimClient;
import dev.simulated_team.simulated.config.server.SimServer;

public interface SimConfigService {

	SimConfigService INSTANCE = ServiceUtil.load(SimConfigService.class);

	SimServer server();

	boolean serverLoaded();

	SimClient client();

	boolean clientLoaded();
}
