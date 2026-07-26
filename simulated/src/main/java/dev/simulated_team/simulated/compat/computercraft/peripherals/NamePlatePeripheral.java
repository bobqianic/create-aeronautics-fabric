package dev.simulated_team.simulated.compat.computercraft.peripherals;

import dan200.computercraft.api.lua.LuaFunction;
import dev.simulated_team.simulated.content.blocks.nameplate.NameplateBlockEntity;

public class NamePlatePeripheral extends SimPeripheral<NameplateBlockEntity>{
    public NamePlatePeripheral(final NameplateBlockEntity blockEntity) {
        super(blockEntity);
    }

    @Override
    public String getType() {
        return "name_plate";
    }

    @LuaFunction
    public void setName(final String newName) {
        this.blockEntity.setName(newName, true, null);
    }

    @LuaFunction
    public String getName() {
        return this.blockEntity.getName();
    }
}
