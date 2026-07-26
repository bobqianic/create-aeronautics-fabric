package dev.eriksonn.aeronautics.fabric;

import com.tterrag.registrate.util.entry.FluidEntry;
import dev.eriksonn.aeronautics.Aeronautics;
import dev.eriksonn.aeronautics.api.levitite_blend_crystallization.LevititeBlendDummyInterface;
import dev.eriksonn.aeronautics.fabric.content.fluid.LevititeBlendFlowing;
import dev.eriksonn.aeronautics.fabric.content.fluid.LevititeBlendSource;
import dev.eriksonn.aeronautics.index.AeroSoundEvents;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributeHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.FluidState;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public final class FabricAeroFluids {
    public static final FluidEntry<LevititeBlendFlowing> LEVITITE_BLEND = Aeronautics.getRegistrate()
            .fluid("levitite_blend",
                    Aeronautics.path("fluid/levitite_blend_still"),
                    Aeronautics.path("fluid/levitite_blend_flow"),
                    LevititeBlendFlowing::new)
            .lang("Levitite Blend")
            .fluidProperties(properties -> properties
                    .levelDecreasePerBlock(2)
                    .tickRate(25)
                    .flowSpeed(3)
                    .blastResistance(100.0F))
            .fluidAttributes(FabricAeroFluids::createAttributes)
            .source(LevititeBlendSource::new)
            .register();

    private FabricAeroFluids() {
    }

    public static void init() {
    }

    public static void tickLevititeBlend(final ServerLevel level, final BlockPos pos,
                                         final FluidState fluidState, final boolean source) {
        for (final Direction direction : Direction.values()) {
            if (level.getFluidState(pos.relative(direction)).is(FluidTags.LAVA)) {
                level.setBlockAndUpdate(pos, source
                        ? Blocks.OBSIDIAN.defaultBlockState()
                        : Blocks.CALCITE.defaultBlockState());
                return;
            }
        }

        if (fluidState.getType() instanceof final LevititeBlendDummyInterface levititeBlend) {
            levititeBlend.levititeBlendTick(level, pos, fluidState);
        }
    }

    private static FluidVariantAttributeHandler createAttributes() {
        return new FluidVariantAttributeHandler() {
            @Override
            public Optional<SoundEvent> getFillSound(final FluidVariant variant) {
                return Optional.of(AeroSoundEvents.LEVITITE_BLEND_FILL.event());
            }

            @Override
            public Optional<SoundEvent> getEmptySound(final FluidVariant variant) {
                return Optional.of(AeroSoundEvents.LEVITITE_BLEND_EMPTY.event());
            }

            @Override
            public int getViscosity(final FluidVariant variant, @Nullable final Level level) {
                return 1500;
            }
        };
    }
}
