package dev.simulated_team.simulated.ponder.instructions;

import dev.simulated_team.simulated.mixin_interface.ponder.PonderSceneExtension;
import com.zurrtum.create.client.ponder.foundation.PonderScene;
import com.zurrtum.create.client.ponder.foundation.instruction.TickingInstruction;

public class ScaleSceneInstruction extends TickingInstruction {

    private final float scaleFactor;
    private float oldScale;

    private final int ticks;
    private int progress;

    public ScaleSceneInstruction(final float scaleFactor, final int ticks) {
        super(false, ticks);
        this.scaleFactor = scaleFactor;
        this.ticks = ticks;
    }

    @Override
    public void onScheduled(final PonderScene scene) {
        super.onScheduled(scene);
        ((PonderSceneExtension) scene).simulated$setScaleFactor(1);
    }

    @Override
    protected void firstTick(final PonderScene scene) {
        super.firstTick(scene);
        this.oldScale = scene.getScaleFactor();
        this.progress = 0;
    }

    @Override
    public void tick(final PonderScene scene) {
        super.tick(scene);
        this.progress++;
        float percentage = ((float) this.progress / this.ticks);
        percentage = percentage * percentage * (3 - 2 * percentage);
        final float currentScale = (this.scaleFactor - this.oldScale) * percentage + this.oldScale;
        ((PonderSceneExtension) scene).simulated$setScaleFactor(currentScale);
    }
}
