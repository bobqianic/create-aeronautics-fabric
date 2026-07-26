package dev.eriksonn.aeronautics.content.ponder.instructions;

import dev.eriksonn.aeronautics.content.blocks.propeller.bearing.propeller_bearing.PropellerBearingBlockEntity;
import com.zurrtum.create.client.ponder.foundation.PonderScene;
import com.zurrtum.create.client.ponder.foundation.instruction.PonderInstruction;
import net.minecraft.core.BlockPos;

public class SetPropellerSailsInstruction extends PonderInstruction {

    BlockPos pos;
    float sails;
    public SetPropellerSailsInstruction(BlockPos pos,float sails)
    {
        this.pos = pos;
        this.sails = sails;
    }
    @Override
    public boolean isComplete() {
        return true;
    }

    @Override
    public void tick(PonderScene scene) {
        if(scene.getWorld().getBlockEntity(pos) instanceof PropellerBearingBlockEntity propeller)
        {
            propeller.totalSailPower = sails;
        }
    }
}
