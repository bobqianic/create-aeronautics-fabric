package dev.simulated_team.simulated.ponder.instructions;

import dev.simulated_team.simulated.content.blocks.nav_table.NavTableBlockEntity;
import com.zurrtum.create.client.ponder.api.element.WorldSectionElement;
import com.zurrtum.create.client.ponder.api.level.PonderLevel;
import com.zurrtum.create.client.ponder.foundation.PonderScene;
import com.zurrtum.create.client.ponder.foundation.instruction.TickingInstruction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;

public class NavTableRotationInstruction extends TickingInstruction {
    protected final BlockPos location;
    protected final int ticks;
    protected final int angle;
    protected float initialAngle;
    protected int progress;

    protected WorldSectionElement element;

    public NavTableRotationInstruction(final BlockPos location, final int angle, final int ticks) {
        super(false, ticks);
        this.location = location;
        this.angle = angle;
        this.ticks = ticks;
    }

    @Override
    protected void firstTick(final PonderScene scene) {
        super.firstTick(scene);
        this.progress = 0;

        final BlockEntity be = scene.getWorld().getBlockEntity(this.location);

        if (be instanceof final NavTableBlockEntity nbe) {
            this.initialAngle = nbe.lerpedAngleDegrees.getValue();
        }
    }

    @Override
    public void tick(final PonderScene scene) {
        super.tick(scene);

        this.progress++;
        final float lerpedValue = (float) this.progress / this.ticks;

        final PonderLevel level = scene.getWorld();
        final BlockEntity be = level.getBlockEntity(this.location);

        if (be instanceof final NavTableBlockEntity nbe) {
            nbe.forceCurrentAngle(this.initialAngle + this.angle * lerpedValue);
        }
    }
}
