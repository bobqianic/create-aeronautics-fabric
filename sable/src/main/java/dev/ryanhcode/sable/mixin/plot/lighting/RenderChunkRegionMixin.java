package dev.ryanhcode.sable.mixin.plot.lighting;

import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.mixinterface.plot.SubLevelContainerHolder;
import net.minecraft.client.renderer.chunk.RenderSectionRegion;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/**
 * PORT-NOTE(mc26.1): RenderChunkRegion was replaced by RenderSectionRegion.
 */
@Mixin(RenderSectionRegion.class)
public class RenderChunkRegionMixin implements SubLevelContainerHolder {

    @Shadow
    @Final
    private Level level;

    @Override
    public SubLevelContainer sable$getPlotContainer() {
        return SubLevelContainer.getContainer(this.level);
    }
}
