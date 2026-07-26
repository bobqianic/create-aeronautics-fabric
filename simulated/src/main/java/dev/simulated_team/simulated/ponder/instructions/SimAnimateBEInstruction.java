package dev.simulated_team.simulated.ponder.instructions;

import com.zurrtum.create.client.foundation.ponder.instruction.AnimateBlockEntityInstruction;
import dev.simulated_team.simulated.content.blocks.steering_wheel.SteeringWheelBlockEntity;
import dev.simulated_team.simulated.content.blocks.torsion_spring.TorsionSpringBlockEntity;
import com.zurrtum.create.client.ponder.api.level.PonderLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class SimAnimateBEInstruction extends AnimateBlockEntityInstruction {

    protected SimAnimateBEInstruction(final BlockPos location, final float totalDelta, final int ticks, final BiConsumer<PonderLevel, Float> setter, final Function<PonderLevel, Float> getter) {
        super(location, totalDelta, ticks, setter, getter);
    }

    public static AnimateBlockEntityInstruction torsionSpring(final BlockPos location, final float totalDelta, final int ticks) {
        return new SimAnimateBEInstruction(location, totalDelta, ticks,
                (level, value) -> castIfPresent(level, location, TorsionSpringBlockEntity.class)
                        .ifPresent(be -> be.setAngle(value)),
                level -> castIfPresent(level, location, TorsionSpringBlockEntity.class)
                        .map(TorsionSpringBlockEntity::getAngle).orElse(0f)
        );
    }

    public static AnimateBlockEntityInstruction steeringWheel(final BlockPos location, final float totalDelta, final int ticks) {
        return new SimAnimateBEInstruction(location, totalDelta, ticks,
                (level, value) -> castIfPresent(level, location, SteeringWheelBlockEntity.class)
                        .ifPresent(be -> {
                            be.targetAngleToUpdate = value;
                        }),
                (level) -> castIfPresent(level, location, SteeringWheelBlockEntity.class)
                    .map(be -> be.targetAngleToUpdate).orElse(0f)
        );
    }

    public static <T> Optional<T> castIfPresent(final PonderLevel world, final BlockPos pos, final Class<T> beType) {
        final BlockEntity blockEntity = world.getBlockEntity(pos);
        if (beType.isInstance(blockEntity))
            return Optional.of(beType.cast(blockEntity));
        return Optional.empty();
    }
}
