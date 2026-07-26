package dev.simulated_team.simulated.compat.computercraft.peripherals;

import dan200.computercraft.api.peripheral.IPeripheral;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3dc;

import java.util.List;

public abstract class SimPeripheral<T extends BlockEntity> implements IPeripheral {

    protected final T blockEntity;

    public SimPeripheral(final T blockEntity) {
        this.blockEntity = blockEntity;
    }

    @Override
    public boolean equals(final IPeripheral iPeripheral) {
        return iPeripheral == this;
    }

    static List<Float> vecList(final Vec3 vec3) {
        return List.of((float) vec3.x(), (float) vec3.y(), (float) vec3.z());
    }

    static List<Float> vecList(final Vector3dc vec3) {
        return List.of((float) vec3.x(), (float) vec3.y(), (float) vec3.z());
    }
}
