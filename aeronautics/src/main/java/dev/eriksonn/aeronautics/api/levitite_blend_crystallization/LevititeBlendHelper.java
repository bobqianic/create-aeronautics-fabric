package dev.eriksonn.aeronautics.api.levitite_blend_crystallization;

import com.zurrtum.create.content.processing.burner.BlazeBurnerBlock;
import com.zurrtum.create.content.processing.burner.LitBlazeBurnerBlock;
import dev.eriksonn.aeronautics.index.AeroRegistries;
import dev.eriksonn.aeronautics.service.AeroLevititeService;
import foundry.veil.platform.registry.RegistryObject;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;

import java.util.Optional;

import static dev.eriksonn.aeronautics.index.AeroLevititeBlendPropagationContexts.SOUL_CONTEXT;
import static dev.eriksonn.aeronautics.index.AeroLevititeBlendPropagationContexts.STANDARD_CONTEXT;

public class LevititeBlendHelper {
    public static Fluid getFluid() {
        return AeroLevititeService.INSTANCE.getFluid();
    }

    public static BlockState crystallizeLevititeBlend(final Level level, final BlockPos pos, final CrystalPropagationContext context) {
        context.onCrystallize(level, pos);

        if (!level.isClientSide()) {
            updateSurroundingLevititeBlend(level, pos, context);
        }

        return context.getCrystalBlockState(level, pos);
    }

    public static void updateSurroundingLevititeBlend(final Level pLevel, final BlockPos pPos, final CrystalPropagationContext context) {
        for (final Direction direction : Direction.values()) {
            final BlockPos newPos = pPos.relative(direction);
            final FluidState state = pLevel.getFluidState(newPos);

            if (state.getType() instanceof LevititeBlendDummyInterface && context.canSpreadTo(state)) {
                addLevititeBlendTicker(pLevel, newPos, false, false, context.getContextForSpread(pLevel, newPos));
            }
        }
    }

    public static void checkSurroundingSources(final Level level, final BlockPos pos, final FluidState state) {
        if (state.getType() instanceof LevititeBlendDummyInterface && state.isSource()) {
            for (final Direction direction : Direction.values()) {
                final BlockPos blockpos = pos.relative(direction);

                final CrystalPropagationContext context = getContextFromBlock(level, blockpos);
                if (context != null) {
                    addLevititeBlendTicker(level, pos, true, true, context);
                    break;
                }
            }
        }
    }

    public static void spawnParticles(final Level pLevel, final BlockPos pPos, final ParticleOptions type, final int count) {
        if (!pLevel.isClientSide()) {
            final double d0 = 0.5625D;
            final RandomSource random = pLevel.random;
            final ServerLevel serverLevel = (ServerLevel) pLevel;

            serverLevel.sendParticles(type, pPos.getX() + 0.5, pPos.getY() + 0.5, pPos.getZ() + 0.5, count, 0.3, 0.3, 0.3, 0);
        }
    }

    public static void addLevititeBlendTicker(final Level level, final BlockPos pPos, final boolean requiresCatalyst, final boolean isDormant, final CrystalPropagationContext context) {
        if (!level.isClientSide()) {
            LevititeCrystallizerManager.addTicker(level, pPos, context.getNewAge(level, 0, isDormant), requiresCatalyst, isDormant, context);
        }
    }

    public static CrystalPropagationContext getContextFromBlock(final Level pLevel, final BlockPos pPos) {
        final BlockState state = pLevel.getBlockState(pPos);
        CrystalPropagationContext standardContext = STANDARD_CONTEXT.get();
        CrystalPropagationContext soulContext = SOUL_CONTEXT.get();

        if (state.getBlock() instanceof BlazeBurnerBlock && state.getValue(BlazeBurnerBlock.HEAT_LEVEL).isAtLeast(BlazeBurnerBlock.HeatLevel.SMOULDERING))
            return standardContext;
        if (state.getBlock() instanceof LitBlazeBurnerBlock)
            return state.getValue(LitBlazeBurnerBlock.FLAME_TYPE) == LitBlazeBurnerBlock.FlameType.REGULAR ? standardContext : soulContext;

        final Optional<Boolean> litState = state.getOptionalValue(BlockStateProperties.LIT);
        if (litState.isPresent() && !litState.get())
            return null;

        for (RegistryObject<CrystalPropagationContext> entry : AeroRegistries.LEVITITE_CRYSTAL_PROPAGATION_CONTEXT.getEntries()) {
            CrystalPropagationContext context = entry.get();
            if(state.is(context.getCatalyzerTag())) {
                return context;
            }
        }

        return null;
    }
}
