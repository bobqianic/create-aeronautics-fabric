package dev.simulated_team.simulated.mixin.create_assembly;

import com.zurrtum.create.content.contraptions.elevator.ElevatorPulleyBlockEntity;
import dev.simulated_team.simulated.mixin_interface.create_assembly.IControlContraptionExtension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = ElevatorPulleyBlockEntity.class, remap = false)
public abstract class ElevatorPulleyBlockEntityMixin implements IControlContraptionExtension  {
    @Shadow public abstract void disassemble();

    @Override
    public void sable$disassemble() {
        this.disassemble();
    }
}
