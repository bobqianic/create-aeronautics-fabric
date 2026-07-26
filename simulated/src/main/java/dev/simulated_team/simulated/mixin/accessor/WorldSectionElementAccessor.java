package dev.simulated_team.simulated.mixin.accessor;

import com.zurrtum.create.client.ponder.foundation.element.WorldSectionElementImpl;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(WorldSectionElementImpl.class)
public interface WorldSectionElementAccessor {

    @Accessor
    Vec3 getCenterOfRotation();
}
