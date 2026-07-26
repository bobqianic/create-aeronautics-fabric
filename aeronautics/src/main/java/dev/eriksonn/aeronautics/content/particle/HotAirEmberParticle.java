
package dev.eriksonn.aeronautics.content.particle;

import dev.ryanhcode.sable.api.particle.ParticleSubLevelKickable;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

public class HotAirEmberParticle extends SingleQuadParticle implements ParticleSubLevelKickable {
    private final boolean isSoul;

    protected HotAirEmberParticle(final ClientLevel level, final double x, final double y, final double z, final double xSpeed, final double ySpeed, final double zSpeed, final boolean isSoul, final TextureAtlasSprite sprite) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed, sprite);
        this.isSoul = isSoul;
        this.quadSize = 1.0f / 16.0f / 2.0f;
        this.lifetime = 18;

        final float randomStrength = 0.1f;
        this.xd = (Math.random() * (double)2.0F - (double)1.0F) * (double) randomStrength;
        this.yd = (Math.random() * (double)2.0F - (double)1.0F) * (double) randomStrength;
        this.zd = (Math.random() * (double)2.0F - (double)1.0F) * (double) randomStrength;
        final double d0 = (Math.random() + Math.random() + (double)1.0F) * (double)0.15F;
        final double d1 = Math.sqrt(this.xd * this.xd + this.yd * this.yd + this.zd * this.zd);
        this.xd = this.xd / d1 * d0 * (double) randomStrength;
        this.yd = this.yd / d1 * d0 * (double) randomStrength + 0.1f;
        this.zd = this.zd / d1 * d0 * (double) randomStrength;

        this.xd *= xSpeed;
        this.yd *= ySpeed;
        this.zd *= zSpeed;
    }

    protected Layer getLayer() {
        return Layer.TRANSLUCENT;
    }


    @Override
    public void tick() {
      super.tick();
        final int fadeOutTicks = 2;
        this.alpha = 1.0f - (float) Mth.clamp(this.age - (this.lifetime - fadeOutTicks), 0, 1) / fadeOutTicks;

    }

    public int getLightColor(final float partialTick) {
        final BlockPos blockpos = new BlockPos((int) this.x, (int) this.y, (int) this.z);
        return this.level.isLoaded(blockpos) ? (LevelRenderer.getLightColor(this.level, blockpos) | (15 << 4)) : 0;
    }


    public int getPalettePosition() {
        return Mth.floor((1.0f - (float) this.age / this.lifetime) * 7);
    }


    @Override
    protected float getU0() {
        return super.getU0() + (this.getPalettePosition() / 7.0f) * this.getSpriteWidth();
    }

    @Override
    protected float getU1() {
        return super.getU0() + this.getSpriteWidth() / 7.0f + (this.getPalettePosition() / 7.0f) * this.getSpriteWidth();
    }

    private float getSpriteWidth() {
        return super.getU1() - super.getU0();
    }

    @Override
    protected float getV0() {
        return super.getV0() + this.getSpritePixelHeight() * (this.isSoul ? 1.0f: 0.0f);
    }

    @Override
    protected float getV1() {
        return super.getV0() + this.getSpritePixelHeight() * (this.isSoul ? 2.0f: 1.0f);
    }

    private float getSpritePixelHeight() {
        return (super.getV1() - super.getV0()) / 2.0f;
    }

    @Override
    public boolean sable$shouldKickFromTracking() {
        return false;
    }

    @Override
    public boolean sable$shouldCollideWithTrackingSubLevel() {
        return false;
    }

    public static class Factory implements ParticleProvider<HotAirEmberParticleData> {
        private final SpriteSet spriteSet;

        public Factory(final SpriteSet animatedSprite) {
            this.spriteSet = animatedSprite;
        }

        public Particle createParticle(final HotAirEmberParticleData data, final ClientLevel worldIn, final double x, final double y, final double z,
                                       final double xSpeed, final double ySpeed, final double zSpeed, final RandomSource random) {
            return new HotAirEmberParticle(worldIn, x, y, z, xSpeed, ySpeed, zSpeed, data.isSoul, this.spriteSet.get(0, 1));
        }
    }
}
