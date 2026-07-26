package dev.ryanhcode.offroad.fabric.service;

import com.zurrtum.create.api.contraption.storage.item.MountedItemStorageWrapper;
import com.zurrtum.create.content.contraptions.MountedStorageManager;
import dev.ryanhcode.offroad.content.blocks.borehead_bearing.BoreheadAttachedStorage;
import dev.ryanhcode.offroad.content.blocks.borehead_bearing.BoreheadBearingBlockEntity;
import dev.ryanhcode.offroad.service.OffroadMountedStorageService;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;

import java.lang.ref.WeakReference;
import java.util.function.Predicate;

public final class FabricOffroadMountedStorageService implements OffroadMountedStorageService {
    @Override
    @SuppressWarnings("unchecked")
    public <T extends MountedStorageManager & BoreheadAttachedStorage> T getSidedBoreheadContraptionMountedStorage() {
        return (T) new FabricBoreheadMountedStorage();
    }

    private static final class FabricBoreheadMountedStorage extends MountedStorageManager
            implements BoreheadAttachedStorage {
        private WeakReference<BoreheadBearingBlockEntity> attachedBorehead = new WeakReference<>(null);
        private boolean insertAllowed;

        @Override
        public void initialize() {
            super.initialize();
            this.items = new BoreheadInventory(this.items);
            this.allItems = this.items;
            if (this.fuelItems != null) {
                this.fuelItems = new BoreheadInventory(this.fuelItems);
            }
        }

        @Override
        public void attachBlockEntity(final BoreheadBearingBlockEntity blockEntity) {
            this.attachedBorehead = new WeakReference<>(blockEntity);
        }

        @Override
        public void setInsertAllowed(final boolean insertionAllowed) {
            this.insertAllowed = insertionAllowed;
        }

        @Override
        public void invokeUnstall() {
            final BoreheadBearingBlockEntity blockEntity = this.attachedBorehead.get();
            if (blockEntity != null) {
                blockEntity.startUnstalling();
            }
        }

        private final class BoreheadInventory extends MountedItemStorageWrapper {
            private BoreheadInventory(final MountedItemStorageWrapper wrapped) {
                super(wrapped.storages);
            }

            @Override
            public boolean canPlaceItem(final int slot, final ItemStack stack) {
                return FabricBoreheadMountedStorage.this.insertAllowed && super.canPlaceItem(slot, stack);
            }

            @Override
            public int insert(final ItemStack stack, final int maxAmount, final Direction side) {
                return FabricBoreheadMountedStorage.this.insertAllowed
                        ? super.insert(stack, maxAmount, side)
                        : 0;
            }

            @Override
            public ItemStack removeItem(final int slot, final int amount) {
                return this.extracted(super.removeItem(slot, amount));
            }

            @Override
            public ItemStack removeItemNoUpdate(final int slot) {
                return this.extracted(super.removeItemNoUpdate(slot));
            }

            @Override
            public int extract(final ItemStack stack, final int maxAmount, final Direction side) {
                return this.extracted(super.extract(stack, maxAmount, side));
            }

            @Override
            public ItemStack extract(final Predicate<ItemStack> predicate, final int maxAmount, final Direction side) {
                return this.extracted(super.extract(predicate, maxAmount, side));
            }

            @Override
            public ItemStack preciseExtract(final Predicate<ItemStack> predicate, final int maxAmount,
                                            final Direction side) {
                return this.extracted(super.preciseExtract(predicate, maxAmount, side));
            }

            @Override
            public int extractAll(final Predicate<ItemStack> predicate, final int maxAmount, final Direction side) {
                return this.extracted(super.extractAll(predicate, maxAmount, side));
            }

            private ItemStack extracted(final ItemStack stack) {
                if (!stack.isEmpty()) {
                    FabricBoreheadMountedStorage.this.invokeUnstall();
                }
                return stack;
            }

            private int extracted(final int amount) {
                if (amount > 0) {
                    FabricBoreheadMountedStorage.this.invokeUnstall();
                }
                return amount;
            }
        }
    }
}
