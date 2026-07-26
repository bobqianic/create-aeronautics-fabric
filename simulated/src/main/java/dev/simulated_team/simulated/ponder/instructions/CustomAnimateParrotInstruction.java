package dev.simulated_team.simulated.ponder.instructions;

import it.unimi.dsi.fastutil.floats.FloatUnaryOperator;
import com.zurrtum.create.client.ponder.api.element.ElementLink;
import com.zurrtum.create.client.ponder.api.element.ParrotElement;
import net.minecraft.world.phys.Vec3;

import java.util.function.BiConsumer;
import java.util.function.Function;

public class CustomAnimateParrotInstruction extends CustomAnimateElementInstruction<ParrotElement> {

    protected CustomAnimateParrotInstruction(
            final ElementLink<ParrotElement> link,
            final Vec3 totalDelta,
            final int ticks,
            final BiConsumer<ParrotElement, Vec3> setter,
            final Function<ParrotElement, Vec3> getter,
            final FloatUnaryOperator positionFunc) {
        super(link, totalDelta, ticks, setter, getter, positionFunc);
    }

    public static CustomAnimateParrotInstruction move(
            final ElementLink<ParrotElement> link,
            final Vec3 offset,
            final int ticks,
            final FloatUnaryOperator positionFunc) {
        return new CustomAnimateParrotInstruction(link, offset, ticks, (wse, v) -> wse.setPositionOffset(v, ticks == 0),
                ParrotElement::getPositionOffset, positionFunc);
    }
}
