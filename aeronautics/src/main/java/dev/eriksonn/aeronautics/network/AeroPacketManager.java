package dev.eriksonn.aeronautics.network;

import dev.eriksonn.aeronautics.Aeronautics;
import dev.eriksonn.aeronautics.network.packets.LevititeCatalystCrystallizationPacket;
import foundry.veil.api.network.VeilPacketManager;

public class AeroPacketManager {
	public static VeilPacketManager INSTANCE = VeilPacketManager.create(Aeronautics.MOD_ID, "0.1");

	public static void init() {
		INSTANCE.registerServerbound(LevititeCatalystCrystallizationPacket.TYPE, LevititeCatalystCrystallizationPacket.STREAM_CODEC, LevititeCatalystCrystallizationPacket::handle);
	}
}
