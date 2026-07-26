package dev.simulated_team.simulated.fabric.transfer;

import dev.simulated_team.simulated.multiloader.tanks.CFluidType;
import dev.simulated_team.simulated.multiloader.tanks.SingleTank;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;

import java.util.Collections;
import java.util.Iterator;

public final class SingleTankStorage extends SnapshotParticipant<SingleTankStorage.Snapshot>
        implements Storage<FluidVariant>, StorageView<FluidVariant> {
    private final SingleTank tank;

    public SingleTankStorage(final SingleTank tank) {
        this.tank = tank;
    }

    @Override
    public long insert(final FluidVariant resource, final long maxAmount, final TransactionContext transaction) {
        final CFluidType type = new CFluidType(resource.getFluid(), null);
        final long inserted = SingleTank.calculateInsert(this.tank, type, maxAmount);
        if (inserted > 0) {
            this.updateSnapshots(transaction);
            SingleTank.applyInsert(this.tank, type, inserted);
        }
        return inserted;
    }

    @Override
    public long extract(final FluidVariant resource, final long maxAmount, final TransactionContext transaction) {
        final long extracted = SingleTank.calculateExtract(
                this.tank, new CFluidType(resource.getFluid(), null), maxAmount);
        if (extracted > 0) {
            this.updateSnapshots(transaction);
            SingleTank.applyExtract(this.tank, extracted);
        }
        return extracted;
    }

    @Override
    public boolean isResourceBlank() {
        return this.tank.type.isBlank();
    }

    @Override
    public FluidVariant getResource() {
        return this.isResourceBlank() ? FluidVariant.blank() : FluidVariant.of(this.tank.type.fluid);
    }

    @Override
    public long getAmount() {
        return this.tank.amount;
    }

    @Override
    public long getCapacity() {
        return this.tank.capacity;
    }

    @Override
    public Iterator<StorageView<FluidVariant>> iterator() {
        return Collections.<StorageView<FluidVariant>>singleton(this).iterator();
    }

    @Override
    protected Snapshot createSnapshot() {
        return new Snapshot(this.tank.type, this.tank.amount);
    }

    @Override
    protected void readSnapshot(final Snapshot snapshot) {
        this.tank.readSnapshot(snapshot.type(), snapshot.amount());
    }

    record Snapshot(CFluidType type, long amount) {
    }
}
