package dev.eriksonn.aeronautics.mixin.propeller_bearing;

import dev.eriksonn.aeronautics.content.blocks.propeller.bearing.contraption.PropellerBearingContraptionEntity;
import dev.ryanhcode.sable.api.block.BlockSubLevelLiftProvider;
import dev.ryanhcode.sable.api.sublevel.KinematicContraption;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;

import java.util.Map;

@Mixin(PropellerBearingContraptionEntity.class)
public abstract class PropellerBearingContraptionEntityMixin implements KinematicContraption {

	@Override
	public Map<BlockPos, BlockSubLevelLiftProvider.LiftProviderContext> sable$liftProviders() {
		return Map.of();
	}
}
