package dev.simulated_team.simulated.ponder.instructions;

import com.zurrtum.create.client.ponder.api.PonderPalette;
import com.zurrtum.create.client.ponder.api.element.ElementLink;
import com.zurrtum.create.client.ponder.api.element.WorldSectionElement;
import com.zurrtum.create.client.ponder.foundation.PonderScene;
import com.zurrtum.create.client.ponder.foundation.instruction.TickingInstruction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Objects;

public class ChaseAABBWithLinkInstruction extends TickingInstruction {

    protected final ElementLink<WorldSectionElement> elementLink;
    protected WorldSectionElement element;
    protected final AABB bb;
    protected final Object slot;
    protected final PonderPalette color;
    protected double timeShift;
    protected Vec3 previousOffset;

    public ChaseAABBWithLinkInstruction(final ElementLink<WorldSectionElement> elementLink, final PonderPalette color, final Object slot, final AABB bb, final int ticks, final double timeShift) {
        super(false, ticks);
        this.elementLink = elementLink;
        this.color = color;
        this.slot = slot;
        this.bb = bb;
        this.timeShift = timeShift;
    }

    @Override
    protected final void firstTick(final PonderScene scene) {
        super.firstTick(scene);
        this.element = Objects.requireNonNull(scene.resolve(this.elementLink), "elementLink");
        this.previousOffset = this.element.getAnimatedOffset();
    }

    @Override
    public void tick(final PonderScene scene) {
        super.tick(scene);
        final Vec3 offset = this.element.getAnimatedOffset();
        final Vec3 shiftedOffset = this.previousOffset.lerp(offset, this.timeShift);
        this.previousOffset = offset;
        final AABB offsetBB = this.bb.move(shiftedOffset);
        scene.getOutliner()
                .chaseAABB(this.slot, offsetBB)
                .lineWidth(1 / 16f)
                .colored(this.color.getColor());
    }
}
