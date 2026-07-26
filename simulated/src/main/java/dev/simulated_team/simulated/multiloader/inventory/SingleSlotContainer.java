package dev.simulated_team.simulated.multiloader.inventory;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class SingleSlotContainer implements AbstractContainer {

    public final ContainerSlot slot;

    public final int maxStackSize;

    public SingleSlotContainer(final int maxStackSize) {
        this.maxStackSize = maxStackSize;

        this.slot = new ContainerSlot(0, ItemStack.EMPTY, Items.AIR, this);
    }

    @Override
    public int insertGeneral(final ItemInfoWrapper item, final int amountToInsert, final boolean simulate) {
        return this.commonInsert(item, this.slot, amountToInsert, simulate);
    }

    @Override
    public ItemStack insertSlot(final ItemStack stack, final int slot, final boolean simulate) {
        final int inserted = this.commonInsert(ItemInfoWrapper.generateFromStack(stack), this.slot, stack.getCount(), simulate);
        if (inserted > 0) {
            final ItemStack copied = stack.copy();
            copied.shrink(inserted);
            return copied;
        }

        return stack;
    }

    @Override
    public int extractGeneral(final ItemInfoWrapper info, final int amountToExtract, final boolean simulate) {
        return this.commonExtract(info, this.slot, amountToExtract, simulate);
    }

    @Override
    public ItemStack extractSlot(final int index, final int amountToExtract, final boolean simulate) {
        if (index != 0) {
            return ItemStack.EMPTY;
        }

        final ItemStack newStack = this.slot.getStack().copy();
        final long extracted = this.commonExtract(ItemInfoWrapper.generateFromStack(newStack), this.slot, amountToExtract, simulate);
        if (extracted > 0) {
            newStack.setCount((int) extracted);
            return newStack;
        }

        return ItemStack.EMPTY;
    }

    @Override
    public List<ContainerSlot> getInventoryAsList() {
        return List.of(this.slot);
    }

    @Override
    public Set<ContainerSlot> getPopulatedSlots() {
        if (!this.slot.isEmpty()) {
            return Set.of(this.slot);
        }

        return new HashSet<>();
    }

    @Override
    public CompoundTag write(final HolderLookup.Provider provider) {
        return this.slot.write(provider);
    }

    @Override
    public void read(final HolderLookup.Provider provider, final CompoundTag nbt) {
        this.slot.read(provider, nbt);
    }

    @Override
    public int getContainerSize() {
        return 1;
    }

    @Override
    public boolean canInsertItem(final ItemInfoWrapper info, final ContainerSlot slot) {
        return this.canInsertItem(info);
    }

    public abstract boolean canInsertItem(ItemInfoWrapper info);

    @Override
    public boolean isEmpty() {
        return this.slot.isEmpty();
    }

    @Override
    public @NotNull ItemStack getItem(final int slot) {
        if (slot != 0) {
            return ItemStack.EMPTY;
        }

        return this.slot.getStack();
    }

    @Override
    public void setItem(final int slot, final @NotNull ItemStack stack) {
        if (slot != 0) {
            return;
        }

        this.slot.setStack(stack);
    }

    @Override
    public void onStackItemChange(final ContainerSlot slot, final ItemStack oldSlotStack, final ItemStack newSlotStack) {
        this.setChanged();
    }

    @Override
    public int getMaxStackSize() {
        return this.maxStackSize;
    }

    @Override
    public void clearContent() {
        this.slot.clear();
    }

    @Override
    public void setChanged() {
    }
}
