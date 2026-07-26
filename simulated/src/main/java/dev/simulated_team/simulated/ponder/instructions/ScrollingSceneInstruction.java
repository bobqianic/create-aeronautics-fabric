package dev.simulated_team.simulated.ponder.instructions;

import dev.simulated_team.simulated.ponder.SmoothMovementUtils;
import dev.simulated_team.simulated.ponder.records.ScrollingSceneRecord;
import com.zurrtum.create.client.ponder.foundation.PonderScene;
import com.zurrtum.create.client.ponder.foundation.instruction.PonderInstruction;
import net.minecraft.world.phys.Vec3;

public class ScrollingSceneInstruction extends PonderInstruction {

    public ScrollingSceneInstruction(final ScrollingSceneRecord scrollingScene, final boolean moveCloseToFar) {
        final Vec3 movement = Vec3.atLowerCornerOf(scrollingScene.directionTravelling().getUnitVec3i().multiply(scrollingScene.groundLength()));

        scrollingScene.scene().addInstruction(CustomAnimateWorldSectionInstruction.move(moveCloseToFar ? scrollingScene.groundClose() : scrollingScene.groundFar(), movement.scale(-2f), 0, SmoothMovementUtils.linear()));

        scrollingScene.scene().addInstruction(CustomAnimateWorldSectionInstruction.move(scrollingScene.groundClose(), movement, scrollingScene.ticksPerCycle(), SmoothMovementUtils.linear()));
        scrollingScene.scene().addInstruction(CustomAnimateWorldSectionInstruction.move(scrollingScene.groundFar(), movement, scrollingScene.ticksPerCycle(), SmoothMovementUtils.linear()));
    }

    @Override
    public boolean isComplete() {
        return true;
    }

    @Override
    public void tick(final PonderScene scene) {
    }
}