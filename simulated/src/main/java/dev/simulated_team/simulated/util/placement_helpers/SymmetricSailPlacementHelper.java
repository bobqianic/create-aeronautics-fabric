package dev.simulated_team.simulated.util.placement_helpers;

import com.zurrtum.create.catnip.placement.IPlacementHelper;
import com.zurrtum.create.catnip.placement.PlacementOffset;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;

import java.util.List;
import java.util.function.Predicate;

public class SymmetricSailPlacementHelper extends SimplePlacementHelper {

    public SymmetricSailPlacementHelper(final Predicate<ItemStack> itemPredicate, final Predicate<BlockState> statePredicate) {
        super(itemPredicate, statePredicate);
    }

    @Override
    public PlacementOffset getOffset(final Player player, final Level world, final BlockState state, final BlockPos pos, final BlockHitResult ray) {
        final Direction.Axis axis;
        if (state.hasProperty(BlockStateProperties.AXIS)) {
            axis = state.getValue(BlockStateProperties.AXIS);
        } else {
            return PlacementOffset.fail();
        }

        final List<Direction> validDir = IPlacementHelper.orderedByDistanceExceptAxis(pos, ray.getLocation(), axis);
        for (final Direction dir : validDir) {
            if (!world.getBlockState(pos.relative(dir)).canBeReplaced())
                continue;

            return PlacementOffset.success(pos.relative(dir), (s) -> s.setValue(BlockStateProperties.AXIS, axis));
        }

        return PlacementOffset.fail();
    }
}
