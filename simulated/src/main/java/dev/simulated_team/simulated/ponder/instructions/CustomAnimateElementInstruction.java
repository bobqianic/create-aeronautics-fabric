package dev.simulated_team.simulated.ponder.instructions;

import com.zurrtum.create.client.ponder.api.element.ElementLink;
import com.zurrtum.create.client.ponder.api.element.PonderSceneElement;
import com.zurrtum.create.client.ponder.foundation.PonderScene;
import com.zurrtum.create.client.ponder.foundation.instruction.TickingInstruction;
import net.minecraft.world.phys.Vec3;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.UnaryOperator;

public class CustomAnimateElementInstruction<T extends PonderSceneElement> extends TickingInstruction {

    protected Vec3 deltaPerTick;
    protected Vec3 totalDelta;
    protected Vec3 target;
    protected ElementLink<T> link;
    protected T element;

    private final BiConsumer<T, Vec3> setter;
    private final Function<T, Vec3> getter;
    private final UnaryOperator<Float> positionFunc;

    protected CustomAnimateElementInstruction(final ElementLink<T> link, final Vec3 totalDelta, final int ticks,
                                              final BiConsumer<T, Vec3> setter, final Function<T, Vec3> getter, final UnaryOperator<Float> positionFunc) {
        super(false, ticks);
        this.link = link;
        this.setter = setter;
        this.getter = getter;
        this.totalDelta = totalDelta;
        this.deltaPerTick = totalDelta.scale(1d / ticks);
        this.target = totalDelta;
        this.positionFunc = positionFunc;
    }


    @Override
    protected final void firstTick(final PonderScene scene) {
        super.firstTick(scene);
        this.element = scene.resolve(this.link);
        if (this.element == null)
            return;
        this.target = this.getter.apply(this.element)
                .add(this.totalDelta);
    }

    @Override
    public void tick(final PonderScene scene) {
        super.tick(scene);
        if (this.element == null)
            return;

        if (this.totalTicks == 0) {
            this.setter.accept(this.element, this.getter.apply(this.element)
                    .add(this.totalDelta.scale(1)));
            return;
        }

        final int time = this.totalTicks - this.remainingTicks - 1;
        final float P1 = this.positionFunc.apply(time / (float) this.totalTicks);
        final float P2 = this.positionFunc.apply((time + 1) / (float) this.totalTicks);

        final float delta = P2 - P1;

        this.setter.accept(this.element, this.getter.apply(this.element)
                .add(this.totalDelta.scale(delta)));

    }
}
