package dev.eriksonn.aeronautics.fabric.content.fluid;

import com.tterrag.registrate.fabric.SimpleFlowableFluid;
import dev.eriksonn.aeronautics.api.levitite_blend_crystallization.LevititeBlendDummyInterface;
import dev.eriksonn.aeronautics.fabric.FabricAeroFluids;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

public final class LevititeBlendFlowing extends SimpleFlowableFluid.Flowing implements LevititeBlendDummyInterface {
    public LevititeBlendFlowing(final Properties properties) {
        super(properties);
    }

    @Override
    public void tick(final ServerLevel level, final BlockPos pos, final BlockState blockState, final FluidState fluidState) {
        super.tick(level, pos, blockState, fluidState);
        FabricAeroFluids.tickLevititeBlend(level, pos, fluidState, false);
    }
}
