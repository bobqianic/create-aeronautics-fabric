package dev.simulated_team.simulated.compat.create;

import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.content.logistics.filter.FilterItemStack;
import com.zurrtum.create.foundation.blockEntity.behaviour.filtering.ServerFilteringBehaviour;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;

import java.util.function.Predicate;

public class FilteringBehaviour extends ServerFilteringBehaviour {
    private final ValueBoxTransform slotPositioning;

    public FilteringBehaviour(final SmartBlockEntity blockEntity, final ValueBoxTransform slotPositioning) {
        super(blockEntity);
        this.slotPositioning = slotPositioning;
    }

    public FilteringBehaviour withPredicate(final Predicate<ItemStack> predicate) {
        super.withPredicate(predicate);
        return this;
    }

    public ValueBoxTransform getSlotPositioning() {
        return slotPositioning;
    }

    @Override
    public void read(final ValueInput input, final boolean clientPacket) {
        super.read(input, clientPacket);
        if (getFilter().isEmpty()) {
            final ItemStack legacyFilter = input.read("SimulatedFilter", ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
            if (!legacyFilter.isEmpty()) {
                filter = FilterItemStack.of(legacyFilter);
            }
        }
    }
}
