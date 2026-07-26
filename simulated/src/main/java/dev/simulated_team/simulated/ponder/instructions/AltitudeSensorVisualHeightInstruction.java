package dev.simulated_team.simulated.ponder.instructions;

import dev.simulated_team.simulated.content.blocks.altitude_sensor.AltitudeSensorBlockEntity;
import it.unimi.dsi.fastutil.floats.FloatUnaryOperator;
import com.zurrtum.create.client.ponder.api.level.PonderLevel;
import com.zurrtum.create.client.ponder.foundation.PonderScene;
import com.zurrtum.create.client.ponder.foundation.instruction.TickingInstruction;
import net.minecraft.core.BlockPos;

public abstract class AltitudeSensorVisualHeightInstruction extends TickingInstruction {

    protected final BlockPos location;
    protected final float startValue;
    protected final float endValue;
    protected final FloatUnaryOperator interpolation;

    public AltitudeSensorVisualHeightInstruction(final BlockPos location, final int ticks, final float startValue, final float endValue, final FloatUnaryOperator interpolation) {
        super(false, ticks);
        this.location = location;
        this.startValue = startValue;
        this.endValue = endValue;
        this.interpolation = interpolation;
    }

    public float getLerpedValue() {
        if (this.totalTicks != 0) {
            return this.startValue + (this.endValue - this.startValue) * this.interpolation.apply(1f - (float) this.remainingTicks / this.totalTicks);
        }
        return this.endValue;
    }

    public static class Linear extends AltitudeSensorVisualHeightInstruction {
        public Linear(final BlockPos location, final int ticks, final float startValue, final float endValue, final FloatUnaryOperator interpolation) {
            super(location, ticks, startValue, endValue, interpolation);
        }

        @Override
        protected void firstTick(final PonderScene scene) {
            super.firstTick(scene);
            final PonderLevel world = scene.getWorld();
            if (world.getBlockEntity(this.location) instanceof final AltitudeSensorBlockEntity be) {
                be.updateVisualHeight = true;
                be.previousVisualHeight = this.location.getY();
                be.visualHeight = this.location.getY();
            }
        }

        @Override
        public void tick(final PonderScene scene) {
            super.tick(scene);
            final PonderLevel world = scene.getWorld();
            if (world.getBlockEntity(this.location) instanceof final AltitudeSensorBlockEntity be) {
                final float targetValue = this.getLerpedValue();
                be.lowSignal = be.toNormalHeight(this.location.getY() - targetValue);
                be.highSignal = be.toNormalHeight(this.location.getY() - targetValue + 1);
            }
        }
    }

    public static class Radial extends AltitudeSensorVisualHeightInstruction {

        // it seems to jump at the end sometimes and idk why - works good enough still, just fudge the start/end values
        public Radial(final BlockPos location, final int ticks, final float startValue, final float endValue, final FloatUnaryOperator interpolation) {
            super(location, ticks, startValue, endValue, interpolation);
        }

        @Override
        protected void firstTick(final PonderScene scene) {
            super.firstTick(scene);
            final PonderLevel world = scene.getWorld();
            if (world.getBlockEntity(this.location) instanceof final AltitudeSensorBlockEntity be) {
                be.updateVisualHeight = false;
                be.previousVisualHeight = this.startValue;
                be.visualHeight = this.endValue;
            }
        }

        @Override
        public void tick(final PonderScene scene) {
            super.tick(scene);
            final PonderLevel world = scene.getWorld();
            if (world.getBlockEntity(this.location) instanceof final AltitudeSensorBlockEntity be) {
                final float targetValue = this.getLerpedValue();
                be.visualHeight = targetValue;
            }
        }
    }
}
