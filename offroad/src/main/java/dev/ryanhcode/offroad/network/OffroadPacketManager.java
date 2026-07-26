package dev.ryanhcode.offroad.network;

import dev.ryanhcode.offroad.Offroad;
import dev.ryanhcode.offroad.network.borehead_bearing.ClientboundMultiMiningSync;
import foundry.veil.api.network.VeilPacketManager;

public class OffroadPacketManager {
	public static VeilPacketManager INSTANCE = VeilPacketManager.create(Offroad.MOD_ID, "0.1");

	public static void init() {
		INSTANCE.registerClientbound(ClientboundMultiMiningSync.TYPE, ClientboundMultiMiningSync.CODEC, ClientboundMultiMiningSync::handle);
	}
}
