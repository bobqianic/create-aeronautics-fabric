package dev.simulated_team.simulated.api;

import com.zurrtum.create.content.contraptions.Contraption;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import java.util.Map;

public class BearingSlowdownController {
    public static final float TIMER_SCALE = 3.5f;
    public static final float SMOOTHING_FACTOR = 2.5f;

    private float maxTime;
    private float initialVelocity;

    private float clampedTime;
    private float countdown;
    private float initialAngle;

    /**
     * How many symmetries this slowdown controller has
     */
    private ContraptionSymmetry symmetry;

    private float linearDistance;
    private float scaledOffset;
    private float targetStoppingPoint;
    private float angleOffset;

    public BearingSlowdownController() {}

    /**
     * Generates necessary values for this slowdown controller to operate properly.
     *
     * @param maxTime The maximum amount of time this controller should spend slowing down. maximum of 30 ticks.
     * @param initialVelocity The initial velocity of this slowdown controller.
     * @param initialAngle The initial Angle of this slowdown controller.
     * @param facingDirection The facing direction of this slowdown controller.
     * @param attachedContraption The attached contraption that this slowdown controller will generate symmetries based off of.
     */
    public void generate(final float maxTime,
                         final float initialAngle,
                         final float initialVelocity,
                         final Direction facingDirection,
                         final Contraption attachedContraption)
    {
        generate(maxTime,initialAngle,initialVelocity,getSymmetry(facingDirection,attachedContraption));
    }

    public void generate(final float maxTime,
                         final float initialAngle,
                         final float initialVelocity,final ContraptionSymmetry symmetry) {
        this.maxTime = maxTime;
        this.maxTime = Math.min(this.maxTime, 30);

        this.symmetry = symmetry;

        this.initialAngle = initialAngle;
        this.initialVelocity = initialVelocity;

        this.maxTime = this.getMaxTime() * (float) Math.sqrt(this.symmetry.getAngle()/90);

        this.generateConstants();
        this.applyVelocityClamping();

        this.countdown = this.clampedTime;
    }

    ContraptionSymmetry getSymmetry(final Direction facingDirection, final Contraption attachedContraption)
    {
        boolean halfSymmetry = true;
        boolean quarterSymmetry = true;
        final Map<BlockPos, StructureTemplate.StructureBlockInfo> Blocks = attachedContraption.getBlocks();
        for (final Map.Entry<BlockPos, StructureTemplate.StructureBlockInfo> entry : Blocks.entrySet()) {

            final Block current = entry.getValue().state().getBlock();
            final BlockPos R1 = this.Rotate90(entry.getKey(), facingDirection);
            final BlockPos R2 = this.Rotate90(R1, facingDirection);

            if (!Blocks.containsKey(R1) || !Blocks.get(R1).state().getBlock().equals(current)) {
                quarterSymmetry = false;
            }
            if (!Blocks.containsKey(R2) || !Blocks.get(R2).state().getBlock().equals(current)) {
                halfSymmetry = false;
            }
        }
        if (quarterSymmetry) {
            return ContraptionSymmetry.QUARTER;
        }
        if (halfSymmetry) {
            return ContraptionSymmetry.HALF;
        }
        return ContraptionSymmetry.NONE;

    }

    private void generateConstants() {
        final float symmetryAngle = this.symmetry.getAngle();
        //distance travelled without any slowdown
        this.linearDistance = this.getMaxTime() * this.getInitialVelocity();
        //Where the propeller would end up if it was slowing down at the optimal rate
        final float optimalStoppingPoint = this.initialAngle + this.linearDistance / SMOOTHING_FACTOR;
        //Rounded to the nearest angle compatible with the current symmetry mode
        this.targetStoppingPoint = symmetryAngle * Math.round(optimalStoppingPoint / symmetryAngle);
        //total angular distance the prop has to travel
        this.angleOffset = this.initialAngle - this.targetStoppingPoint;

        this.scaledOffset = this.angleOffset * SMOOTHING_FACTOR + this.linearDistance;
    }

    private void applyVelocityClamping() {
        //shortens the animation time if the maximum velocity is too small
        final float maxVelocity = this.getMaxVelocity();
        final float targetVelocityClamp = 120 / this.getMaxTime();
        if (maxVelocity < targetVelocityClamp) {
            this.clampedTime = this.getMaxTime() * maxVelocity / targetVelocityClamp;
            this.clampedTime = Math.max(this.clampedTime, 2);
            this.linearDistance = this.clampedTime * this.getInitialVelocity();
        } else {
            this.clampedTime = this.getMaxTime();
        }
    }

    private float getMaxVelocity() {
        //i am sorry, but i dont have a good name for these :animal:
        final float A = (1 + SMOOTHING_FACTOR) * this.angleOffset + this.linearDistance;
        final float B = (1 + SMOOTHING_FACTOR) * (SMOOTHING_FACTOR * this.angleOffset + this.linearDistance);
        final float normalizedTimeAtMaxVelocity = (A + this.linearDistance) / B;
        float maxVelocity = Math.abs(this.getInitialVelocity());
        if (Math.abs(B) > 0.001 && normalizedTimeAtMaxVelocity > 0 && normalizedTimeAtMaxVelocity < 1) {
            //estimated velocity at inflection point within the normalized time interval
            float estimatedTurnaroundVelocity = -A * (float) Math.pow((SMOOTHING_FACTOR - 1) * A / B, SMOOTHING_FACTOR - 1) / this.getMaxTime();
            estimatedTurnaroundVelocity = Math.abs(estimatedTurnaroundVelocity);
            maxVelocity = Math.max(estimatedTurnaroundVelocity, maxVelocity);
        }
        return maxVelocity;
    }

    private BlockPos Rotate90(final BlockPos pos, final Direction dir) {
        final int x1 = pos.getX();
        final int y1 = pos.getY();
        final int z1 = pos.getZ();

        final int x2 = dir.getStepX();
        final int y2 = dir.getStepY();
        final int z2 = dir.getStepZ();
        final int dotProduct = x1 * x2 + y1 * y2 + z1 * z2;

        return new BlockPos(
                y1 * z2 - z1 * y2 + dotProduct * x2,
                z1 * x2 - x1 * z2 + dotProduct * y2,
                x1 * y2 - y1 * x2 + dotProduct * z2);//cross product
    }

    //unsure what this is.. current time elapsed?
    public float getTime() {
        return this.clampedTime - this.countdown;
    }

    /**
     * @return Get the remaining time for this controller to reach its goal.
     */
    public float getCountdown() {
        return this.countdown;
    }

    /**
     * @return Whether this controller has reached its goal.
     */
    public boolean stepGoal() {
        if (this.countdown > 0) {
            this.countdown--;
            if (this.countdown <= 0.5) {
                this.countdown = 0;
                return true;
            }
        }

        return false;
    }

    public float getAngle(final float partialTick) {
        final float time = this.getTime() + partialTick;
        if (time >= this.clampedTime) {
            return this.targetStoppingPoint;
        }
        final float normalizedTime = time / this.clampedTime;
        final float lerpParameter = (float) Math.pow(1 - normalizedTime, SMOOTHING_FACTOR);
        return this.targetStoppingPoint + (this.angleOffset + normalizedTime * this.scaledOffset) * lerpParameter;
    }

    public float getSpeed(final float partialTick) {
        final float time = this.getTime() + partialTick;
        if (time >= this.clampedTime) {
            return 0;
        }
        final float normalizedTime = time / this.clampedTime;
        final float lerpDerivative = (float) Math.pow(1 - normalizedTime, SMOOTHING_FACTOR - 1);
        return (this.linearDistance - (1 + SMOOTHING_FACTOR) * normalizedTime * this.scaledOffset) * lerpDerivative / this.clampedTime;
    }

    public void deserializeFromNBT(final CompoundTag nbt) {
        this.countdown = nbt.getFloatOr("CurrentTime", 0);
        this.maxTime = nbt.getFloatOr("DisassemblyTimerTotal", 0);
        this.symmetry = ContraptionSymmetry.values()[nbt.getIntOr("Symmetry", 0)];
        this.initialAngle = nbt.getFloatOr("InitialSlowdownAngle", 0);
        this.initialVelocity = nbt.getFloatOr("InitialSlowdownVelocity", 0);
        this.generateConstants();
        this.applyVelocityClamping();
    }

    public void serializeIntoNBT(final CompoundTag nbt) {
        nbt.putFloat("CurrentTime", this.countdown);
        nbt.putFloat("DisassemblyTimerTotal", this.getMaxTime());
        nbt.putInt("Symmetry", this.symmetry.ordinal());
        nbt.putFloat("InitialSlowdownAngle", this.initialAngle);
        nbt.putFloat("InitialSlowdownVelocity", this.getInitialVelocity());
    }

    public float getMaxTime() {
        return this.maxTime;
    }

    public float getInitialVelocity() {
        return this.initialVelocity;
    }

    public enum ContraptionSymmetry
    {
        NONE(360),
        HALF(180),
        QUARTER(90);
        final float angle;
        ContraptionSymmetry(float angle)
        {
            this.angle = angle;
        }
        public float getAngle()
        {
            return angle;
        }
    }
}
