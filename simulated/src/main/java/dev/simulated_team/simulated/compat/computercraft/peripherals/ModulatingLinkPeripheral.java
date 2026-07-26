package dev.simulated_team.simulated.compat.computercraft.peripherals;

import dan200.computercraft.api.lua.LuaFunction;
import dev.simulated_team.simulated.content.blocks.redstone.modulating_receiver.ModulatingLinkedReceiverBlockEntity;

public class ModulatingLinkPeripheral extends SimPeripheral<ModulatingLinkedReceiverBlockEntity>{
    public ModulatingLinkPeripheral(final ModulatingLinkedReceiverBlockEntity blockEntity) {
        super(blockEntity);
    }

    @Override
    public String getType() {
        return "modulating_link";
    }

    @LuaFunction
    public double getClosestDistance() {
        return this.blockEntity.getDistanceToClosest();
    }
}
