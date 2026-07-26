package dev.simulated_team.simulated.multiloader.tanks;

import dev.simulated_team.simulated.service.SimFluidService;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Tuple;
import org.jetbrains.annotations.Nullable;

/**
 * A loader-independent container for tank information to be wrapped by fabric and forge specific implementations
 */
public class SingleTank {
    public long amount;
    public final long capacity;
    public CFluidType type = CFluidType.BLANK;

    public SingleTank(final int capacity) {
        this.capacity = SimFluidService.INSTANCE.mbToLoaderUnits(capacity);
    }

    // helper methods
    public static long calculateInsert(final SingleTank tank, final CFluidType insertedType, final long maxAmount) {
        if (insertedType.equals(tank.type) || tank.type.isBlank()) {
            return Math.min(maxAmount, tank.capacity - tank.amount);
        }

        return 0;
    }

    public static void applyInsert(final SingleTank tank, final CFluidType insertedType, final long insertedAmount) {
        tank.type = insertedType;
        tank.amount += insertedAmount;
    }

    public static long calculateExtract(final SingleTank tank, final CFluidType extractedType, final long maxAmount) {
        if (extractedType.equals(tank.type)) {
            return Math.min(maxAmount, tank.amount);
        }
        return 0;
    }

    public static void applyExtract(final SingleTank tank, final long extractedAmount) {
        tank.amount -= extractedAmount;
        if (tank.amount == 0) {
            tank.type = CFluidType.BLANK;
        }
    }

    /**
     * Performs an insertion of a fluid
     * @param insertedType The type of fluid
     * @param maxAmount    The amount attempted to insert
     * @param simulate     Whether this action shouldn't be applied - always false on fabric, which uses snapshots and rollback
     * @param beforeApply  Used for fabric transaction snapshotting
     * @return The amount actually inserted
     */
    public long insert(final CFluidType insertedType, final long maxAmount, final boolean simulate, @Nullable final Runnable beforeApply) {
        final long v = calculateInsert(this, insertedType, maxAmount);
        if (!simulate) {
            if (beforeApply != null)
                beforeApply.run();
            applyInsert(this, insertedType, v);
        }
        return v;
    }

    public final long insert(final CFluidType insertedType, final long maxAmount, final boolean simulate) {
        return this.insert(insertedType, maxAmount, simulate, null);
    }

    /**
     * Performs an extraction of a fluid
     * @param extractedType The type of fluid
     * @param maxAmount     The amount attempted to extract
     * @param simulate      Whether this action shouldn't be applied - always false on fabric, which uses snapshots and rollback
     * @param beforeApply  Used for fabric transaction snapshotting
     * @return The amount actually extracted
     */
    public long extract(final CFluidType extractedType, final long maxAmount, final boolean simulate, @Nullable final Runnable beforeApply) {
        final long v = calculateExtract(this, extractedType, maxAmount);
        if (!simulate) {
            if (beforeApply != null)
                beforeApply.run();
            applyExtract(this, v);
        }
        return v;
    }

    public final long extract(final CFluidType insertedType, final long maxAmount, final boolean simulate) {
        return this.extract(insertedType, maxAmount, simulate, null);
    }

    public void read(final CompoundTag tag) {
        this.amount = tag.getIntOr("Amount", 0);
        this.type = CFluidType.read(tag.getCompoundOrEmpty("Variant"));
    }

    public CompoundTag write() {
        final CompoundTag tag = new CompoundTag();
        tag.putLong("Amount", this.amount);
        tag.put("Variant", this.type.write());
        return tag;
    }

    /**
     * @return Type and amount of fluid to be snapshotted for fabrics fluid API
     */
    public Tuple<CFluidType, Long> createSnapshot() {
        return new Tuple<>(this.type, this.amount);
    }

    /**
     * Reads type and amount of fluid to be restored from for fabrics fluid API
     */
    public void readSnapshot(final CFluidType type, final long amount) {
        this.type = type;
        this.amount = amount;
    }
}
