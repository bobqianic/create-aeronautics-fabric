package dev.eriksonn.aeronautics.data;

import com.zurrtum.create.content.kinetics.base.DirectionalAxisKineticBlock;
import com.zurrtum.create.foundation.data.BlockStateGen;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class AeroBlockStateGen {
	public static <T extends DirectionalAxisKineticBlock> void directionalPoweredAxisBlockstate(final DataGenContext<Block, T> ctx, final RegistrateBlockstateProvider prov) {
		BlockStateGen.directionalAxisBlock(ctx, prov, (blockState, vertical) -> prov.models()
				.getExistingFile(prov.modLoc("block/" + ctx.getName() + "/block_" + (vertical ? "vertical" : "horizontal") + (blockState.getValue(BlockStateProperties.POWERED) ? "_powered" : ""))));
	}
}
