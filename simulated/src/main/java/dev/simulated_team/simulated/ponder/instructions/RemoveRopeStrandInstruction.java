package dev.simulated_team.simulated.ponder.instructions;

import com.zurrtum.create.client.foundation.ponder.CreateSceneBuilder;
import dev.simulated_team.simulated.ponder.elements.rope.RopeStrandElement;
import com.zurrtum.create.client.ponder.api.element.ElementLink;
import com.zurrtum.create.client.ponder.foundation.element.ElementLinkImpl;
import com.zurrtum.create.client.ponder.foundation.instruction.FadeOutOfSceneInstruction;
import net.minecraft.core.Direction;

public class RemoveRopeStrandInstruction extends FadeOutOfSceneInstruction<RopeStrandElement> {
    public RemoveRopeStrandInstruction(final int fadeInTicks, final Direction fadeInFrom, final RopeStrandElement element, final CreateSceneBuilder scene) {
        super(fadeInTicks, fadeInFrom, createLink(element, scene));
    }

    public RemoveRopeStrandInstruction(final RopeStrandElement element, final CreateSceneBuilder scene) {
        super(0, Direction.DOWN, createLink(element, scene));
    }

    private static ElementLink<RopeStrandElement> createLink(final RopeStrandElement element, final CreateSceneBuilder scene) {
        final ElementLink<RopeStrandElement> link = new ElementLinkImpl<>(RopeStrandElement.class);
        scene.addInstruction(ponderScene -> ponderScene.linkElement(element, link));
        return link;
    }
}
