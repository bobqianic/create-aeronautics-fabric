package dev.simulated_team.simulated.ponder.instructions;

import it.unimi.dsi.fastutil.floats.FloatUnaryOperator;
import com.zurrtum.create.client.ponder.api.element.ElementLink;
import com.zurrtum.create.client.ponder.api.element.WorldSectionElement;
import net.minecraft.world.phys.Vec3;

import java.util.function.BiConsumer;
import java.util.function.Function;

public class CustomAnimateWorldSectionInstruction extends CustomAnimateElementInstruction<WorldSectionElement> {

    protected CustomAnimateWorldSectionInstruction(
            final ElementLink<WorldSectionElement> link,
            final Vec3 totalDelta,
            final int ticks,
            final BiConsumer<WorldSectionElement, Vec3> setter,
            final Function<WorldSectionElement, Vec3> getter,
            final FloatUnaryOperator positionFunc) {
        super(link, totalDelta, ticks, setter, getter, positionFunc);
    }

    public static CustomAnimateWorldSectionInstruction rotate(
            final ElementLink<WorldSectionElement> link,
            final Vec3 rotation,
            final int ticks,
            final FloatUnaryOperator positionFunc) {
        return new CustomAnimateWorldSectionInstruction(link, rotation, ticks,
                (wse, v) -> wse.setAnimatedRotation(v, ticks == 0), WorldSectionElement::getAnimatedRotation, positionFunc);
    }

    public static CustomAnimateWorldSectionInstruction move(
            final ElementLink<WorldSectionElement> link,
            final Vec3 offset,
            final int ticks,
            final FloatUnaryOperator positionFunc) {
        return new CustomAnimateWorldSectionInstruction(link, offset, ticks, (wse, v) -> wse.setAnimatedOffset(v, ticks == 0),
                WorldSectionElement::getAnimatedOffset, positionFunc);
    }
}
