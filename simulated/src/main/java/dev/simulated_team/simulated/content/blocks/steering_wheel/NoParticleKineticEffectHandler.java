package dev.simulated_team.simulated.content.blocks.steering_wheel;

import com.zurrtum.create.content.kinetics.base.KineticBlockEntity;
import com.zurrtum.create.content.kinetics.base.KineticEffectHandler;

public class NoParticleKineticEffectHandler extends KineticEffectHandler {
    public NoParticleKineticEffectHandler(final KineticBlockEntity kte) {
        super(kte);
    }

    @Override
    public void spawnRotationIndicators() {
    }
}
