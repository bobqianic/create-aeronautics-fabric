package dev.ryanhcode.sable.mixin.compatibility.scalablelux;

import ca.spottedleaf.starlight.common.light.BlockStarLightEngine;
import ca.spottedleaf.starlight.common.light.StarLightEngine;
import dev.ryanhcode.sable.render.SubLevelDynamicLights;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Desc;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Makes ScalableLux's cached block-state reads see the projected solid blocks
 * of moving sub-levels while block light is being propagated.
 */
@Mixin(value = StarLightEngine.class, remap = false)
public abstract class StarLightEngineMixin {
    @Shadow
    protected int chunkOffsetX;

    @Shadow
    protected int chunkOffsetY;

    @Shadow
    protected int chunkOffsetZ;

    @Inject(
            target = @Desc(
                    value = "getBlockState",
                    ret = BlockState.class,
                    args = {int.class, int.class, int.class}
            ),
            at = @At("RETURN"),
            cancellable = true,
            remap = false
    )
    private void sable$includeSubLevelOcclusion(
            final int worldX,
            final int worldY,
            final int worldZ,
            final CallbackInfoReturnable<BlockState> cir
    ) {
        if (!((Object) this instanceof BlockStarLightEngine)) {
            return;
        }

        final BlockState projected =
                SubLevelDynamicLights.getOccludingBlockState(BlockPos.asLong(worldX, worldY, worldZ));
        if (projected != null) {
            cir.setReturnValue(projected);
        }
    }

    @Inject(
            target = @Desc(
                    value = "getBlockState",
                    ret = BlockState.class,
                    args = {int.class, int.class}
            ),
            at = @At("RETURN"),
            cancellable = true,
            remap = false
    )
    private void sable$includeCachedSubLevelOcclusion(
            final int sectionIndex,
            final int localIndex,
            final CallbackInfoReturnable<BlockState> cir
    ) {
        if (!((Object) this instanceof BlockStarLightEngine)) {
            return;
        }

        final int sectionX = sectionIndex % 5 - this.chunkOffsetX;
        final int sectionZ = sectionIndex / 5 % 5 - this.chunkOffsetZ;
        final int sectionY = sectionIndex / 25 - this.chunkOffsetY;
        final int worldX = (sectionX << 4) | (localIndex & 15);
        final int worldY = (sectionY << 4) | ((localIndex >>> 8) & 15);
        final int worldZ = (sectionZ << 4) | ((localIndex >>> 4) & 15);

        final BlockState projected =
                SubLevelDynamicLights.getOccludingBlockState(BlockPos.asLong(worldX, worldY, worldZ));
        if (projected != null) {
            cir.setReturnValue(projected);
        }
    }
}
