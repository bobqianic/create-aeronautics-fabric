package dev.simulated_team.simulated.mixin.accessor;

import com.zurrtum.create.content.contraptions.ControlledContraptionEntity;
import com.zurrtum.create.content.contraptions.StructureTransform;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ControlledContraptionEntity.class)
public interface ControlledContraptionEntityAccessor {

    @Accessor
    BlockPos getControllerPos();

    @Invoker
    StructureTransform invokeMakeStructureTransform();

}
