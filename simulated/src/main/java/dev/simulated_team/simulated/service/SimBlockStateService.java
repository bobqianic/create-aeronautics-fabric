package dev.simulated_team.simulated.service;

import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;
import dev.simulated_team.simulated.content.blocks.auger_shaft.AugerShaftBlock;
import dev.simulated_team.simulated.content.blocks.util.AbstractDirectionalAxisBlock;
import dev.simulated_team.simulated.data.SimBlockStateGen;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.BiFunction;
import java.util.function.Function;

public interface SimBlockStateService {

	SimBlockStateService INSTANCE = ServiceUtil.load(SimBlockStateService.class);

	<T extends Block> void genericModelBuilder(final DataGenContext<Block, T> ctx, final RegistrateBlockstateProvider prov, final Function<BlockState, SimBlockStateGen.XYHolder> xyGetter, final Function<BlockState, Object> modelGetter);

	<P extends AugerShaftBlock> NonNullBiConsumer<DataGenContext<Block, P>, RegistrateBlockstateProvider> augerShaftGenerate(String name, boolean cog);

	<T extends AbstractDirectionalAxisBlock> void directionalAxisBlock(DataGenContext<Block, T> ctx, RegistrateBlockstateProvider prov, BiFunction<BlockState, Boolean, Object> modelFunc);
}
