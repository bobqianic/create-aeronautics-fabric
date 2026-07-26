package dev.ryanhcode.sable.network.tcp;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public interface SableTCPPacket extends CustomPacketPayload {

    void handle(SablePacketContext context);
}
