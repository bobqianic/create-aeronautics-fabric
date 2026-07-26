package dev.eriksonn.aeronautics.content.blocks.levitite;

import dev.eriksonn.aeronautics.api.levitite_blend_crystallization.CrystalPropagationContext;
import dev.eriksonn.aeronautics.api.levitite_blend_crystallization.LevititeBlendHelper;
import dev.eriksonn.aeronautics.index.AeroAdvancements;
import dev.eriksonn.aeronautics.index.AeroBlocks;
import dev.eriksonn.aeronautics.index.AeroSoundEvents;
import dev.eriksonn.aeronautics.index.AeroTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

public class LevititeSoulCrystalPropagationContext implements CrystalPropagationContext {

    @Override
    public void onCrystallizationInitialize(final Level level, final BlockPos pos, final boolean isDormant) {
        level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.2f, 1.5f);
        if (!isDormant)
            LevititeBlendHelper.spawnParticles(level, pos, ParticleTypes.SOUL_FIRE_FLAME, 20);
        LevititeBlendHelper.spawnParticles(level, pos, ParticleTypes.SMOKE, 15);
    }

    @Override
    public void onCrystallize(final Level level, final BlockPos pos) {
        this.onDefaultCrystallize(level, pos);

        if (!level.isClientSide()) {
            AeroSoundEvents.LEVITITE_BLEND_CRYSTALLIZE.play(level, null, pos, 1.0f, 1.0f);
            LevititeBlendHelper.spawnParticles(level, pos, ParticleTypes.SOUL_FIRE_FLAME, 30);
            AeroAdvancements.NOW_AVAILABLE_IN_PINK.awardToNearby(pos, level);
        }
    }

    @Override
    public void onCrystallizationFail(final Level level, final BlockPos pos, final int attempts, final boolean isDormant) {
        LevititeBlendHelper.spawnParticles(level, pos, ParticleTypes.SMOKE, 15);
    }

    @Override
    public BlockState getCrystalBlockState(final Level level, final BlockPos pos) {
        return AeroBlocks.PEARLESCENT_LEVITITE.getDefaultState();
    }

    public boolean canSpreadTo(final FluidState state) {
        return state.is(LevititeBlendHelper.getFluid());
    }

    @Override
    public CrystalPropagationContext getContextForSpread(final Level level, final BlockPos pos) {
        return LevititeCrystalPropagationContext.getRandomContext(this, level, pos);
    }

    @Override
    public TagKey<Block> getCatalyzerTag() {
        return AeroTags.BlockTags.LEVITITE_SOUL_CATALYZER;
    }
}
