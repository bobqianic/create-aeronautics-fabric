package dev.simulated_team.simulated.ponder.records;

import com.zurrtum.create.client.foundation.ponder.CreateSceneBuilder;
import com.zurrtum.create.client.ponder.api.element.ElementLink;
import com.zurrtum.create.client.ponder.api.element.WorldSectionElement;
import net.minecraft.core.Direction;

public record ScrollingSceneRecord(CreateSceneBuilder scene, ElementLink<WorldSectionElement> groundClose, ElementLink<WorldSectionElement> groundFar, Direction directionTravelling, int groundLength, int ticksPerCycle) {
}
