package dev.eriksonn.aeronautics.content.blocks.hot_air.lifting_gas;

import net.minecraft.network.chat.Component;

public interface LiftingGasType {

    Component getName();

    double getFillingTime();

    double getEmptyingTime();

    double getLiftStrength();

    /**
     * smoothly increases convergence rate to the target by this factor
     * @return
     */
    double getResponsivenessAdjustmentFactor();

    /**
     * @return approximate range of increased convergence, as ratio of the full balloon volume
     */
    double getResponsivenessAdjustmentRange();
}
