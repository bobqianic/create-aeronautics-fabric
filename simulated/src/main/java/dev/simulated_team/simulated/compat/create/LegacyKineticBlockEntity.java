package dev.simulated_team.simulated.compat.create;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.List;

public class LegacyKineticBlockEntity extends com.zurrtum.create.content.kinetics.base.KineticBlockEntity
        implements LegacyKineticTooltipProvider {
    public LegacyKineticBlockEntity(final BlockEntityType<?> type, final BlockPos pos, final BlockState state) {
        super(type, pos, state);
    }

    @Override
    protected void write(final ValueOutput output, final boolean clientPacket) {
        super.write(output, clientPacket);
        LegacyKineticPersistence.write(output, level, clientPacket, this::write);
    }

    @Override
    protected void read(final ValueInput input, final boolean clientPacket) {
        super.read(input, clientPacket);
        LegacyKineticPersistence.read(input, clientPacket, this::read);
    }

    @Override
    public void writeSafe(final ValueOutput output) {
        super.writeSafe(output);
        LegacyKineticPersistence.write(output, level, false, this::write);
    }

    protected void write(final CompoundTag tag, final HolderLookup.Provider registries, final boolean clientPacket) {
    }

    protected void read(final CompoundTag tag, final HolderLookup.Provider registries, final boolean clientPacket) {
    }

    @Override
    public boolean addToTooltip(final List<Component> tooltip, final boolean isPlayerSneaking) {
        return LegacyKineticTooltipBridge.addToTooltip(this, tooltip, isPlayerSneaking);
    }

    @Override
    public boolean addToGoggleTooltip(final List<Component> tooltip, final boolean isPlayerSneaking) {
        return LegacyKineticTooltipBridge.addToGoggleTooltip(this, tooltip, isPlayerSneaking);
    }

    protected void addStressImpactStats(final List<Component> tooltip, final float stressAtBase) {
        LegacyKineticTooltipBridge.addStressImpactStats(this, tooltip, stressAtBase);
    }
}
