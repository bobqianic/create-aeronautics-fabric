package dev.eriksonn.aeronautics.registry;

import dev.simulated_team.simulated.registrate.SimulatedRegistrate;
import com.tterrag.registrate.util.entry.RegistryEntry;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import dev.eriksonn.aeronautics.content.blocks.hot_air.lifting_gas.LiftingGasType;
import dev.eriksonn.aeronautics.index.AeroRegistries;
import net.minecraft.resources.ResourceLocation;

public class AeroRegistrate extends SimulatedRegistrate {
    public AeroRegistrate(final ResourceLocation initialSection, final String modId) {
        super(initialSection, modId);
    }

    public <T extends LiftingGasType> RegistryEntry<LiftingGasType, T> liftingGasType(final String name, final NonNullSupplier<T> type) {
        return this.simple(this.self(), name, AeroRegistries.Keys.LIFTING_GAS_TYPE, type);
    }
}
