package dev.eriksonn.aeronautics.index;

import com.tterrag.registrate.util.entry.RegistryEntry;
import dev.eriksonn.aeronautics.Aeronautics;
import dev.eriksonn.aeronautics.content.blocks.hot_air.lifting_gas.DefaultLiftingGas;
import dev.eriksonn.aeronautics.content.blocks.hot_air.lifting_gas.LiftingGasType;
import dev.eriksonn.aeronautics.content.blocks.hot_air.lifting_gas.SteamLiftingGas;
import dev.eriksonn.aeronautics.registry.AeroRegistrate;

public class AeroLiftingGasTypes {

    public static final AeroRegistrate REGISTRATE = Aeronautics.getRegistrate();

    public static RegistryEntry<LiftingGasType, DefaultLiftingGas> DEFAULT_GAS = REGISTRATE.liftingGasType("default", DefaultLiftingGas::new);
    public static RegistryEntry<LiftingGasType, SteamLiftingGas> STEAM = REGISTRATE.liftingGasType("steam", SteamLiftingGas::new);

    public static void init() {
        // no-op
    }
}