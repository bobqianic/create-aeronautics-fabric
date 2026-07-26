package dev.simulated_team.simulated.ponder.instructions;

import dev.simulated_team.simulated.ponder.outliners.LerpedLineOutline;
import dev.simulated_team.simulated.ponder.records.PonderLineRecord;
import it.unimi.dsi.fastutil.floats.FloatUnaryOperator;
import com.zurrtum.create.client.ponder.foundation.PonderScene;
import com.zurrtum.create.client.ponder.foundation.instruction.TickingInstruction;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class ChasingLineInstruction extends TickingInstruction {

    final PonderLineRecord startLine;
    final PonderLineRecord endLine;
    final int size;
    final int color;
    final String slot;
    final FloatUnaryOperator smoothing;
    final int lerpTicks;

    LerpedLineOutline lerpedLine;
    Vec3 oldLineStart;
    Vec3 oldLineEnd;

    // Animate between two lines
    public ChasingLineInstruction(final PonderLineRecord startLine, final PonderLineRecord endLine, final int size, final int color, final String slot, final int lerpTicks, final int holdTicks, final FloatUnaryOperator smoothing) {
        super(false, lerpTicks + holdTicks);
        this.startLine = startLine;
        this.endLine = endLine;
        this.size = size;
        this.color = color;
        this.slot = slot;
        this.smoothing = smoothing;
        this.lerpTicks = lerpTicks;
    }

    // Animate a single line stretching out
    public ChasingLineInstruction(final PonderLineRecord line, final int size, final int color, final String slot, final int lerpTicks, final int holdTicks, final FloatUnaryOperator smoothing) {
        super(false, lerpTicks + holdTicks);
        this.startLine = new PonderLineRecord(line.startPos(), line.startPos());
        this.endLine = line;
        this.size = size;
        this.color = color;
        this.slot = slot;
        this.smoothing = smoothing;
        this.lerpTicks = lerpTicks;
    }

    // Animate a single line stretching out
    public ChasingLineInstruction(final Vec3 startPos, final Vec3 endPos, final int size, final int color, final int lerpTicks, final int holdTicks, final FloatUnaryOperator smoothing) {
        super(false, lerpTicks + holdTicks);
        this.startLine = new PonderLineRecord(startPos, startPos);
        this.endLine = new PonderLineRecord(startPos, endPos);
        this.size = size;
        this.color = color;
        this.slot = startPos.toString();
        this.smoothing = smoothing;
        this.lerpTicks = lerpTicks;
    }

    public ChasingLineInstruction(final Vec3 startPos, final Vec3 endPos, final int size, final int color, final String slot, final int lerpTicks, final int holdTicks, final FloatUnaryOperator smoothing) {
        super(false, lerpTicks + holdTicks);
        this.startLine = new PonderLineRecord(startPos, startPos);
        this.endLine = new PonderLineRecord(startPos, endPos);
        this.size = size;
        this.color = color;
        this.slot = slot;
        this.smoothing = smoothing;
        this.lerpTicks = lerpTicks;
    }

    @Override
    protected void firstTick(final PonderScene scene) {
        super.firstTick(scene);

        this.oldLineStart = this.startLine.startPos();
        this.oldLineEnd = this.startLine.endPos();

        this.lerpedLine = new LerpedLineOutline(this.startLine);
    }

    @Override
    public void tick(final PonderScene scene) {
        super.tick(scene);

        float percentage = Mth.clamp((((float) (this.totalTicks - this.remainingTicks) / (float) this.lerpTicks)), 0, 1);

        percentage = this.smoothing.apply(percentage);

        final Vec3 currentStartPos = (this.endLine.startPos().subtract(this.startLine.startPos())).scale(percentage).add(this.startLine.startPos());
        final Vec3 currentEndPos = (this.endLine.endPos().subtract(this.startLine.endPos())).scale(percentage).add(this.startLine.endPos());

        this.lerpedLine.update(this.oldLineStart, this.oldLineEnd, currentStartPos, currentEndPos);

        scene.getOutliner()
                .showOutline(this.slot, this.lerpedLine)
                .lineWidth(this.size / 16f)
                .colored(this.color);

        this.oldLineStart = currentStartPos;
        this.oldLineEnd = currentEndPos;
    }
}
