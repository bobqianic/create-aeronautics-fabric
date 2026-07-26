package dev.simulated_team.simulated.util.placement_helpers;

import com.zurrtum.create.catnip.placement.IPlacementHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Predicate;

public abstract class SimplePlacementHelper implements IPlacementHelper {

    Predicate<ItemStack> itemPredicate;

    Predicate<BlockState> statePredicate;


    public SimplePlacementHelper(final Predicate<ItemStack> itemPredicate, final Predicate<BlockState> statePredicate) {
        this.itemPredicate = itemPredicate;
        this.statePredicate = statePredicate;
    }


    @Override
    public Predicate<ItemStack> getItemPredicate() {
        return this.itemPredicate;
    }

    @Override
    public Predicate<BlockState> getStatePredicate() {
        return this.statePredicate;
    }
}
