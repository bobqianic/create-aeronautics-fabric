package dev.eriksonn.aeronautics.content.blocks.hot_air.lifting_gas;

import dev.eriksonn.aeronautics.config.AeroConfig;
import dev.eriksonn.aeronautics.data.AeroLang;
import net.minecraft.network.chat.Component;

public class SteamLiftingGas implements LiftingGasType {
    @Override
    public Component getName() {
        return AeroLang.translate("lifting_gas.steam").component();
    }

    @Override
    public double getFillingTime() {
        return 9.0 * 20;
    }

    @Override
    public double getEmptyingTime() {
        return 9.0 * 20;
    }

    @Override
    public double getLiftStrength() {
        return AeroConfig.server().physics.steamStrength.get();
    }

    @Override
    public double getResponsivenessAdjustmentFactor() {
        return 5;
    }

    @Override
    public double getResponsivenessAdjustmentRange() {
        return 0.05;
    }
}
