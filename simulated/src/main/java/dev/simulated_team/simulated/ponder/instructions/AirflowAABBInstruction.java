package dev.simulated_team.simulated.ponder.instructions;

import com.zurrtum.create.client.ponder.api.PonderPalette;
import com.zurrtum.create.client.ponder.foundation.PonderScene;
import com.zurrtum.create.client.ponder.foundation.instruction.TickingInstruction;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class AirflowAABBInstruction extends TickingInstruction {

    protected AABB bb;
    protected PonderPalette color;
    protected int hash;
    protected Direction direction;
    protected float speed;
    protected float spacing;

    protected boolean easeIn;
    protected boolean easeOut;

    public AirflowAABBInstruction(final PonderPalette color, final AABB bb, final int ticks, final Direction direction, final float speed, final float spacing) {
        this(color, bb, ticks, direction, speed, spacing, true, false);
    }

    public AirflowAABBInstruction(final PonderPalette color, final AABB bb, final int ticks, final Direction direction, final float speed, final float spacing, final boolean easeIn, final boolean easeOut) {
        super(false, ticks);

        this.bb = bb;
        this.color = color;

        this.speed = speed / 20;
        this.spacing = spacing;
        this.direction = direction;

        this.easeIn = easeIn;
        this.easeOut = easeOut;
    }

    @Override
    protected final void firstTick(final PonderScene scene) {
        super.firstTick(scene);
        this.hash = scene.getWorld().random.nextInt();
    }

    @Override
    public void tick(final PonderScene scene) {
        super.tick(scene);

        final int age = this.totalTicks - this.remainingTicks;
        final float offset = this.speed * age;
        final float totalOffset = this.totalTicks * this.speed;

        double length = Vec3.atLowerCornerOf(this.direction.getUnitVec3i()).dot(new Vec3(this.bb.getXsize(), this.bb.getYsize(), this.bb.getZsize()));
        length = Math.abs(length);
        final AABB commonBB = this.bb.contract(this.direction.getStepX() * length, this.direction.getStepY() * length, this.direction.getStepZ() * length);

        final int startIndex = (int) Math.ceil((this.easeIn ? Math.max(offset - length, 0) : offset - length) / this.spacing);
        final int endIndex = (int) Math.floor((this.easeOut ? Math.min(offset, totalOffset - length) : offset) / this.spacing);

        for (int i = startIndex; i <= endIndex; i++) {
            final double position = offset - i * this.spacing;
            final AABB currentBB = commonBB.move(Vec3.atLowerCornerOf(this.direction.getUnitVec3i()).scale(position));
            scene.getOutliner()
                    .chaseAABB(this.hash + i, currentBB)
                    .lineWidth(1 / 32f)
                    .colored(this.color.getColor());
        }
    }
}
