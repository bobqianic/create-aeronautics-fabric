package dev.simulated_team.simulated.multiloader.inventory;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class ContainerSlot implements NBTSerializable {

    public static final ContainerSlot EMPTY = new ContainerSlot();

    private final int index;
    private final AbstractContainer parent;

    private Item type;
    private ItemStack stack;

    public ContainerSlot() {
        this.index = -1;
        this.parent = null;

        this.type = ItemStack.EMPTY.getItem();
        this.stack = ItemStack.EMPTY;
    }

    public ContainerSlot(final int slot, final ItemStack stack, final Item type, final AbstractContainer parent) {
        this.index = slot;
        this.stack = stack;
        this.type = type;
        this.parent = parent;

        //populate the parent's fields
        parent.populateFields(this);
    }

    public static ContainerSlot of(final int slot, final Item type, final AbstractContainer parent) {
        return of(slot, new ItemStack(type), parent);
    }

    public static ContainerSlot of(final int slot, final ItemStack stack, final AbstractContainer parent) {
        return new ContainerSlot(slot, stack, stack.getItem(), parent);
    }

    public int insertStack(final ItemInfoWrapper info, final int maxAmount, final boolean simulate) {
        //Make sure the incoming item is valid to be inserted into this slot, and the held item is either empty or the same item
        if (this.canInsert(info) && (this.getStack().isEmpty() || this.getStack().getItem() == info.type())) {
            //Add max stack size limitation
            final int insertedAmount = Math.min(Math.min(this.parent.getMaxStackSize(), info.type().getDefaultMaxStackSize()) - this.getStack().getCount(), maxAmount);

            //set the current stack in this slot if this isn't a simulated action
            if (!simulate && insertedAmount > 0) {
                ItemStack newstack = this.getStack().copy();
                if (newstack.isEmpty()) {
                    newstack = ItemInfoWrapper.generateFromInfo(info);

                    newstack.setCount(insertedAmount);
                } else {
                    newstack.grow(insertedAmount);
                }

                this.setStack(newstack);
            }

            return insertedAmount;
        }

        return 0;
    }

    public int extractStack(final ItemInfoWrapper info, final int maxAmount, final boolean simulate) {
        if (this.canExtract() && !this.getStack().isEmpty()) {
            if (info != null && this.getStack().getItem() != info.type()) {
                return 0;
            }

            final int extracted = Math.min(this.getStack().getCount(), maxAmount);
            if (!simulate && extracted > 0) {
                final ItemStack newStack = this.getStack().copy();
                newStack.shrink(extracted);

                this.setStack(newStack);
            }

            return extracted;
        }

        return 0;
    }

    public void shrink(final long amountToShrink) {
        ItemStack copied = this.getStack().copy();

        if (copied.getCount() - amountToShrink <= 0) {
            copied = ItemStack.EMPTY;
        } else {
            copied.shrink((int) amountToShrink);
        }

        this.setStack(copied);
    }

    public boolean canInsert(final ItemInfoWrapper info) {
        return this.parent.canInsertItem(info, this);
    }

    public boolean canExtract() {
        return this.parent.canExtractFromSlot(this);
    }

    public boolean isEmpty() {
        return this == EMPTY || this.getStack().isEmpty();
    }

    public void setStack(final ItemStack stack) {
        this.parent.onStackItemChange(this, this.getStack(), stack);

        this.stack = stack;
        this.type = stack.getItem();

        this.parent.setChanged();
    }

    public int getIndex() {
        return this.index;
    }

    public ItemStack getStack() {
        return this.stack;
    }

    public Item getType() {
        return this.type;
    }

    public AbstractContainer getParent() {
        return this.parent;
    }

    public void clear() {
        this.stack = ItemStack.EMPTY;
        this.type = Items.AIR;
    }

    @Override
    public CompoundTag write(final HolderLookup.Provider provider) {
        final CompoundTag slotTag = new CompoundTag();

        slotTag.putInt("index", this.getIndex());
        if (!this.getStack().isEmpty()) {
            slotTag.put("item", new CompoundTag());
        }

        return slotTag;
    }

    @Override
    public void read(final HolderLookup.Provider provider, final CompoundTag nbt) {
        this.stack = ItemStack.EMPTY;

        if (nbt.contains("item")) {
            this.stack = ItemStack.EMPTY;
        }

        this.type = this.stack.getItem();
    }
}
