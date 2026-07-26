package dev.simulated_team.simulated.content.blocks.redstone.linked_typewriter.screen;

import com.zurrtum.create.infrastructure.items.ItemStackHandler;
import dev.simulated_team.simulated.Simulated;
import dev.simulated_team.simulated.content.blocks.redstone.linked_typewriter.LinkedTypewriterBlockEntity;
import dev.simulated_team.simulated.index.SimMenuTypes;
import dev.simulated_team.simulated.service.SimMenuService;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.ValueInput;

public class LinkedTypewriterMenuCommon extends AbstractContainerMenu {
    private static final int PLAYER_SLOT_COUNT = 36;

    public final ItemStackHandler ghostInventory = new ItemStackHandler(2);
    public final LinkedTypewriterBlockEntity contentHolder;
    public boolean slotsActive;

    public LinkedTypewriterMenuCommon(final MenuType<?> type, final int id, final Inventory inventory,
                                      final LinkedTypewriterMenuData data) {
        this(type, id, inventory, createOnClient(data));
    }

    public LinkedTypewriterMenuCommon(final MenuType<?> type, final int id, final Inventory inventory,
                                      final LinkedTypewriterBlockEntity blockEntity) {
        super(type, id);
        this.contentHolder = blockEntity;
        this.addSlots(inventory);
    }

    public static LinkedTypewriterMenuCommon create(final int id, final Inventory inventory,
                                                    final LinkedTypewriterBlockEntity blockEntity) {
        return SimMenuService.INSTANCE.getLoaderLinkedTypewriter(
                SimMenuTypes.LINKED_TYPEWRITER.get(), id, inventory, blockEntity);
    }

    private static LinkedTypewriterBlockEntity createOnClient(final LinkedTypewriterMenuData data) {
        final ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            return null;
        }

        final BlockEntity blockEntity = level.getBlockEntity(data.blockPos());
        if (!(blockEntity instanceof final LinkedTypewriterBlockEntity linkedTypewriter)) {
            return null;
        }

        try (ProblemReporter.ScopedCollector logging =
                     new ProblemReporter.ScopedCollector(linkedTypewriter.problemPath(), Simulated.LOGGER)) {
            final ValueInput view = TagValueInput.create(logging, level.registryAccess(), data.updateTag());
            linkedTypewriter.readClient(view);
        }
        return linkedTypewriter;
    }

    private void addSlots(final Inventory inventory) {
        final int playerX = 38;
        final int playerY = 59;

        // Keep the hotbar first: the ghost-slot click handling relies on the
        // first 36 menu slots belonging to the player.
        for (int hotbarSlot = 0; hotbarSlot < 9; hotbarSlot++) {
            this.addSlot(new ActiveSlot(inventory, hotbarSlot, playerX + hotbarSlot * 18, playerY + 58));
        }
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                this.addSlot(new ActiveSlot(
                        inventory,
                        column + row * 9 + 9,
                        playerX + column * 18,
                        playerY + row * 18
                ));
            }
        }

        for (int slot = 0; slot < this.ghostInventory.getContainerSize(); slot++) {
            this.addSlot(new ActiveSlot(this.ghostInventory, slot, 105 + slot * 18, 1));
        }
    }

    @Override
    public void clicked(final int slotId, final int dragType, final ClickType clickType, final Player player) {
        if (slotId < PLAYER_SLOT_COUNT || slotId >= this.slots.size()) {
            super.clicked(slotId, dragType, clickType, player);
            return;
        }
        if (!this.slotsActive || clickType == ClickType.THROW) {
            return;
        }

        final int ghostSlot = slotId - PLAYER_SLOT_COUNT;
        final ItemStack carried = this.getCarried();
        if (clickType == ClickType.CLONE) {
            if (player.isCreative() && carried.isEmpty()) {
                final ItemStack copy = this.ghostInventory.getItem(ghostSlot).copy();
                copy.setCount(copy.getMaxStackSize());
                this.setCarried(copy);
            }
            return;
        }

        this.ghostInventory.setItem(
                ghostSlot,
                carried.isEmpty() ? ItemStack.EMPTY : carried.copyWithCount(1)
        );
        this.getSlot(slotId).setChanged();
    }

    @Override
    public boolean canTakeItemForPickAll(final ItemStack stack, final Slot slot) {
        return slot.container instanceof Inventory;
    }

    @Override
    public boolean canDragTo(final Slot slot) {
        return slot.container instanceof Inventory;
    }

    @Override
    public ItemStack quickMoveStack(final Player player, final int slotIndex) {
        if (!this.slotsActive || slotIndex < 0 || slotIndex >= this.slots.size()) {
            return ItemStack.EMPTY;
        }

        if (slotIndex < PLAYER_SLOT_COUNT) {
            final ItemStack source = this.getSlot(slotIndex).getItem();
            if (source.isEmpty()) {
                return ItemStack.EMPTY;
            }
            for (int slot = 0; slot < this.ghostInventory.getContainerSize(); slot++) {
                if (this.ghostInventory.getItem(slot).isEmpty()) {
                    this.ghostInventory.setItem(slot, source.copyWithCount(1));
                    this.getSlot(PLAYER_SLOT_COUNT + slot).setChanged();
                    break;
                }
            }
        } else {
            final int ghostSlot = slotIndex - PLAYER_SLOT_COUNT;
            this.ghostInventory.setItem(ghostSlot, ItemStack.EMPTY);
            this.getSlot(slotIndex).setChanged();
        }
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(final Player player) {
        return this.contentHolder != null
                && this.contentHolder.getLevel() != null
                && LinkedTypewriterBlockEntity.playerInRange(
                        player, this.contentHolder.getLevel(), this.contentHolder.getBlockPos());
    }

    private class ActiveSlot extends Slot {
        private ActiveSlot(final Container container, final int slot, final int x, final int y) {
            super(container, slot, x, y);
        }

        @Override
        public boolean isActive() {
            return LinkedTypewriterMenuCommon.this.slotsActive;
        }
    }
}
