package dev.eriksonn.aeronautics.mixin.create_sublevel;

import com.zurrtum.create.content.kinetics.belt.transport.BeltMovementHandler.TransportedEntityInfo;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = TransportedEntityInfo.class, remap = false)
public interface TransportedEntityInfoAccessor {

    @Accessor("lastCollidedPos")
    BlockPos aeronautics$getLastCollidedPos();
}
