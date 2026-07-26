package dev.simulated_team.simulated.service;

public interface SimPlatformService {

	SimPlatformService INSTANCE = ServiceUtil.load(SimPlatformService.class);

	boolean isLoaded(String modId);
}
