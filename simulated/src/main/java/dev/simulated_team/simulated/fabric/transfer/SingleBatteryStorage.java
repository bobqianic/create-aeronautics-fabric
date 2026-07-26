package dev.simulated_team.simulated.fabric.transfer;

import dev.simulated_team.simulated.multiloader.energy.SingleBattery;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import team.reborn.energy.api.EnergyStorage;

public final class SingleBatteryStorage extends SnapshotParticipant<Integer> implements EnergyStorage {
    private final SingleBattery battery;

    public SingleBatteryStorage(final SingleBattery battery) {
        this.battery = battery;
    }

    @Override
    public long insert(final long maxAmount, final TransactionContext transaction) {
        final int inserted = this.battery.receiveEnergy((int) Math.min(Integer.MAX_VALUE, maxAmount), true);
        if (inserted > 0) {
            this.updateSnapshots(transaction);
            this.battery.receiveEnergy(inserted, false);
        }
        return inserted;
    }

    @Override
    public long extract(final long maxAmount, final TransactionContext transaction) {
        final int extracted = this.battery.extractEnergy((int) Math.min(Integer.MAX_VALUE, maxAmount), true);
        if (extracted > 0) {
            this.updateSnapshots(transaction);
            this.battery.extractEnergy(extracted, false);
        }
        return extracted;
    }

    @Override
    public long getAmount() {
        return this.battery.getEnergy();
    }

    @Override
    public long getCapacity() {
        return this.battery.maxEnergy;
    }

    @Override
    protected Integer createSnapshot() {
        return this.battery.getEnergy();
    }

    @Override
    protected void readSnapshot(final Integer snapshot) {
        this.battery.setEnergy(snapshot);
    }
}
