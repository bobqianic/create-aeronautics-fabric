package dev.simulated_team.simulated.ponder.instructions;

import dev.simulated_team.simulated.ponder.elements.rope.RopeStrandElement;
import com.zurrtum.create.client.ponder.foundation.instruction.FadeIntoSceneInstruction;
import net.minecraft.core.Direction;

public class CreateRopeStrandInstruction extends FadeIntoSceneInstruction<RopeStrandElement> {
    public CreateRopeStrandInstruction(final int fadeInTicks, final Direction fadeInFrom, final RopeStrandElement element) {
        super(fadeInTicks, fadeInFrom, element);
    }

    public CreateRopeStrandInstruction(final RopeStrandElement element) {
        super(0, Direction.DOWN, element);
    }

    @Override
    protected Class<RopeStrandElement> getElementClass() {
        return RopeStrandElement.class;
    }
}
