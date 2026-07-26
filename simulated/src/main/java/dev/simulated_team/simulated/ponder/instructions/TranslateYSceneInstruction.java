package dev.simulated_team.simulated.ponder.instructions;

import dev.simulated_team.simulated.mixin_interface.ponder.PonderSceneExtension;
import it.unimi.dsi.fastutil.floats.FloatUnaryOperator;
import com.zurrtum.create.client.ponder.foundation.PonderScene;
import com.zurrtum.create.client.ponder.foundation.instruction.TickingInstruction;
import net.minecraft.util.Mth;

public class TranslateYSceneInstruction extends TickingInstruction {
    float yOffset;
    float oldY;

    int ticks;
    int progress;
    FloatUnaryOperator smoothing;

    public TranslateYSceneInstruction(final float yOffset, final int ticks) {
        this(yOffset, ticks, f -> f);
    }

    public TranslateYSceneInstruction(final float yOffset, final int ticks, final FloatUnaryOperator smoothing) {
        super(false, ticks + 1);
        this.yOffset = yOffset;
        this.ticks = ticks;
        this.smoothing = smoothing;
    }

    @Override
    public void onScheduled(final PonderScene scene) {
        super.onScheduled(scene);
        ((PonderSceneExtension) scene).simulated$setYOffset(0);
    }

    @Override
    protected void firstTick(final PonderScene scene) {
        super.firstTick(scene);
        this.oldY = scene.getYOffset();
        this.progress = 0;
    }

    @Override
    public void tick(final PonderScene scene) {
        super.tick(scene);
        this.progress++;
        float percentage = Mth.clamp((float) this.progress / this.ticks, 0, 1);
        percentage = this.smoothing.apply(percentage);
        final float currentScale = (this.yOffset - this.oldY) * percentage + this.oldY;
        ((PonderSceneExtension) scene).simulated$setYOffset(currentScale);
    }
}
