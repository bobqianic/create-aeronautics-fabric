package dev.ryanhcode.sable.mixinterface.plot;

import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.ApiStatus;

/**
 * Internal API extension. Use {@link SubLevelContainer#getContainer(Level)}
 */
@ApiStatus.Internal
public interface SubLevelContainerHolder {

    SubLevelContainer sable$getPlotContainer();

}
