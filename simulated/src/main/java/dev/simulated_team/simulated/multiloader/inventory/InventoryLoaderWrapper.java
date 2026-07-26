package dev.simulated_team.simulated.multiloader.inventory;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Wrapper class dedicated to interacting with other mod's inventories in multiloader environments
 */
public abstract class InventoryLoaderWrapper implements AbstractContainer {

    public Consumer<Boolean> callback;

    public abstract ItemStack extractAny(int maxAmount, boolean simulate, boolean exact);

    @Override
    public void setItem(final int slot, final @NotNull ItemStack stack) {}

    @Override
    public void clearContent() {}

    @Override
    public void setChanged() {}

    @Override
    public CompoundTag write(final HolderLookup.Provider provider) {
        return new CompoundTag();
    }

    @Override
    public void read(final HolderLookup.Provider provider, final CompoundTag nbt) {

    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public List<ContainerSlot> getInventoryAsList() {
        return new ArrayList<>();
    }

    @Override
    public Set<ContainerSlot> getPopulatedSlots() {
        return new HashSet<>();
    }

    /**
     * @param callback The callback to invoke when an item is modified in this inventory. true == extraction. false == insertion.
     */
    public void inventoryModificationCallback(final Consumer<Boolean> callback) {
        this.callback = callback;
    }
}
