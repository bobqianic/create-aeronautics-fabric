package dev.simulated_team.simulated.ponder.instructions;

import dev.simulated_team.simulated.content.blocks.docking_connector.DockingConnectorBlockEntity;
import com.zurrtum.create.client.ponder.api.level.PonderLevel;
import com.zurrtum.create.client.ponder.foundation.PonderScene;
import com.zurrtum.create.client.ponder.foundation.instruction.PonderInstruction;
import net.minecraft.core.BlockPos;

public class LinkDockingConnectorsInstruction extends PonderInstruction {
    final BlockPos fromPos;
    final BlockPos toPos;

    public LinkDockingConnectorsInstruction(BlockPos fromPos, BlockPos toPos) {
        this.fromPos = fromPos;
        this.toPos = toPos;
    }

    @Override
    public boolean isComplete() {
        return true;
    }

    @Override
    public void tick(PonderScene scene) {
        final PonderLevel world = scene.getWorld();

        if (world.getBlockEntity(fromPos) instanceof final DockingConnectorBlockEntity be1 && world.getBlockEntity(toPos) instanceof final DockingConnectorBlockEntity be2)
            be1.tank.connect(toPos, be2.tank);
    }
}
