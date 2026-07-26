package dev.ryanhcode.sable.mixin.compatibility.scalablelux;

import ca.spottedleaf.starlight.common.light.BlockStarLightEngine;
import dev.ryanhcode.sable.render.SubLevelDynamicLights;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.chunk.LightChunkGetter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Supplies projected moving-sub-level emission to ScalableLux at the two
 * points where it establishes or recalculates a block-light source.
 *
 * <p>The local slots are stable for ScalableLux 0.1.6 (commit c25518a), which
 * is the version compiled and tested by this port.</p>
 */
@Mixin(value = BlockStarLightEngine.class, remap = false)
public abstract class BlockStarLightEngineMixin {
    @ModifyVariable(
            method = "checkBlock",
            at = @At("STORE"),
            index = 9,
            remap = false
    )
    private int sable$includeSubLevelEmissionOnCheck(
            final int original,
            final LightChunkGetter lightAccess,
            final int worldX,
            final int worldY,
            final int worldZ
    ) {
        return Math.max(
                original,
                SubLevelDynamicLights.getLightEmission(BlockPos.asLong(worldX, worldY, worldZ))
        );
    }

    @ModifyVariable(
            method = "calculateLightValue",
            at = @At("STORE"),
            index = 7,
            remap = false
    )
    private int sable$includeSubLevelEmissionOnRecalculate(
            final int original,
            final LightChunkGetter lightAccess,
            final int worldX,
            final int worldY,
            final int worldZ,
            final int expect
    ) {
        return Math.max(
                original,
                SubLevelDynamicLights.getLightEmission(BlockPos.asLong(worldX, worldY, worldZ))
        );
    }
}
