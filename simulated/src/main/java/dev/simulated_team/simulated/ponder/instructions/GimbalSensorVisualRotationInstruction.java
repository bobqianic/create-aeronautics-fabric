package dev.simulated_team.simulated.ponder.instructions;

import dev.simulated_team.simulated.content.blocks.gimbal_sensor.GimbalSensorBlockEntity;
import com.zurrtum.create.client.ponder.api.level.PonderLevel;
import com.zurrtum.create.client.ponder.foundation.PonderScene;
import com.zurrtum.create.client.ponder.foundation.instruction.PonderInstruction;
import net.minecraft.core.BlockPos;

public class GimbalSensorVisualRotationInstruction extends PonderInstruction {
    BlockPos location;
    Boolean unlocked;

    public GimbalSensorVisualRotationInstruction(final BlockPos location, final Boolean unlocked) {
        this.location = location;
        this.unlocked = unlocked;
    }

    @Override
    public boolean isComplete() {
        return false;
    }

    @Override
    public void tick(final PonderScene scene) {
        final PonderLevel world = scene.getWorld();
        if (world.getBlockEntity(this.location) instanceof final GimbalSensorBlockEntity be) {
            be.updateVisualRotation = this.unlocked;
        }
    }
}
