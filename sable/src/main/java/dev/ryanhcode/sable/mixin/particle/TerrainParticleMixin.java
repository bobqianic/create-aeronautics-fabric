package dev.ryanhcode.sable.mixin.particle;

import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.api.SubLevelHelper;
import dev.ryanhcode.sable.api.sublevel.ClientSubLevelContainer;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.ClientSubLevel;
import dev.ryanhcode.sable.sublevel.SubLevel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.TerrainParticle;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

// PORT-NOTE(mc26.1): TerrainParticle no longer overrides getLightColor (renamed getLightCoords on
// Particle, LevelRenderer.getLightColor renamed getLightCoords), so the old @Redirect target is gone.
// The sub-level light scaling is reinstated as a merged override of Particle.getLightCoords that
// mirrors the 1.21.1 TerrainParticle logic (light sampled at the source block pos).
@Mixin(TerrainParticle.class)
public abstract class TerrainParticleMixin extends Particle {

    @Shadow
    @Final
    private BlockPos pos;

    protected TerrainParticleMixin(final ClientLevel clientLevel, final double d, final double e, final double f) {
        super(clientLevel, d, e, f);
    }

    @Override
    protected int getLightCoords(final float partialTick) {
        final int existingColor = super.getLightCoords(partialTick);

        if (!this.level.hasChunkAt(this.pos)) {
            return existingColor;
        }

        final ClientSubLevelContainer container = SubLevelContainer.getContainer(Minecraft.getInstance().level);
        assert container != null;
        final SubLevel subLevel = Sable.HELPER.getContainingClient(this.pos);

        if (subLevel instanceof final ClientSubLevel clientSubLevel) {
            final int color = LevelRenderer.getLightCoords(this.level, this.pos);
            return clientSubLevel.scaleLightColor(color);
        } else if (container.inBounds(this.pos)) {
            return existingColor;
        }

        return LevelRenderer.getLightCoords(this.level, this.pos);
    }

}
