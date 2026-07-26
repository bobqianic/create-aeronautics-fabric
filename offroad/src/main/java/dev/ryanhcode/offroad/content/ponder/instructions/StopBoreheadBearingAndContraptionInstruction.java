package dev.ryanhcode.offroad.content.ponder.instructions;

import dev.ryanhcode.offroad.content.blocks.borehead_bearing.BoreheadBearingBlockEntity;
import dev.ryanhcode.offroad.index.OffroadBlockEntityTypes;
import com.zurrtum.create.client.ponder.foundation.PonderScene;
import com.zurrtum.create.client.ponder.foundation.instruction.PonderInstruction;
import net.minecraft.core.BlockPos;

import java.util.Optional;

public class StopBoreheadBearingAndContraptionInstruction extends PonderInstruction {

    private final ChangeBoreheadAndContraptionSpeedInstruction instruction;
    private final BlockPos boreheadPos;

    private final boolean forced;

    public StopBoreheadBearingAndContraptionInstruction(final BlockPos boreheadPos, final ChangeBoreheadAndContraptionSpeedInstruction instruction, final boolean forced) {
        this.instruction = instruction;
        this.boreheadPos = boreheadPos;

        this.forced = forced;
    }

    @Override
    public boolean isComplete() {
        return true;
    }

    @Override
    public void tick(final PonderScene scene) {
        final Optional<BoreheadBearingBlockEntity> be = scene.getWorld().getBlockEntity(this.boreheadPos, OffroadBlockEntityTypes.BOREHEAD_BEARING.get());
        be.ifPresent(bhb -> bhb.setSpeed(0));

        if (this.forced) {
            this.instruction.forcedStop = true;
        } else {
            this.instruction.startSlowing = true;
        }
    }
}
