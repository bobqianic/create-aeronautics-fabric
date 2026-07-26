package dev.ryanhcode.sable.platform;

import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public interface SableSubLevelRenderPlatform {
    SableSubLevelRenderPlatform INSTANCE = SablePlatformUtil.load(SableSubLevelRenderPlatform.class);

    // PORT-NOTE(mc26.1): tesselateBlock/getRenderLayers removed — the
    // single-block draw path they served was deleted with the BakedModel/
    // ShaderInstance rework. Re-add when single-block rendering is rebuilt.

    void tryAddFlywheelVisual(final BlockEntity blockEntity);
}
