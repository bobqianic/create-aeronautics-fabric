package dev.simulated_team.simulated.compat.create;

import com.google.common.collect.ImmutableList;
import com.zurrtum.create.client.foundation.blockEntity.ValueSettingsBoard;
import com.zurrtum.create.client.foundation.blockEntity.ValueSettingsFormatter;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.ValueSettings;
import com.zurrtum.create.foundation.blockEntity.behaviour.scrollValue.ServerScrollValueBehaviour;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class ScrollValueBehaviour extends ServerScrollValueBehaviour {
    private static final String LEGACY_DATA_KEY = "simulated_legacy_scroll";

    public static class StepContext {
        public int currentValue;
        public boolean forward;
        public boolean shift;
        public boolean control;
    }

    public Component label;
    public int value;
    protected final ValueBoxTransform slotPositioning;
    protected Function<Integer, String> formatter = String::valueOf;
    protected Supplier<Boolean> active = () -> true;

    public ScrollValueBehaviour(final Component label, final SmartBlockEntity blockEntity,
                                final ValueBoxTransform slotPositioning) {
        super(blockEntity);
        this.label = label;
        this.slotPositioning = slotPositioning;
    }

    @Override
    public ScrollValueBehaviour between(final int min, final int max) {
        super.between(min, max);
        value = Mth.clamp(value, min, max);
        return this;
    }

    public ScrollValueBehaviour withFormatter(final Function<Integer, String> formatter) {
        this.formatter = formatter;
        return this;
    }

    @Override
    public ScrollValueBehaviour withCallback(final Consumer<Integer> callback) {
        super.withCallback(callback);
        return this;
    }

    @Override
    public int getValue() {
        return value;
    }

    @Override
    public void setValue(final int value) {
        final int clamped = Mth.clamp(value, min, max);
        if (this.value == clamped) {
            return;
        }
        this.value = clamped;
        callback.accept(clamped);
        blockEntity.setChanged();
        blockEntity.sendData();
    }

    public ScrollValueBehaviour onlyActiveWhen(final Supplier<Boolean> condition) {
        active = condition;
        return this;
    }

    public String formatValue() {
        return formatter.apply(value);
    }

    public boolean isActive() {
        return active.get();
    }

    public boolean acceptsValueSettings() {
        return true;
    }

    public boolean mayInteract(final Player player) {
        return true;
    }

    public boolean onlyVisibleWithWrench() {
        return false;
    }

    public int netId() {
        return 0;
    }

    public ValueBoxTransform getSlotPositioning() {
        return slotPositioning;
    }

    public ValueSettingsBoard createBoard(final Player player, final BlockHitResult hitResult) {
        return new ValueSettingsBoard(label, max, 10, ImmutableList.of(Component.literal("Value")), new ValueSettingsFormatter());
    }

    @Override
    public ValueSettings getValueSettings() {
        return new ValueSettings(0, value);
    }

    @Override
    public void setValueSettings(final Player player, final ValueSettings settings, final boolean ctrlDown) {
        setValue(settings.value());
    }

    @Override
    public void onShortInteract(final Player player, final InteractionHand hand, final Direction side, final BlockHitResult hitResult) {
    }

    public boolean testHit(final Vec3 hit) {
        final Vec3 localHit = hit.subtract(Vec3.atLowerCornerOf(blockEntity.getBlockPos()));
        return slotPositioning.testHit(blockEntity.getLevel(), blockEntity.getBlockPos(), blockEntity.getBlockState(), localHit);
    }

    @Override
    public void write(final ValueOutput output, final boolean clientPacket) {
        super.value = value;
        super.write(output, clientPacket);
        final CompoundTag tag = new CompoundTag();
        tag.putInt("Value", value);
        write(tag, blockEntity.getLevel() != null ? blockEntity.getLevel().registryAccess() : net.minecraft.core.RegistryAccess.EMPTY, clientPacket);
        output.store(LEGACY_DATA_KEY + getClass().getName(), CompoundTag.CODEC, tag);
    }

    @Override
    public void read(final ValueInput input, final boolean clientPacket) {
        super.read(input, clientPacket);
        value = super.value;
        final CompoundTag tag = input.read(LEGACY_DATA_KEY + getClass().getName(), CompoundTag.CODEC).orElseGet(CompoundTag::new);
        value = Mth.clamp(tag.getIntOr("Value", value), min, max);
        read(tag, input.lookup(), clientPacket);
    }

    public void write(final CompoundTag tag, final HolderLookup.Provider registries, final boolean clientPacket) {
    }

    public void read(final CompoundTag tag, final HolderLookup.Provider registries, final boolean clientPacket) {
    }

    public boolean writeToClipboard(final HolderLookup.Provider registries, final CompoundTag tag, final Direction side) {
        tag.putInt("Value", value);
        return true;
    }

    public boolean readFromClipboard(final HolderLookup.Provider registries, final CompoundTag tag, final Player player,
                                     final Direction side, final boolean simulate) {
        if (!simulate) {
            setValue(tag.getIntOr("Value", value));
        }
        return true;
    }

    protected void playFeedbackSound(final Object ignored) {
    }
}
