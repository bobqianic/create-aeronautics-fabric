package dev.simulated_team.simulated.ponder.instructions;

import dev.simulated_team.simulated.mixin_interface.ponder.PonderSceneExtension;
import com.zurrtum.create.client.ponder.foundation.PonderScene;
import com.zurrtum.create.client.ponder.foundation.instruction.TickingInstruction;
import net.minecraft.world.phys.Vec3;

import java.util.function.Function;
import java.util.function.UnaryOperator;

/**
 * Instructions to move the shadow of the ponder scene over timestamp<br>
 * Overlapping instructions behave additively (2 simultaneous instructions moving up by 1 block will result in moving up 2 blocks)
 */
public class CustomMoveBaseShadowInstruction extends TickingInstruction {
    protected Vec3 previousPos;
    protected Function<Vec3, Function<Float, Vec3>> initialPosFunc;
    protected Function<Float, Vec3> posFunc;

    /**
     * @param initialPosFunc initialPlatePos -> (fraction through duration -> change in position since start)
     * @param ticks duration
     */
    protected CustomMoveBaseShadowInstruction(final Function<Vec3, Function<Float, Vec3>> initialPosFunc, final int ticks) {
        super(false, ticks);
        this.initialPosFunc = initialPosFunc;
    }

    public static CustomMoveBaseShadowInstruction delta(final Vec3 delta, final int ticks, final UnaryOperator<Float> interpolation) {
        return new CustomMoveBaseShadowInstruction(v -> f -> {
            final float i = interpolation.apply(f);
            return delta.scale(i);
        }, ticks);
    }

    /**
     * Is not guaranteed to finish at the target if there are multiple overlapping instructions
     */
    public static CustomMoveBaseShadowInstruction to(final Vec3 target, final int ticks, final UnaryOperator<Float> interpolation) {
        return new CustomMoveBaseShadowInstruction(v -> f -> {
            final float i = interpolation.apply(f);
            return target.subtract(v).scale(i).add(v.scale(1 - i));
        }, ticks);
    }

    public static CustomMoveBaseShadowInstruction to(final Vec3 target) {
        return new CustomMoveBaseShadowInstruction(v -> f -> (target.subtract(v)), 1);
    }

    @Override
    protected void firstTick(final PonderScene scene) {
        super.firstTick(scene);
        this.previousPos = Vec3.ZERO;
        this.posFunc = this.initialPosFunc.apply(((PonderSceneExtension)scene).simulated$getShadowOffset(0));
    }

    @Override
    public void tick(final PonderScene scene) {
        super.tick(scene);
        final float f = 1 - (float) this.remainingTicks / this.totalTicks;
        final Vec3 pos = this.posFunc.apply(f);
        if (this.totalTicks <= 1) {
            ((PonderSceneExtension) scene).simulated$setShadowOffset(pos);
            ((PonderSceneExtension) scene).simulated$setOldShadowOffset(pos);
        } else {
            ((PonderSceneExtension) scene).simulated$moveShadowOffset(pos.subtract(this.previousPos));
        }
        this.previousPos = pos;
    }
}
