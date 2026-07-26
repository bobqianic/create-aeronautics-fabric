package dev.eriksonn.aeronautics.content.particle;

import dev.ryanhcode.sable.api.particle.ParticleSubLevelKickable;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.RandomSource;

public class AirPoofParticle extends SingleQuadParticle implements ParticleSubLevelKickable {
    protected AirPoofParticle(final ClientLevel level, final double x, final double y, final double z, final double xSpeed, final double ySpeed, final double zSpeed, final TextureAtlasSprite sprite) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed, sprite);
        this.alpha = level.random.nextFloat() * 0.2f + 0.3f;
        this.xd = xSpeed;
        this.yd = ySpeed;
        this.zd = zSpeed;
    }

    @Override
    protected Layer getLayer() {
        return Layer.TRANSLUCENT;
    }

    @Override
    public boolean sable$shouldKickFromTracking() {
        return false;
    }

    @Override
    public boolean sable$shouldCollideWithTrackingSubLevel() {
        return false;
    }

    public record Factory(SpriteSet spriteSet) implements ParticleProvider<AirPoofParticleData> {
        public Particle createParticle(final AirPoofParticleData data, final ClientLevel worldIn, final double x, final double y, final double z,
                                       final double xSpeed, final double ySpeed, final double zSpeed, final RandomSource random) {
            return new AirPoofParticle(worldIn, x, y, z, xSpeed, ySpeed, zSpeed, this.spriteSet.get(random));
        }
    }
}
