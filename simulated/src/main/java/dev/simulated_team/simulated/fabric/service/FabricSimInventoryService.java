package dev.simulated_team.simulated.fabric.service;

import com.tterrag.registrate.util.nullness.NonNullConsumer;
import com.zurrtum.create.content.contraptions.MountedStorageManager;
import dev.simulated_team.simulated.fabric.transfer.FabricInventoryLoaderWrapper;
import dev.simulated_team.simulated.fabric.transfer.SingleBatteryStorage;
import dev.simulated_team.simulated.fabric.transfer.SingleTankStorage;
import dev.simulated_team.simulated.multiloader.energy.SingleBattery;
import dev.simulated_team.simulated.multiloader.inventory.AbstractContainer;
import dev.simulated_team.simulated.multiloader.inventory.InventoryLoaderWrapper;
import dev.simulated_team.simulated.multiloader.tanks.SingleTank;
import dev.simulated_team.simulated.service.SimInventoryService;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.jetbrains.annotations.Nullable;
import team.reborn.energy.api.EnergyStorage;

import java.util.function.BiFunction;

public final class FabricSimInventoryService implements SimInventoryService {
    @Override
    @SuppressWarnings("unchecked")
    public <T extends InventoryLoaderWrapper> T getInventory(
            @Nullable final BlockEntity blockEntity, @Nullable final Direction direction) {
        if (blockEntity == null || blockEntity.getLevel() == null) {
            return null;
        }
        final Storage<ItemVariant> storage = ItemStorage.SIDED.find(
                blockEntity.getLevel(), blockEntity.getBlockPos(), blockEntity.getBlockState(), blockEntity, direction);
        return storage == null ? null : (T) new FabricInventoryLoaderWrapper(storage);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends InventoryLoaderWrapper> T getWrappedAllItemsFromContraption(final MountedStorageManager manager) {
        return (T) new FabricInventoryLoaderWrapper(InventoryStorage.of(manager.getAllItems(), null));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends InventoryLoaderWrapper> T getWrappedMountedItemsFromContraption(final MountedStorageManager manager) {
        return (T) new FabricInventoryLoaderWrapper(InventoryStorage.of(manager.getMountedItems(), null));
    }

    @Override
    public <T extends BlockEntity> NonNullConsumer<BlockEntityType<T>> registerInventory(
            final BiFunction<T, Direction, AbstractContainer> getter) {
        return type -> ItemStorage.SIDED.registerForBlockEntity((blockEntity, direction) -> {
            final AbstractContainer inventory = getter.apply(blockEntity, direction);
            return inventory == null ? null : InventoryStorage.of(inventory, direction);
        }, type);
    }

    @Override
    public <T extends BlockEntity> NonNullConsumer<BlockEntityType<T>> registerTank(
            final BiFunction<T, Direction, SingleTank> getter) {
        return type -> FluidStorage.SIDED.registerForBlockEntity((blockEntity, direction) -> {
            final SingleTank tank = getter.apply(blockEntity, direction);
            return tank == null ? null : new SingleTankStorage(tank);
        }, type);
    }

    @Override
    public <T extends BlockEntity> NonNullConsumer<BlockEntityType<T>> registerBattery(
            final BiFunction<T, Direction, SingleBattery> getter) {
        return type -> EnergyStorage.SIDED.registerForBlockEntity((blockEntity, direction) -> {
            final SingleBattery battery = getter.apply(blockEntity, direction);
            return battery == null ? null : new SingleBatteryStorage(battery);
        }, type);
    }
}
