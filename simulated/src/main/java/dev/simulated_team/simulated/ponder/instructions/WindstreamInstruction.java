package dev.simulated_team.simulated.ponder.instructions;

import dev.simulated_team.simulated.ponder.outliners.LerpedLineOutline;
import dev.simulated_team.simulated.ponder.records.PonderLineRecord;
import com.zurrtum.create.client.ponder.api.PonderPalette;
import com.zurrtum.create.client.ponder.foundation.PonderScene;
import com.zurrtum.create.client.ponder.foundation.instruction.TickingInstruction;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class WindstreamInstruction extends TickingInstruction {

    PonderLineRecord line;
    final int size;
    final PonderPalette color;
    final String slot;
    final int lerpTicks;
    final AABB bb;
    final Vec3 windDir;

    LerpedLineOutline lerpedLine;
    Vec3 oldLineStart;
    Vec3 oldLineEnd;

    public WindstreamInstruction(final AABB bb, final Vec3 windDir, final int size, final PonderPalette color, final String slot, final int lerpTicks, final int holdTicks) {
        super(false, 2 * lerpTicks + holdTicks + 1);
        this.size = size;
        this.color = color;
        this.slot = slot;
        this.lerpTicks = lerpTicks;
        this.bb = bb;
        this.windDir = windDir;
    }

    @Override
    protected void firstTick(final PonderScene scene) {
        super.firstTick(scene);
        final Vec3 lineBase = new Vec3(
                (this.bb.maxX - this.bb.minX) * scene.getWorld().getRandom().nextDouble() + this.bb.minX,
                (this.bb.maxY - this.bb.minY) * scene.getWorld().getRandom().nextDouble() + this.bb.minY,
                (this.bb.maxZ - this.bb.minZ) * scene.getWorld().getRandom().nextDouble() + this.bb.minZ
        );
        this.line = PonderLineRecord.withOffset(lineBase, lineBase.add(this.windDir));

        final Vec3 offset = new Vec3(87, 0, 0);
        this.oldLineStart = lineBase.add(offset);
        this.oldLineEnd = lineBase.add(offset);
        this.lerpedLine = new LerpedLineOutline(this.line);
    }

    @Override
    public void tick(final PonderScene scene) {
        super.tick(scene);

        final float inLerp = Mth.clamp((((float) (this.totalTicks - this.remainingTicks) / (float) this.lerpTicks)), 0, 1);
        final float outLerp = Mth.clamp((((float) (this.remainingTicks - 1) / (float) this.lerpTicks)), 0, 1);

        final float percentage = Math.min(inLerp, outLerp);

        final Vec3 currentStartPos = (this.line.startPos().subtract(this.line.endPos())).scale(percentage).add(this.line.endPos());
        final Vec3 currentEndPos = (this.line.endPos().subtract(this.line.startPos())).scale(percentage).add(this.line.startPos());

        this.lerpedLine.update(
                outLerp < inLerp ? this.line.endPos() : this.oldLineEnd,
                outLerp < inLerp ? this.oldLineStart : this.line.startPos(),
                outLerp < inLerp ? this.line.endPos() : currentEndPos,
                outLerp < inLerp ? currentStartPos : this.line.startPos()
        );

        scene.getOutliner()
                .showOutline(this.slot, this.lerpedLine)
                .lineWidth(this.size / 16f)
                .colored(this.color.getColor());

        this.oldLineStart = currentStartPos;
        this.oldLineEnd = currentEndPos;
    }
}
