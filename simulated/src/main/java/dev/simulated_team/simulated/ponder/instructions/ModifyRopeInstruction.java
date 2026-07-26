package dev.simulated_team.simulated.ponder.instructions;

import com.zurrtum.create.client.foundation.ponder.CreateSceneBuilder;
import dev.ryanhcode.sable.companion.math.JOMLConversion;
import dev.simulated_team.simulated.ponder.SmoothMovementUtils;
import dev.simulated_team.simulated.ponder.elements.rope.PonderRopePose;
import dev.simulated_team.simulated.ponder.elements.rope.RopeStrandElement;
import it.unimi.dsi.fastutil.floats.FloatUnaryOperator;
import com.zurrtum.create.client.ponder.foundation.PonderScene;
import com.zurrtum.create.client.ponder.foundation.instruction.TickingInstruction;
import net.minecraft.world.phys.Vec3;

public class ModifyRopeInstruction extends TickingInstruction {
    private final RopeStrandElement rope;
    public PonderRopePose targetPose;
    public PonderRopePose currentPose = new PonderRopePose();
    public PonderRopePose startPose = new PonderRopePose();
    private FloatUnaryOperator interpolator = SmoothMovementUtils.linear();

    public ModifyRopeInstruction(final int ticks, final RopeStrandElement rope, final Vec3 from, final Vec3 to, final double length, final double sog, final double floorDistance) {
        super(false, ticks);
        this.rope = rope;

        this.targetPose = new PonderRopePose(JOMLConversion.toJOML(from), JOMLConversion.toJOML(to), length, sog, floorDistance);
    }

    public ModifyRopeInstruction(final int duration, final RopeStrandElement rope) {
        this(duration, rope, JOMLConversion.toMojang(rope.scenePose.start), JOMLConversion.toMojang(rope.scenePose.end), rope.scenePose.length, rope.scenePose.sog, rope.scenePose.floorHeight);
    }

    public ModifyRopeInstruction setStart(final Vec3 start) {
        this.targetPose.start.set(JOMLConversion.toJOML(start));
        this.rope.scenePose.start.set(JOMLConversion.toJOML(start));
        return this;
    }

    public ModifyRopeInstruction setEnd(final Vec3 end) {
        this.targetPose.end.set(JOMLConversion.toJOML(end));
        this.rope.scenePose.end.set(JOMLConversion.toJOML(end));
        return this;
    }

    public ModifyRopeInstruction setLength(final double length) {
        this.targetPose.length = length;
        this.rope.scenePose.length = length;
        return this;
    }

    public ModifyRopeInstruction setSog(final double sog) {
        this.targetPose.sog = sog;
        this.rope.scenePose.sog = sog;
        return this;
    }

    public ModifyRopeInstruction setInterpolator(final FloatUnaryOperator interpolator) {
        this.interpolator = interpolator;
        return this;
    }

    public ModifyRopeInstruction start(final CreateSceneBuilder scene) {
        scene.addInstruction(this);
        return this;
    }

    @Override
    public void reset(final PonderScene scene) {
        super.reset(scene);
    }

    @Override
    protected void firstTick(final PonderScene scene) {
        super.firstTick(scene);

        this.startPose.set(this.rope.pose);
        this.currentPose.set(this.rope.pose);
    }

    @Override
    public void tick(final PonderScene scene) {
        super.tick(scene);
        final int ticks = this.totalTicks - this.remainingTicks;
        double t = (double) ticks / this.totalTicks;
        t = this.interpolator.apply((float) t);

        this.startPose.lerp(this.startPose, this.targetPose, this.currentPose, t);
        this.rope.set(this.currentPose);

        if(this.remainingTicks == 0) {
            this.rope.set(this.currentPose);
        }
    }
}
