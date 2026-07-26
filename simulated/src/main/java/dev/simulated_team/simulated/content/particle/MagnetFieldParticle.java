package dev.simulated_team.simulated.content.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;

public class MagnetFieldParticle extends SimpleAnimatedParticle {

    private final Vec3 motion;

    protected MagnetFieldParticle(final ClientLevel world, final double x, final double y, final double z, final double dx, final double dy,
                                  final double dz, final SpriteSet sprite, final boolean negative) {
        super(world, x, y, z, sprite, world.random.nextFloat() * .5f);
        this.hasPhysics = false;
        this.lifetime = 5;
        this.quadSize *= 0.75F;
        this.xo = x;
        this.yo = y;
        this.zo = z;
        this.motion = new Vec3(dx, dy, dz);

        this.selectSprite(0);
        this.setAlpha(0.4f);
        if(negative)
            this.setColor(0.7f,0.7f,1);
        else
            this.setColor(1,0.7f,0.7f);
    }

    public SingleQuadParticle.Layer getLayer() {
        return SingleQuadParticle.Layer.TRANSLUCENT;
    }

    private void dissipate() {
        this.remove();
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (this.age++ >= this.lifetime) {
            this.remove();
            return;
        }

        this.selectSprite(this.age);

        this.xd = this.motion.x;
        this.yd = this.motion.y;
        this.zd = this.motion.z;
        this.move(this.xd, this.yd, this.zd);

    }

    public int getLightColor(final float partialTick) {
        final BlockPos blockpos = new BlockPos((int) this.x, (int) this.y, (int) this.z);
        return this.level.isLoaded(blockpos) ? LevelRenderer.getLightColor(this.level, blockpos) : 0;
    }

    private void selectSprite(final int index) {
        this.setSprite(this.sprites.get(index, 8));
    }

    public static class Factory implements ParticleProvider<MagnetFieldParticleData> {
        private final SpriteSet spriteSet;

        public Factory(final SpriteSet animatedSprite) {
            this.spriteSet = animatedSprite;
        }

        public Particle createParticle(final MagnetFieldParticleData data, final ClientLevel level, final double x, final double y, final double z,
                                       final double xSpeed, final double ySpeed, final double zSpeed, final RandomSource random) {
            return new MagnetFieldParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, this.spriteSet, data.isNegative());
        }
    }
}
