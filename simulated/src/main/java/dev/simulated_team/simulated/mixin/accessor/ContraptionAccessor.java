package dev.simulated_team.simulated.mixin.accessor;

import com.zurrtum.create.content.contraptions.Contraption;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(Contraption.class)
public interface ContraptionAccessor {

    @Accessor("superglue")
    List<AABB> getSuperGlue();

}
