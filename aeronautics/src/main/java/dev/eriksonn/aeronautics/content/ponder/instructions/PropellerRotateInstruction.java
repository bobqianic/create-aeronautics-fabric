package dev.eriksonn.aeronautics.content.ponder.instructions;

import com.zurrtum.create.content.kinetics.base.KineticBlockEntity;
import dev.eriksonn.aeronautics.content.blocks.propeller.bearing.propeller_bearing.PropellerBearingBlockEntity;
import dev.simulated_team.simulated.api.BearingSlowdownController;
import com.zurrtum.create.client.ponder.api.element.ElementLink;
import com.zurrtum.create.client.ponder.api.element.WorldSectionElement;
import com.zurrtum.create.client.ponder.foundation.PonderScene;
import com.zurrtum.create.client.ponder.foundation.instruction.PonderInstruction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class PropellerRotateInstruction extends PonderInstruction {

    BlockPos pos;
    List<ElementLink<WorldSectionElement>> contraptions;
    long activatedContraptionsIndex = 0;
    final Direction direction;
    final Vec3 normal;
    float lastAngle;
    float targetSpeed;
    final float originalSails;
    final float originalSpeed;
    float currentSpeed;
    float sailSmoothingAmount;
    boolean stopped;
    BearingSlowdownController slowdownController = null;

    PropellerParticleSpawningInstruction.ParticleSpawner spawner = null;
    float particleSpeedScale;
    float particleAmountScale;

    public PropellerRotateInstruction(BlockPos pos, ElementLink<WorldSectionElement> contraption, Direction direction, float targetSpeed,float sailSmoothingAmount)
    {
        this.pos = pos;
        this.contraptions = new ArrayList<>();
        contraptions.add(contraption);
        this.direction = direction;
        originalSpeed = this.targetSpeed = targetSpeed;
        originalSails = this.sailSmoothingAmount = sailSmoothingAmount;
        Vec3i n = direction.getUnitVec3i();
        normal = new Vec3(Math.abs(n.getX()),Math.abs(n.getY()),Math.abs(n.getZ()));
    }

    @Override
    public void reset(PonderScene scene) {
        super.reset(scene);
        sailSmoothingAmount = originalSails;
        targetSpeed = originalSpeed;
        currentSpeed = 0;
        lastAngle = 0;
        slowdownController = null;
        stopped = false;
    }

    @Override
    public boolean isComplete() {
        return stopped;
    }

    @Override
    public void tick(PonderScene scene) {
        if(scene.getWorld().getBlockEntity(pos) instanceof PropellerBearingBlockEntity bearing)
        {
            float angle = bearing.getInterpolatedAngle(0);

            if(slowdownController != null)
            {
                stopped = slowdownController.stepGoal();
                currentSpeed = slowdownController.getSpeed(0);
                angle = slowdownController.getAngle(0);
            }else {
                currentSpeed = Mth.lerp(0.4f / (float) Math.sqrt(this.sailSmoothingAmount), currentSpeed, KineticBlockEntity.convertToAngular(targetSpeed));
                angle += currentSpeed;
            }

            if(spawner != null)
            {
                spawner.particleSpeed = currentSpeed * particleSpeedScale / (10f*20f);
                spawner.particleAmount = Math.abs(currentSpeed) * particleAmountScale / 10f;
                spawner.tick(scene);
            }

            for (ElementLink<WorldSectionElement> contraption : contraptions) {
                WorldSectionElement link = scene.resolve(contraption);
                if (link != null)
                    updateLinkAngle(link, angle, false);
            }
            bearing.setAngle(angle);
        }
    }

    public void addSection(PonderScene scene,ElementLink<WorldSectionElement> section)
    {
        contraptions.add(section);
        if(scene.getWorld().getBlockEntity(pos) instanceof PropellerBearingBlockEntity bearing)
        {
            float angle = bearing.getInterpolatedAngle(0);
            WorldSectionElement link = scene.resolve(section);
            if(link != null)
                updateLinkAngle(link,angle,true);
        }
    }
    void updateLinkAngle(WorldSectionElement link,float angle,boolean forced)
    {
        Vec3 v = link.getAnimatedRotation();
        double d = v.dot(normal);//keep rotation on other angles
        link.setAnimatedRotation(v.add(normal.scale(angle-d)),forced);
    }
}
