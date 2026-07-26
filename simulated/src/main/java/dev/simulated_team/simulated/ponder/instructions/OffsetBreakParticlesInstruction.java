package dev.simulated_team.simulated.ponder.instructions;

import com.zurrtum.create.client.ponder.api.level.PonderLevel;
import com.zurrtum.create.client.ponder.foundation.PonderScene;
import com.zurrtum.create.client.ponder.foundation.instruction.PonderInstruction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public class OffsetBreakParticlesInstruction extends PonderInstruction {

    AABB bb;
    BlockState state;

    @Override
    public boolean isComplete() {
        return true;
    }

    // Yes this is a very slightly modified addBlockDestroyEffects already in PonderLevel. Yes its evil. Yes I'm sorry.
    public void addBlockDestroyEffects(final PonderLevel level, final AABB bb, final BlockState state) {
        final double d1 = Math.min(1.0D, bb.maxX - bb.minX);
        final double d2 = Math.min(1.0D, bb.maxY - bb.minY);
        final double d3 = Math.min(1.0D, bb.maxZ - bb.minZ);
        final int i = Math.max(2, Mth.ceil(d1 / 0.25D));
        final int j = Math.max(2, Mth.ceil(d2 / 0.25D));
        final int k = Math.max(2, Mth.ceil(d3 / 0.25D));

        // Iterates over a grid of positions, spawning a break particle at each
        for (int l = 0; l < i; ++l) {
            for (int i1 = 0; i1 < j; ++i1) {
                for (int j1 = 0; j1 < k; ++j1) {
                    final double subPosX = (l + 0.5D) / i;
                    final double subPosY = (i1 + 0.5D) / j;
                    final double subPosZ = (j1 + 0.5D) / k;
                    final double posX = subPosX * d1 + bb.minX;
                    final double posY = subPosY * d2 + bb.minY;
                    final double posZ = subPosZ * d3 + bb.minZ;
                    level.addParticle(new BlockParticleOption(ParticleTypes.BLOCK, state), posX, posY,
                            posZ, subPosX - 0.5D, subPosY - 0.5D, subPosZ - 0.5D);
                }
            }
        }
    }

    public OffsetBreakParticlesInstruction(final AABB bb, final BlockState state) {
        this.bb = bb;
        this.state = state;
    }

    @Override
    public void tick(final PonderScene scene) {
        final PonderLevel level = scene.getWorld();
        this.addBlockDestroyEffects(level, this.bb, this.state);
    }
}