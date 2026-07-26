package dev.simulated_team.simulated.content.navigation_targets.lodestone_compass_compatability;

import dev.simulated_team.simulated.compat.create.CommonLevelAttached;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.joml.Vector3d;

import java.util.UUID;

public class ClientLodestonePositions {

	public static final CommonLevelAttached<ClientLodestonePositions> clientPositions = new CommonLevelAttached<>(level -> new ClientLodestonePositions());
	public final Object2ObjectOpenHashMap<UUID, Vector3d> CLIENT_LODESTONE_MAP = new Object2ObjectOpenHashMap<>();


}
