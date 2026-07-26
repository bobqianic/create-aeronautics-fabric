package dev.eriksonn.aeronautics.content.ponder.instructions;

import dev.eriksonn.aeronautics.mixin.ponder.TickingInstructionAccessor;
import dev.eriksonn.aeronautics.mixinterface.TickingInstructionExtension;
import com.zurrtum.create.client.ponder.foundation.PonderScene;
import com.zurrtum.create.client.ponder.foundation.instruction.AnimateElementInstruction;
import com.zurrtum.create.client.ponder.foundation.instruction.PonderInstruction;
import com.zurrtum.create.client.ponder.foundation.instruction.TickingInstruction;

public class TickingStoppingInstruction extends PonderInstruction {

    final TickingInstruction instruction;
    public TickingStoppingInstruction(TickingInstruction instruction)
    {
        this.instruction = instruction;
    }
    @Override
    public boolean isComplete() {
        return true;
    }

    @Override
    public void tick(PonderScene scene) {
        ((TickingInstructionExtension)instruction).aeronautics$stopInstruction();
    }
}
