package dev.eriksonn.aeronautics.mixin.ponder;

import dev.eriksonn.aeronautics.mixinterface.TickingInstructionExtension;
import com.zurrtum.create.client.ponder.foundation.instruction.TickingInstruction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = TickingInstruction.class, remap = false)
public class TickingInstructionMixin implements TickingInstructionExtension {
    @Unique
    boolean aeronautics$isStopped;

    @Override
    public void aeronautics$stopInstruction() {
        aeronautics$isStopped = true;
    }
    @Inject(method = "isComplete",at = @At("HEAD"), cancellable = true)
    public void isComplete(CallbackInfoReturnable<Boolean> cir)
    {
        if(aeronautics$isStopped)
            cir.setReturnValue(true);
    }
}
