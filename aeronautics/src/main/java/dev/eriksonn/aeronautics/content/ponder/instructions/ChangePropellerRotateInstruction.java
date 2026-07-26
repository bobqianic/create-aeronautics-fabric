package dev.eriksonn.aeronautics.content.ponder.instructions;

import dev.eriksonn.aeronautics.content.blocks.propeller.bearing.propeller_bearing.PropellerBearingBlockEntity;
import dev.simulated_team.simulated.api.BearingSlowdownController;
import com.zurrtum.create.client.ponder.api.element.ElementLink;
import com.zurrtum.create.client.ponder.api.element.WorldSectionElement;
import com.zurrtum.create.client.ponder.foundation.PonderScene;
import com.zurrtum.create.client.ponder.foundation.instruction.PonderInstruction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public abstract class ChangePropellerRotateInstruction extends PonderInstruction {

    private final PropellerRotateInstruction instruction;
    private final BiConsumer<PropellerRotateInstruction,PonderScene> consumer;

    ChangePropellerRotateInstruction(PropellerRotateInstruction instruction, BiConsumer<PropellerRotateInstruction,PonderScene> consumer) {
        this.instruction = instruction;
        this.consumer = consumer;
    }

    @Override
    public boolean isComplete() {
        return true;
    }

    @Override
    public void tick(PonderScene scene) {
        consumer.accept(instruction,scene);
    }

    public static class SetRotationRate extends ChangePropellerRotateInstruction {
        public SetRotationRate(PropellerRotateInstruction instruction, float targetSpeed) {
            super(instruction, (i,s) -> i.targetSpeed = targetSpeed);
        }
    }

    public static class StopRotation extends ChangePropellerRotateInstruction {
        public StopRotation(PropellerRotateInstruction instruction, float duration) {
            super(instruction, (i,s) -> {
                if (s.getWorld().getBlockEntity(i.pos) instanceof PropellerBearingBlockEntity bearing) {
                    float angle = bearing.getInterpolatedAngle(0);
                    i.slowdownController = new BearingSlowdownController();
                    i.slowdownController.generate(duration, angle, i.currentSpeed, BearingSlowdownController.ContraptionSymmetry.QUARTER);
                }
            });
        }
    }

    public static class SetParticles extends ChangePropellerRotateInstruction {
        public SetParticles(PropellerRotateInstruction instruction, @Nullable final ElementLink<WorldSectionElement> link, final float particleAmount, final float particleSpeed, final float radius, final boolean hasCollision) {
            super(instruction, (i,s) -> {
                i.particleSpeedScale = particleSpeed;
                i.particleAmountScale = particleAmount;
                i.spawner = new PropellerParticleSpawningInstruction.ParticleSpawner(link, i.pos.offset(i.direction.getUnitVec3i()), i.direction, 0, 0, radius, hasCollision);
            });
        }
        public SetParticles(PropellerRotateInstruction instruction,BlockPos location, @Nullable final ElementLink<WorldSectionElement> link, final float particleAmount, final float particleSpeed, final float radius, final boolean hasCollision) {
            super(instruction, (i,s) -> {
                i.particleSpeedScale = particleSpeed;
                i.particleAmountScale = particleAmount;
                i.spawner = new PropellerParticleSpawningInstruction.ParticleSpawner(link, location, i.direction, 0, 0, radius, hasCollision);
            });
        }
    }
    public static class StopParticles extends ChangePropellerRotateInstruction {
        public StopParticles(PropellerRotateInstruction instruction) {
            super(instruction, (i,s) -> {
                i.spawner = null;
            });
        }
    }
    public static class AddSection extends ChangePropellerRotateInstruction {
        public AddSection(PropellerRotateInstruction instruction,ElementLink<WorldSectionElement> link)
        {
            super(instruction, (i,s) -> i.addSection(s,link));
        }
    }

}
