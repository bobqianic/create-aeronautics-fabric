package dev.simulated_team.simulated.mixin.accessor;

import com.zurrtum.create.content.redstone.link.RedstoneLinkBlockEntity;
import com.zurrtum.create.content.redstone.link.ServerLinkBehaviour;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(RedstoneLinkBlockEntity.class)
public interface RedstoneLinkBlockEntityAccessor {

    @Accessor("link")
    ServerLinkBehaviour getLink();


}
